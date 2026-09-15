package com.aafkir.tifssi.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.shared.application.exception.RequestValidationException;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceLineRepository invoiceLineRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private InvoiceApiMapper invoiceApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Project project;
    private Mission mission;
    private Profile profile;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        project = new Project();
        project.setId(1L);
        project.setCompany(company);
        project.setProjectCode("PRJ-BILL-001");
        project.setProjectName("Billing MVP");
        project.setStatus(ProjectStatus.ACTIVE);

        profile = new Profile();
        profile.setId(2L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);

        mission = new Mission();
        mission.setId(3L);
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Developer");
        mission.setDailyRate(new BigDecimal("800.00"));
        mission.setStatus(MissionStatus.ACTIVE);

        lenient().when(invoiceApiMapper.toSummaryResponse(any(Invoice.class)))
                .thenAnswer(invocation -> toSummaryResponse(invocation.getArgument(0)));
        lenient().when(invoiceApiMapper.toLineResponse(any(InvoiceLine.class)))
                .thenAnswer(invocation -> toLineResponse(invocation.getArgument(0)));
    }

    @Test
    void createShouldPersistInvoiceWithZeroTotals() {
        InvoiceCreateRequest request = new InvoiceCreateRequest(
                " INV-2026-001 ",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                InvoiceStatus.DRAFT
        );
        Invoice mappedInvoice = new Invoice();
        mappedInvoice.setIssueDate(request.issueDate());
        mappedInvoice.setDueDate(request.dueDate());
        mappedInvoice.setStatus(request.status());

        when(projectService.getProject(1L)).thenReturn(project);
        when(invoiceApiMapper.toEntity(request)).thenReturn(mappedInvoice);
        when(invoiceRepository.existsByInvoiceNumber("INV-2026-001")).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(99L);
            return invoice;
        });

        InvoiceDetailResponse response = invoiceService.create(1L, request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.invoiceNumber()).isEqualTo("INV-2026-001");
        assertThat(response.totalHt()).isEqualByComparingTo("0.00");
        assertThat(response.totalVat()).isEqualByComparingTo("0.00");
        assertThat(response.totalTtc()).isEqualByComparingTo("0.00");
        assertThat(response.lines()).isEmpty();

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getProject()).isSameAs(project);
    }

    @Test
    void createShouldRejectDuplicateInvoiceNumber() {
        InvoiceCreateRequest request = new InvoiceCreateRequest(
                "INV-2026-001",
                LocalDate.of(2026, 7, 1),
                null,
                InvoiceStatus.DRAFT
        );
        Invoice mappedInvoice = new Invoice();
        mappedInvoice.setIssueDate(request.issueDate());
        mappedInvoice.setStatus(request.status());

        when(projectService.getProject(1L)).thenReturn(project);
        when(invoiceApiMapper.toEntity(request)).thenReturn(mappedInvoice);
        when(invoiceRepository.existsByInvoiceNumber("INV-2026-001")).thenReturn(true);

        assertThatThrownBy(() -> invoiceService.create(1L, request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Invoice request is invalid.");

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void addManualLineShouldRecalculateInvoiceTotals() {
        Invoice invoice = draftInvoice();
        invoice.setId(5L);

        InvoiceLineCreateRequest request = new InvoiceLineCreateRequest(
                InvoiceLineType.FIXED_FEE,
                "  Package setup  ",
                new BigDecimal("1.00"),
                " PACKAGE ",
                new BigDecimal("1000.00"),
                new BigDecimal("20.00")
        );
        InvoiceLine mappedLine = new InvoiceLine();
        mappedLine.setLineType(request.lineType());
        mappedLine.setDescription(request.description());
        mappedLine.setQuantity(request.quantity());
        mappedLine.setUnit(request.unit());
        mappedLine.setUnitPrice(request.unitPrice());
        mappedLine.setVatRate(request.vatRate());

        List<InvoiceLine> storedLines = new ArrayList<>();

        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceApiMapper.toEntity(request)).thenReturn(mappedLine);
        when(invoiceLineRepository.findAllByInvoiceIdOrderByDisplayOrderAscIdAsc(5L)).thenAnswer(invocation -> List.copyOf(storedLines));
        when(invoiceLineRepository.save(any(InvoiceLine.class))).thenAnswer(invocation -> {
            InvoiceLine line = invocation.getArgument(0);
            line.setId(50L);
            storedLines.add(line);
            return line;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceDetailResponse response = invoiceService.addManualLine(5L, request);

        assertThat(response.totalHt()).isEqualByComparingTo("1000.00");
        assertThat(response.totalVat()).isEqualByComparingTo("200.00");
        assertThat(response.totalTtc()).isEqualByComparingTo("1200.00");
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().getFirst().lineType()).isEqualTo(InvoiceLineType.FIXED_FEE);
        assertThat(response.lines().getFirst().sourceType()).isEqualTo(InvoiceLineSourceType.MANUAL);
    }

    @Test
    void generateFromTimesAndExpensesShouldAppendValidatedSources() {
        Invoice invoice = draftInvoice();
        invoice.setId(5L);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setId(100L);
        timeEntry.setMission(mission);
        timeEntry.setProfile(profile);
        timeEntry.setWorkDate(LocalDate.of(2026, 7, 2));
        timeEntry.setQuantity(new BigDecimal("2.00"));
        timeEntry.setUnitType(TimeEntryUnitType.DAY);
        timeEntry.setStatus(TimeEntryStatus.VALIDATED);

        Expense expense = new Expense();
        expense.setId(200L);
        expense.setMission(mission);
        expense.setProfile(profile);
        expense.setExpenseDate(LocalDate.of(2026, 7, 3));
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setAmount(new BigDecimal("150.00"));
        expense.setCurrency("EUR");
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setBillable(true);
        com.aafkir.tifssi.expenses.domain.model.ExpenseReport report = new com.aafkir.tifssi.expenses.domain.model.ExpenseReport();
        report.setStatus(ExpenseReportStatus.VALIDATED);
        expense.setExpenseReport(report);

        List<InvoiceLine> storedLines = new ArrayList<>();

        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceLineRepository.findAllByInvoiceIdOrderByDisplayOrderAscIdAsc(5L)).thenAnswer(invocation -> List.copyOf(storedLines));
        when(missionRepository.findAllByProjectIdOrderByIdAsc(1L)).thenReturn(List.of(mission));
        when(timeEntryRepository.findAllByMissionIdInAndTimesheetStatusOrderByWorkDateAscIdAsc(List.of(3L), com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus.VALIDATED))
                .thenReturn(List.of(timeEntry));
        when(expenseRepository.findAllByMissionIdInAndExpenseReportStatusOrderByExpenseDateAscIdAsc(List.of(3L), ExpenseReportStatus.VALIDATED))
                .thenReturn(List.of(expense));
        when(invoiceLineRepository.saveAll(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<InvoiceLine> generatedLines = invocation.getArgument(0);
            long id = 1000L;
            for (InvoiceLine line : generatedLines) {
                line.setId(id++);
                storedLines.add(line);
            }
            return generatedLines;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceDetailResponse response = invoiceService.generateFromTimesAndExpenses(5L);

        assertThat(response.lines()).hasSize(2);
        assertThat(response.totalHt()).isEqualByComparingTo("1750.00");
        assertThat(response.totalVat()).isEqualByComparingTo("350.00");
        assertThat(response.totalTtc()).isEqualByComparingTo("2100.00");
        assertThat(response.lines().getFirst().lineType()).isEqualTo(InvoiceLineType.TIME);
        assertThat(response.lines().getFirst().sourceType()).isEqualTo(InvoiceLineSourceType.TIME_ENTRY);
        assertThat(response.lines().get(1).lineType()).isEqualTo(InvoiceLineType.EXPENSE);
        assertThat(response.lines().get(1).sourceType()).isEqualTo(InvoiceLineSourceType.EXPENSE);
    }

    private Invoice draftInvoice() {
        Invoice invoice = new Invoice();
        invoice.setProject(project);
        invoice.setInvoiceNumber("INV-2026-001");
        invoice.setIssueDate(LocalDate.of(2026, 7, 1));
        invoice.setDueDate(LocalDate.of(2026, 7, 31));
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTotalHt(new BigDecimal("0.00"));
        invoice.setTotalVat(new BigDecimal("0.00"));
        invoice.setTotalTtc(new BigDecimal("0.00"));
        return invoice;
    }

    private InvoiceSummaryResponse toSummaryResponse(Invoice invoice) {
        return new InvoiceSummaryResponse(
                invoice.getId(),
                invoice.getProject() == null ? null : invoice.getProject().getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getTotalHt(),
                invoice.getTotalVat(),
                invoice.getTotalTtc(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    private InvoiceLineResponse toLineResponse(InvoiceLine line) {
        return new InvoiceLineResponse(
                line.getId(),
                line.getInvoice() == null ? null : line.getInvoice().getId(),
                line.getLineType(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnit(),
                line.getUnitPrice(),
                line.getVatRate(),
                line.getTotalHt(),
                line.getTotalVat(),
                line.getTotalTtc(),
                line.getSourceType(),
                line.getSourceId(),
                line.getCreatedAt(),
                line.getUpdatedAt()
        );
    }
}
