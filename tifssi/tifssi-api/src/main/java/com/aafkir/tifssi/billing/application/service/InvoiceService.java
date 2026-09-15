package com.aafkir.tifssi.billing.application.service;

import com.aafkir.tifssi.billing.api.dto.request.InvoiceCreateRequest;
import com.aafkir.tifssi.billing.api.dto.request.InvoiceLineCreateRequest;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceDetailResponse;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceLineResponse;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceSummaryResponse;
import com.aafkir.tifssi.billing.api.mapper.InvoiceApiMapper;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineSourceType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceStatus;
import com.aafkir.tifssi.billing.domain.model.Invoice;
import com.aafkir.tifssi.billing.domain.model.InvoiceLine;
import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceLineRepository;
import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceRepository;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.shared.api.error.ApiFieldError;
import com.aafkir.tifssi.shared.application.exception.RequestValidationException;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8.00");
    private static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("20.00");
    private static final String DEFAULT_CURRENCY = "EUR";

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final ProjectService projectService;
    private final MissionRepository missionRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ExpenseRepository expenseRepository;
    private final InvoiceApiMapper invoiceApiMapper;
    private final EntityValidationService entityValidationService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            ProjectService projectService,
            MissionRepository missionRepository,
            TimeEntryRepository timeEntryRepository,
            ExpenseRepository expenseRepository,
            InvoiceApiMapper invoiceApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.projectService = projectService;
        this.missionRepository = missionRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.expenseRepository = expenseRepository;
        this.invoiceApiMapper = invoiceApiMapper;
        this.entityValidationService = entityValidationService;
    }

    public InvoiceDetailResponse create(Long projectId, InvoiceCreateRequest request) {
        Project project = projectService.getProject(projectId);

        Invoice invoice = invoiceApiMapper.toEntity(request);
        invoice.setProject(project);
        invoice.setInvoiceNumber(normalizeInvoiceNumber(request.invoiceNumber()));
        invoice.setCurrency(DEFAULT_CURRENCY);
        invoice.setTotalHt(ZERO);
        invoice.setTotalVat(ZERO);
        invoice.setTotalTtc(ZERO);

        validateInvoice(invoice);
        entityValidationService.validate(invoice);
        return toDetailResponse(invoiceRepository.save(invoice), List.of());
    }

    @Transactional(readOnly = true)
    public List<InvoiceSummaryResponse> findAllByProject(Long projectId) {
        projectService.getProject(projectId);
        return invoiceRepository.findAllByProjectIdOrderByIssueDateAscIdAsc(projectId)
                .stream()
                .map(invoiceApiMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse findById(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        return toDetailResponse(invoice, getInvoiceLines(invoiceId));
    }

    public InvoiceDetailResponse addManualLine(Long invoiceId, InvoiceLineCreateRequest request) {
        Invoice invoice = getInvoice(invoiceId);
        ensureInvoiceIsEditable(invoice);

        InvoiceLine invoiceLine = invoiceApiMapper.toEntity(request);
        invoiceLine.setInvoice(invoice);
        invoiceLine.setDescription(normalizeDescription(request.description()));
        invoiceLine.setUnit(normalizeUnit(request.unit()));
        invoiceLine.setSourceType(InvoiceLineSourceType.MANUAL);
        invoiceLine.setSourceId(null);
        invoiceLine.setDisplayOrder(nextDisplayOrder(invoiceId));

        validateInvoiceLine(invoiceLine);
        calculateLineTotals(invoiceLine);
        entityValidationService.validate(invoiceLine);
        invoiceLineRepository.save(invoiceLine);

        List<InvoiceLine> lines = getInvoiceLines(invoiceId);
        recalculateInvoiceTotals(invoice, lines);
        invoiceRepository.save(invoice);
        return toDetailResponse(invoice, lines);
    }

    public InvoiceDetailResponse generateFromTimesAndExpenses(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        ensureInvoiceIsEditable(invoice);

        List<InvoiceLine> existingLines = getInvoiceLines(invoiceId);
        Set<String> existingSources = existingLines.stream()
                .filter(line -> line.getSourceType() != null && line.getSourceId() != null)
                .map(this::toSourceKey)
                .collect(Collectors.toCollection(HashSet::new));

        List<Mission> missions = missionRepository.findAllByProjectIdOrderByIdAsc(invoice.getProject().getId());
        if (missions.isEmpty()) {
            recalculateInvoiceTotals(invoice, existingLines);
            invoiceRepository.save(invoice);
            return toDetailResponse(invoice, existingLines);
        }

        Map<Long, Mission> missionsById = missions.stream().collect(Collectors.toMap(Mission::getId, mission -> mission));
        List<Long> missionIds = missions.stream().map(Mission::getId).toList();

        List<TimeEntry> timeEntries = timeEntryRepository.findAllByMissionIdInAndTimesheetStatusOrderByWorkDateAscIdAsc(
                missionIds,
                TimesheetStatus.VALIDATED
        );
        List<Expense> expenses = expenseRepository.findAllByMissionIdInAndExpenseReportStatusOrderByExpenseDateAscIdAsc(
                missionIds, ExpenseReportStatus.VALIDATED);
        expenses = expenses.stream().filter(Expense::isBillable).toList();

        validateExpenseCurrencies(expenses);

        int displayOrder = nextDisplayOrder(existingLines);
        List<InvoiceLine> generatedLines = new java.util.ArrayList<>();

        for (TimeEntry timeEntry : timeEntries) {
            String sourceKey = toSourceKey(InvoiceLineSourceType.TIME_ENTRY, timeEntry.getId());
            if (existingSources.contains(sourceKey)) {
                continue;
            }

            Mission mission = missionsById.get(timeEntry.getMission().getId());
            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setLineType(InvoiceLineType.TIME);
            line.setDescription("Temps " + mission.getRoleName() + " - " + timeEntry.getWorkDate());
            line.setQuantity(timeEntry.getQuantity());
            line.setUnit(timeEntry.getUnitType().name());
            line.setUnitPrice(resolveTimeEntryUnitPrice(mission, timeEntry));
            line.setVatRate(DEFAULT_VAT_RATE);
            line.setSourceType(InvoiceLineSourceType.TIME_ENTRY);
            line.setSourceId(timeEntry.getId());
            line.setDisplayOrder(displayOrder++);

            validateInvoiceLine(line);
            calculateLineTotals(line);
            entityValidationService.validate(line);
            generatedLines.add(line);
            existingSources.add(sourceKey);
        }

        for (Expense expense : expenses) {
            String sourceKey = toSourceKey(InvoiceLineSourceType.EXPENSE, expense.getId());
            if (existingSources.contains(sourceKey)) {
                continue;
            }

            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setLineType(InvoiceLineType.EXPENSE);
            line.setDescription("Frais " + expense.getCategory() + " - " + expense.getExpenseDate());
            line.setQuantity(BigDecimal.ONE.setScale(2));
            line.setUnit("UNIT");
            line.setUnitPrice(expense.getAmount());
            line.setVatRate(DEFAULT_VAT_RATE);
            line.setSourceType(InvoiceLineSourceType.EXPENSE);
            line.setSourceId(expense.getId());
            line.setDisplayOrder(displayOrder++);

            validateInvoiceLine(line);
            calculateLineTotals(line);
            entityValidationService.validate(line);
            generatedLines.add(line);
            existingSources.add(sourceKey);
        }

        if (!generatedLines.isEmpty()) {
            invoiceLineRepository.saveAll(generatedLines);
        }

        List<InvoiceLine> lines = getInvoiceLines(invoiceId);
        recalculateInvoiceTotals(invoice, lines);
        invoiceRepository.save(invoice);
        return toDetailResponse(invoice, lines);
    }

    public void delete(Long invoiceId) {
        invoiceRepository.delete(getInvoice(invoiceId));
    }

    public Invoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
    }

    private void validateInvoice(Invoice invoice) {
        if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(invoice.getIssueDate())) {
            throw new RequestValidationException(
                    "Invoice request is invalid.",
                    List.of(new ApiFieldError("dueDate", "dueDate must be greater than or equal to issueDate."))
            );
        }
        if (invoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber())) {
            throw new RequestValidationException(
                    "Invoice request is invalid.",
                    List.of(new ApiFieldError("invoiceNumber", "invoiceNumber must be unique."))
            );
        }
    }

    private void validateInvoiceLine(InvoiceLine line) {
        if (line.getUnitPrice() == null) {
            throw new IllegalArgumentException("unitPrice cannot be null.");
        }
        if (line.getUnitPrice().compareTo(ZERO) < 0 && line.getLineType() != InvoiceLineType.ADJUSTMENT) {
            throw new RequestValidationException(
                    "Invoice line request is invalid.",
                    List.of(new ApiFieldError("unitPrice", "Negative unitPrice is only allowed for ADJUSTMENT lines."))
            );
        }
    }

    private void ensureInvoiceIsEditable(Invoice invoice) {
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new RequestValidationException(
                    "Invoice request is invalid.",
                    List.of(new ApiFieldError("status", "Invoice must be in DRAFT status to be modified."))
            );
        }
    }

    private void validateExpenseCurrencies(List<Expense> expenses) {
        Set<String> currencies = expenses.stream()
                .map(Expense::getCurrency)
                .collect(Collectors.toSet());

        if (currencies.size() > 1) {
            throw new RequestValidationException(
                    "Invoice generation request is invalid.",
                    List.of(new ApiFieldError("invoiceId", "Validated expenses must use a single currency to generate an invoice."))
            );
        }
    }

    private BigDecimal resolveTimeEntryUnitPrice(Mission mission, TimeEntry timeEntry) {
        if (mission.getDailyRate() == null) {
            throw new RequestValidationException(
                    "Invoice generation request is invalid.",
                    List.of(new ApiFieldError("projectId", "Mission dailyRate is required to invoice validated time entries."))
            );
        }

        return timeEntry.getUnitType() == TimeEntryUnitType.HOUR
                ? mission.getDailyRate().divide(HOURS_PER_DAY, 2, RoundingMode.HALF_UP)
                : mission.getDailyRate();
    }

    private void calculateLineTotals(InvoiceLine line) {
        BigDecimal totalHt = line.getQuantity()
                .multiply(line.getUnitPrice())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVat = totalHt.multiply(line.getVatRate())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        line.setTotalHt(totalHt);
        line.setTotalVat(totalVat);
        line.setTotalTtc(totalHt.add(totalVat));
    }

    private void recalculateInvoiceTotals(Invoice invoice, List<InvoiceLine> lines) {
        invoice.setTotalHt(lines.stream().map(InvoiceLine::getTotalHt).reduce(ZERO, BigDecimal::add));
        invoice.setTotalVat(lines.stream().map(InvoiceLine::getTotalVat).reduce(ZERO, BigDecimal::add));
        invoice.setTotalTtc(lines.stream().map(InvoiceLine::getTotalTtc).reduce(ZERO, BigDecimal::add));
    }

    private List<InvoiceLine> getInvoiceLines(Long invoiceId) {
        return invoiceLineRepository.findAllByInvoiceIdOrderByDisplayOrderAscIdAsc(invoiceId);
    }

    private int nextDisplayOrder(Long invoiceId) {
        return nextDisplayOrder(getInvoiceLines(invoiceId));
    }

    private int nextDisplayOrder(List<InvoiceLine> lines) {
        return lines.stream()
                .map(InvoiceLine::getDisplayOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private InvoiceDetailResponse toDetailResponse(Invoice invoice, List<InvoiceLine> lines) {
        InvoiceSummaryResponse summary = invoiceApiMapper.toSummaryResponse(invoice);
        List<InvoiceLineResponse> lineResponses = lines.stream()
                .map(invoiceApiMapper::toLineResponse)
                .toList();

        return new InvoiceDetailResponse(
                summary.id(),
                summary.projectId(),
                summary.invoiceNumber(),
                summary.issueDate(),
                summary.dueDate(),
                summary.status(),
                summary.totalHt(),
                summary.totalVat(),
                summary.totalTtc(),
                lineResponses,
                summary.createdAt(),
                summary.updatedAt()
        );
    }

    private String normalizeInvoiceNumber(String invoiceNumber) {
        String normalizedInvoiceNumber = invoiceNumber == null ? null : invoiceNumber.trim();
        if (normalizedInvoiceNumber == null || normalizedInvoiceNumber.isEmpty()) {
            throw new RequestValidationException(
                    "Invoice request is invalid.",
                    List.of(new ApiFieldError("invoiceNumber", "invoiceNumber must not be blank."))
            );
        }
        return normalizedInvoiceNumber;
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }

    private String normalizeUnit(String unit) {
        return unit == null ? null : unit.trim();
    }

    private String toSourceKey(InvoiceLine line) {
        return toSourceKey(line.getSourceType(), line.getSourceId());
    }

    private String toSourceKey(InvoiceLineSourceType sourceType, Long sourceId) {
        return sourceType + ":" + sourceId;
    }
}
