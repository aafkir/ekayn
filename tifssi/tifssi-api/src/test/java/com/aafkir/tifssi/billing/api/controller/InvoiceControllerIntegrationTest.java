package com.aafkir.tifssi.billing.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceLineRepository;
import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceRepository;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InvoiceLineRepository invoiceLineRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private TimeEntryRepository timeEntryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseReportRepository expenseReportRepository;
    @Autowired
    private MissionRepository missionRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private com.aafkir.tifssi.timesheets.infrastructure.repository.TimesheetRepository timesheetRepository;

    @AfterEach
    void cleanUp() {
        invoiceLineRepository.deleteAllInBatch();
        invoiceRepository.deleteAllInBatch();
        timeEntryRepository.deleteAllInBatch();
        timesheetRepository.deleteAllInBatch();
        expenseRepository.deleteAllInBatch();
        expenseReportRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void billingEndpointsShouldSupportCreateGenerateListReadAndDeleteFlow() throws Exception {
        TestData testData = createProjectWithValidatedTimeAndExpense();

        MvcResult createInvoiceResult = mockMvc.perform(post("/api/projects/{projectId}/invoices", testData.project().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "INV-2026-001",
                                  "issueDate": "2026-07-01",
                                  "dueDate": "2026-07-31",
                                  "status": "DRAFT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(testData.project().getId()))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-2026-001"))
                .andExpect(jsonPath("$.totalHt").value(0))
                .andExpect(jsonPath("$.lines", hasSize(0)))
                .andReturn();

        long invoiceId = objectMapper.readTree(createInvoiceResult.getResponse().getContentAsString()).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(createInvoiceResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/invoices/" + invoiceId);

        mockMvc.perform(post("/api/invoices/{invoiceId}/lines", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineType": "FIXED_FEE",
                                  "description": "Setup package",
                                  "quantity": 1.00,
                                  "unit": "PACKAGE",
                                  "unitPrice": 1000.00,
                                  "vatRate": 20.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHt").value(1000.00))
                .andExpect(jsonPath("$.totalVat").value(200.00))
                .andExpect(jsonPath("$.totalTtc").value(1200.00))
                .andExpect(jsonPath("$.lines", hasSize(1)));

        mockMvc.perform(post("/api/invoices/{invoiceId}/generate-from-times-and-expenses", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHt").value(2750.00))
                .andExpect(jsonPath("$.totalVat").value(550.00))
                .andExpect(jsonPath("$.totalTtc").value(3300.00))
                .andExpect(jsonPath("$.lines", hasSize(3)))
                .andExpect(jsonPath("$.lines[1].lineType").value("TIME"))
                .andExpect(jsonPath("$.lines[1].sourceType").value("TIME_ENTRY"))
                .andExpect(jsonPath("$.lines[2].lineType").value("EXPENSE"))
                .andExpect(jsonPath("$.lines[2].sourceType").value("EXPENSE"));

        mockMvc.perform(get("/api/projects/{projectId}/invoices", testData.project().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(invoiceId))
                .andExpect(jsonPath("$[0].totalHt").value(2750.00));

        mockMvc.perform(get("/api/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId))
                .andExpect(jsonPath("$.lines", hasSize(3)));

        mockMvc.perform(delete("/api/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{projectId}/invoices", testData.project().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createShouldRejectDueDateBeforeIssueDate() throws Exception {
        TestData testData = createProjectWithValidatedTimeAndExpense();

        mockMvc.perform(post("/api/projects/{projectId}/invoices", testData.project().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "INV-2026-INVALID",
                                  "issueDate": "2026-07-10",
                                  "dueDate": "2026-07-01",
                                  "status": "DRAFT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("dueDate"));
    }

    private TestData createProjectWithValidatedTimeAndExpense() {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
        profile = profileRepository.save(profile);

        Project project = new Project();
        project.setCompany(company);
        project.setProjectCode("PRJ-BILL-001");
        project.setProjectName("Billing MVP");
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        Mission mission = new Mission();
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Developer");
        mission.setDailyRate(new BigDecimal("800.00"));
        mission.setStatus(MissionStatus.ACTIVE);
        mission = missionRepository.save(mission);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setMission(mission);
        timeEntry.setProfile(profile);
        timeEntry.setWorkDate(LocalDate.of(2026, 7, 2));
        timeEntry.setQuantity(new BigDecimal("2.00"));
        timeEntry.setUnitType(TimeEntryUnitType.DAY);
        timeEntry.setStatus(TimeEntryStatus.DRAFT);
        var sheet = new com.aafkir.tifssi.timesheets.domain.model.Timesheet();
        sheet.setProfile(profile);
        sheet.setYear(2026);
        sheet.setMonth(7);
        sheet.setStatus(com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus.VALIDATED);
        timeEntry.setTimesheet(timesheetRepository.save(sheet));
        timeEntryRepository.save(timeEntry);

        Expense expense = new Expense();
        expense.setMission(mission);
        expense.setProfile(profile);
        expense.setExpenseDate(LocalDate.of(2026, 7, 3));
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setAmount(new BigDecimal("150.00"));
        expense.setCurrency("EUR");
        expense.setBillable(true);
        expense.setStatus(ExpenseStatus.DRAFT);
        ExpenseReport report = new ExpenseReport();
        report.setProfile(profile);
        report.setYear(2026);
        report.setMonth(7);
        report.setStatus(ExpenseReportStatus.VALIDATED);
        expense.setExpenseReport(expenseReportRepository.save(report));
        expenseRepository.save(expense);

        return new TestData(project);
    }

    private record TestData(Project project) {
    }
}
