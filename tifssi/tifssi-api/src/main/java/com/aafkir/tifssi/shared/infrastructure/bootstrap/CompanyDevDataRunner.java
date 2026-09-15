package com.aafkir.tifssi.shared.infrastructure.bootstrap;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ActionCreateRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.absences.domain.model.Absence;
import com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineSourceType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceStatus;
import com.aafkir.tifssi.billing.domain.model.Invoice;
import com.aafkir.tifssi.billing.domain.model.InvoiceLine;
import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceRepository;
import com.aafkir.tifssi.crm.application.service.ActionService;
import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.application.service.ContactService;
import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import com.aafkir.tifssi.crm.infrastructure.repository.ActionRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.response.MissionResponse;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseReportRepository;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.application.service.NeedService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.ProfileSkill;
import com.aafkir.tifssi.staffing.domain.model.Skill;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileSkillRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SkillRepository;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimesheetRepository;
import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class CompanyDevDataRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyDevDataRunner.class);
    private static final List<String> DEV_COMPANY_REGISTRATION_NUMBERS = List.of(
            "RCS-PAR-812441209",
            "RCS-LYO-501228774",
            "RCS-NAN-449772318",
            "RCS-BOD-913558620",
            "RCS-LIL-702884119"
    );

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final ActionRepository actionRepository;
    private final NeedRepository needRepository;
    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProfileSkillRepository profileSkillRepository;
    private final ProjectRepository projectRepository;
    private final MissionRepository missionRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final TimesheetRepository timesheetRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseReportRepository expenseReportRepository;
    private final InvoiceRepository invoiceRepository;
    private final AbsenceRepository absenceRepository;
    private final CompanyService companyService;
    private final ContactService contactService;
    private final ActionService actionService;
    private final NeedService needService;
    private final ProfileService profileService;
    private final ProjectService projectService;
    private final MissionService missionService;

    public CompanyDevDataRunner(
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            ActionRepository actionRepository,
            NeedRepository needRepository,
            ProfileRepository profileRepository,
            SkillRepository skillRepository,
            ProfileSkillRepository profileSkillRepository,
            ProjectRepository projectRepository,
            MissionRepository missionRepository,
            TimeEntryRepository timeEntryRepository,
            TimesheetRepository timesheetRepository,
            ExpenseRepository expenseRepository,
            ExpenseReportRepository expenseReportRepository,
            InvoiceRepository invoiceRepository,
            AbsenceRepository absenceRepository,
            CompanyService companyService,
            ContactService contactService,
            ActionService actionService,
            NeedService needService,
            ProfileService profileService,
            ProjectService projectService,
            MissionService missionService
    ) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.actionRepository = actionRepository;
        this.needRepository = needRepository;
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
        this.profileSkillRepository = profileSkillRepository;
        this.projectRepository = projectRepository;
        this.missionRepository = missionRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.timesheetRepository = timesheetRepository;
        this.expenseRepository = expenseRepository;
        this.expenseReportRepository = expenseReportRepository;
        this.invoiceRepository = invoiceRepository;
        this.absenceRepository = absenceRepository;
        this.companyService = companyService;
        this.contactService = contactService;
        this.actionService = actionService;
        this.needService = needService;
        this.profileService = profileService;
        this.projectService = projectService;
        this.missionService = missionService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hasExistingDevCompanyData()) {
            LOGGER.info("Skipping base dev company data loading because the dev company data set already exists.");
            seedOperationalDemoDataFromExistingData();
            return;
        }

        seedDemoData();
        LOGGER.info("Loaded dev company data set: 5 companies, 7 contacts, 15 actions, 10 needs, 10 profiles, 23 skills, 30 profile skills, 6 projects, 7 missions, 42 time entries, 20 expenses, 10 invoices, 18 absences.");
    }

    private boolean hasExistingDevCompanyData() {
        return companyRepository.existsByRegistrationNumberIn(DEV_COMPANY_REGISTRATION_NUMBERS);
    }

    private void seedDemoData() {
        CompanyResponse novalys = createCompany(new CompanySeed(
                "Novalys Banque SA", "Novalys", "client", "client", "Banque",
                "Nadia Mercier", "Paris", "Comite de cadrage migration KYC",
                LocalDate.of(2026, 6, 8), "RCS-PAR-812441209", "FR43812441209",
                "https://novalys-banque.example", "achats@novalys-banque.example",
                "+33142881020", "18 avenue de l'Opera", "Paris", "75002"
        ));
        CompanyResponse mediance = createCompany(new CompanySeed(
                "Mediance Sante SAS", "Mediance", "prospect", "prospect", "Sante",
                "Thomas Vidal", "Lyon", "Relance proposition portail patient",
                LocalDate.of(2026, 6, 10), "RCS-LYO-501228774", "FR18501228774",
                "https://mediance-sante.example", "contact@mediance-sante.example",
                "+33472661450", "42 quai Charles de Gaulle", "Lyon", "69006"
        ));
        CompanyResponse equinoxe = createCompany(new CompanySeed(
                "Equinoxe Cloud Services", "Equinoxe Cloud", "partenaire", "client", "Cloud",
                "Samira Ait Omar", "Nantes", "Atelier capacite equipe DevOps",
                LocalDate.of(2026, 6, 12), "RCS-NAN-449772318", "FR74449772318",
                "https://equinoxe-cloud.example", "partners@equinoxe-cloud.example",
                "+33240551290", "5 mail Pablo Picasso", "Nantes", "44000"
        ));
        CompanyResponse atelier = createCompany(new CompanySeed(
                "Atelier Data Factory SARL", "Atelier Data", "fournisseur", "client", "Data",
                "Julien Caron", "Bordeaux", "Validation contrat sous-traitance BI",
                LocalDate.of(2026, 6, 15), "RCS-BOD-913558620", "FR29913558620",
                "https://atelier-data.example", "finance@atelier-data.example",
                "+33556442010", "11 cours du Chapeau Rouge", "Bordeaux", "33000"
        ));
        CompanyResponse helios = createCompany(new CompanySeed(
                "Helios Retail Group", "Helios Retail", "client", "inactive", "Retail",
                "Claire Dumas", "Lille", "Point de cloture run omnicanal",
                LocalDate.of(2026, 6, 18), "RCS-LIL-702884119", "FR61702884119",
                "https://helios-retail.example", "it@helios-retail.example",
                "+33320553370", "90 rue Nationale", "Lille", "59000"
        ));

        ContactResponse amel = createContact(novalys.id(), "Amel", "Roche", "Directrice transformation", "amel.roche@novalys-banque.example", "+33601020304", true);
        ContactResponse patrick = createContact(novalys.id(), "Patrick", "Legrand", "Responsable architecture", "patrick.legrand@novalys-banque.example", "+33601020305", false);
        ContactResponse elise = createContact(mediance.id(), "Elise", "Garnier", "Directrice des operations", "elise.garnier@mediance-sante.example", "+33611121314", true);
        ContactResponse yacine = createContact(equinoxe.id(), "Yacine", "Belkacem", "Partner Manager", "yacine.belkacem@equinoxe-cloud.example", "+33622232425", true);
        ContactResponse camille = createContact(atelier.id(), "Camille", "Renard", "Fondatrice", "camille.renard@atelier-data.example", "+33633343536", true);
        ContactResponse marc = createContact(atelier.id(), "Marc", "Petit", "Responsable delivery", "marc.petit@atelier-data.example", "+33633343537", false);
        ContactResponse sandra = createContact(helios.id(), "Sandra", "Moreau", "DSI retail", "sandra.moreau@helios-retail.example", "+33644454647", true);

        createAction(novalys.id(), amel.id(), ActionType.CALL, "Appel qualification besoin KYC", "Confirmer le perimetre du lot onboarding et les interlocuteurs conformite.", LocalDate.of(2026, 6, 6), ActionStatus.DONE, "Nadia Mercier", "Envoyer la synthese de qualification");
        createAction(novalys.id(), patrick.id(), ActionType.MEETING, "Atelier architecture risque", "Revue des flux, dependances IAM et contraintes SSI du programme.", LocalDate.of(2026, 6, 9), ActionStatus.DONE, "Patrick Legrand", "Partager le compte-rendu");
        createAction(novalys.id(), amel.id(), ActionType.PRESENTATION, "Presentation du dispositif staffing", "Presenter l'organisation proposee pour la phase de build KYC.", LocalDate.of(2026, 6, 11), ActionStatus.TODO, "Nadia Mercier", "Preparer la presentation");
        createAction(novalys.id(), null, ActionType.EMAIL, "Email de suivi apres reunion", "Envoyer les decisions prises en comite et les points ouverts achats.", LocalDate.of(2026, 6, 12), ActionStatus.TODO, "Nadia Mercier", "Relancer le contact");
        createAction(novalys.id(), amel.id(), ActionType.REMINDER, "Relance validation budget", "Verifier l'arbitrage budget avant lancement de la phase 2.", LocalDate.of(2026, 6, 17), ActionStatus.TODO, "Nadia Mercier", "Attendre validation client");
        createAction(novalys.id(), null, ActionType.OTHER, "Preparer prochain atelier", "Consolider les questions ouvertes avant l'atelier de cadrage multi-equipes.", LocalDate.of(2026, 6, 20), ActionStatus.CANCELLED, "Patrick Legrand", "Aucune");

        createAction(mediance.id(), elise.id(), ActionType.PRESENTATION, "Preparer demonstration produit", "Finaliser la demo portail patient pour le comite de direction.", LocalDate.of(2026, 6, 10), ActionStatus.TODO, "Thomas Vidal", "Preparer la presentation");
        createAction(mediance.id(), elise.id(), ActionType.EMAIL, "Envoyer synthese de reunion", "Partager les decisions prises sur le parcours patient et les points HL7.", LocalDate.of(2026, 6, 13), ActionStatus.DONE, "Thomas Vidal", "Envoyer le devis");
        createAction(mediance.id(), null, ActionType.REMINDER, "Relance apres envoi de proposition", "Reprendre contact avant la fin du trimestre pour valider le cadrage interop.", LocalDate.of(2026, 6, 18), ActionStatus.CANCELLED, "Thomas Vidal", "Relancer le contact");

        createAction(equinoxe.id(), yacine.id(), ActionType.MEETING, "Point d'avancement avec le contact", "Faire le point sur le staffing Kubernetes et le rythme des entretiens.", LocalDate.of(2026, 5, 28), ActionStatus.DONE, "Samira Ait Omar", "Aucune");
        createAction(equinoxe.id(), null, ActionType.CALL, "Appel de cadrage mission data", "Qualifier le besoin FinOps et confirmer la priorite des chantiers.", LocalDate.of(2026, 6, 3), ActionStatus.DONE, "Samira Ait Omar", "Aucune");
        createAction(equinoxe.id(), yacine.id(), ActionType.EMAIL, "Email de suivi apres reunion", "Envoyer les profils proposes et le recap des disponibilites.", LocalDate.of(2026, 6, 5), ActionStatus.DONE, "Samira Ait Omar", "Aucune");

        createAction(atelier.id(), camille.id(), ActionType.CALL, "Point d'avancement avec le contact", "Verifier l'alignement sur les priorites CFO pour la squad BI finance.", LocalDate.of(2026, 6, 7), ActionStatus.TODO, "Julien Caron", "Organiser un atelier");
        createAction(atelier.id(), marc.id(), ActionType.OTHER, "Preparation comite delivery", "Consolider les alertes delivery et les decisions attendues du comite.", LocalDate.of(2026, 6, 16), ActionStatus.TODO, "Julien Caron", "Envoyer la synthese de reunion");
        createAction(atelier.id(), null, ActionType.REMINDER, "Relance validation budget", "Obtenir le feu vert sur l'extension de perimetre Power BI achats.", LocalDate.of(2026, 6, 21), ActionStatus.TODO, "Julien Caron", "Attendre validation client");

        NeedResponse kycNeed = createNeed(novalys.id(), amel.id(), "NEED-2026-101", "Migration KYC et onboarding", NeedStatus.WON, "Paris", true, "780.00", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 31));
        createNeed(novalys.id(), patrick.id(), "NEED-2026-102", "Audit architecture risque", NeedStatus.PRESENTED, "Paris", true, "850.00", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        createNeed(novalys.id(), amel.id(), "NEED-2026-103", "Renfort QA reglementaire", NeedStatus.OPEN, "Paris", true, "620.00", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31));
        createNeed(mediance.id(), elise.id(), "NEED-2026-201", "Portail patient et agenda", NeedStatus.SHORTLIST, "Lyon", true, "690.00", LocalDate.of(2026, 7, 15), LocalDate.of(2027, 2, 28));
        createNeed(mediance.id(), elise.id(), "NEED-2026-202", "Cadrage interop HL7", NeedStatus.DRAFT, "Lyon", false, "740.00", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31));
        NeedResponse devopsNeed = createNeed(equinoxe.id(), yacine.id(), "NEED-2026-301", "Equipe DevOps Kubernetes", NeedStatus.WON, "Nantes", true, "760.00", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 12, 15));
        createNeed(equinoxe.id(), yacine.id(), "NEED-2026-302", "Industrialisation FinOps", NeedStatus.OPEN, "Remote", true, "710.00", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 11, 30));
        NeedResponse biNeed = createNeed(atelier.id(), camille.id(), "NEED-2026-401", "Squad BI finance", NeedStatus.WON, "Bordeaux", true, "690.00", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 10, 31));
        createNeed(atelier.id(), marc.id(), "NEED-2026-402", "Renfort Power BI achats", NeedStatus.PRESENTED, "Bordeaux", true, "640.00", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        NeedResponse retailNeed = createNeed(helios.id(), sandra.id(), "NEED-2026-501", "Run applicatif omnicanal", NeedStatus.WON, "Lille", true, "590.00", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 6, 30));

        Map<String, Skill> skills = createSkills();

        // The only persisted availability state is the active flag and the return date.
        ProfileResponse nina = createProfile(ProfileType.INTERNAL, "Nina", "Dupont", "+33610111213", "Developpeuse Backend", "Expert", null, LocalDate.of(2027, 2, 1), true);
        ProfileResponse hugo = createProfile(ProfileType.INTERNAL, "Hugo", "Bernard", "+33614151617", "Chef de projet digital", "Senior", null, LocalDate.of(2027, 2, 1), true);
        ProfileResponse sara = createProfile(ProfileType.EXTERNAL, "Sara", "Benhamou", "+33618192021", "UX/UI Designer", "Expert", new BigDecimal("780.00"), LocalDate.of(2026, 9, 4), true);
        ProfileResponse omar = createProfile(ProfileType.EXTERNAL, "Omar", "El Idrissi", "+33622232425", "Developpeur Fullstack", "Senior", new BigDecimal("720.00"), LocalDate.of(2026, 10, 1), true);
        ProfileResponse claire = createProfile(ProfileType.INTERNAL, "Claire", "Morel", "+33626272829", "Data Analyst", "Confirmé", null, LocalDate.of(2026, 11, 2), true);
        ProfileResponse mehdi = createProfile(ProfileType.EXTERNAL, "Mehdi", "Kaci", "+33630313233", "DevOps Engineer", "Senior", new BigDecimal("760.00"), LocalDate.of(2027, 1, 1), true);
        ProfileResponse lucas = createProfile(ProfileType.EXTERNAL, "Lucas", "Fontaine", "+33634353637", "Developpeur Backend", "Junior", new BigDecimal("520.00"), LocalDate.of(2026, 9, 4), true);
        ProfileResponse amina = createProfile(ProfileType.EXTERNAL, "Amina", "Diallo", "+33638394041", "Consultante SAP", "Expert", new BigDecimal("800.00"), LocalDate.of(2026, 12, 1), true);
        ProfileResponse julie = createProfile(ProfileType.EXTERNAL, "Julie", "Caron", "+33642434445", "Consultante Salesforce", "Confirmé", new BigDecimal("650.00"), LocalDate.of(2026, 9, 18), true);
        ProfileResponse thomas = createProfile(ProfileType.EXTERNAL, "Thomas", "Leroy", "+33646474849", "Data Analyst", "Junior", new BigDecimal("480.00"), LocalDate.of(2026, 10, 15), false);

        associateSkills(nina, skills, new ProfileSkillSeed("PYTHON", 5, 11, true), new ProfileSkillSeed("DJANGO", 5, 8, false), new ProfileSkillSeed("POSTGRESQL", 5, 10, false));
        associateSkills(hugo, skills, new ProfileSkillSeed("AGILE", 5, 12, true), new ProfileSkillSeed("JIRA", 5, 10, false), new ProfileSkillSeed("CONFLUENCE", 4, 10, false));
        associateSkills(sara, skills, new ProfileSkillSeed("FIGMA", 5, 10, true), new ProfileSkillSeed("UX_RESEARCH", 5, 9, false), new ProfileSkillSeed("DESIGN_SYSTEM", 5, 7, false));
        associateSkills(omar, skills, new ProfileSkillSeed("REACT", 5, 8, true), new ProfileSkillSeed("NODEJS", 5, 8, false), new ProfileSkillSeed("AWS", 4, 5, false));
        associateSkills(claire, skills, new ProfileSkillSeed("SQL", 5, 7, true), new ProfileSkillSeed("POWER_BI", 5, 6, false), new ProfileSkillSeed("EXCEL", 5, 9, false));
        associateSkills(mehdi, skills, new ProfileSkillSeed("AWS", 5, 9, true), new ProfileSkillSeed("DOCKER", 5, 8, false), new ProfileSkillSeed("CI_CD", 5, 8, false));
        associateSkills(lucas, skills, new ProfileSkillSeed("NODEJS", 4, 3, true), new ProfileSkillSeed("POSTGRESQL", 4, 3, false), new ProfileSkillSeed("REACT", 3, 2, false));
        associateSkills(amina, skills, new ProfileSkillSeed("SAP_S4HANA", 5, 14, true), new ProfileSkillSeed("SAP_MM", 5, 12, false), new ProfileSkillSeed("SAP_SD", 4, 8, false));
        associateSkills(julie, skills, new ProfileSkillSeed("SALESFORCE", 5, 6, true), new ProfileSkillSeed("APEX", 4, 5, false), new ProfileSkillSeed("LWC", 4, 4, false));
        associateSkills(thomas, skills, new ProfileSkillSeed("SQL", 4, 2, true), new ProfileSkillSeed("POWER_BI", 3, 2, false), new ProfileSkillSeed("EXCEL", 5, 4, false));

        ProjectResponse kycProject = createProject(novalys.id(), kycNeed.id(), amel.id(), "PRJ-2026-101", "Migration KYC et onboarding", ProjectStatus.ACTIVE, "212000.00", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 31));
        ProjectResponse riskProject = createProject(novalys.id(), null, patrick.id(), "PRJ-2026-102", "Audit flash architecture risque", ProjectStatus.DRAFT, "42000.00", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        ProjectResponse portalProject = createProject(mediance.id(), null, elise.id(), "PRJ-2026-201", "Prototype portail patient", ProjectStatus.DRAFT, "68000.00", LocalDate.of(2026, 7, 15), LocalDate.of(2026, 11, 30));
        ProjectResponse devopsProject = createProject(equinoxe.id(), devopsNeed.id(), yacine.id(), "PRJ-2026-301", "Equipe DevOps Kubernetes", ProjectStatus.ACTIVE, "164000.00", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 12, 15));
        ProjectResponse biProject = createProject(atelier.id(), biNeed.id(), camille.id(), "PRJ-2026-401", "Squad BI finance", ProjectStatus.ACTIVE, "126000.00", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 10, 31));
        ProjectResponse retailProject = createProject(helios.id(), retailNeed.id(), sandra.id(), "PRJ-2026-501", "Run applicatif omnicanal", ProjectStatus.CLOSED, "98000.00", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 6, 30));

        MissionResponse ninaKyc = createMission(kycProject.id(), nina.id(), "Lead Backend", "800.00", 100, MissionStatus.ACTIVE, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 31));
        MissionResponse hugoKyc = createMission(kycProject.id(), hugo.id(), "Project Manager", "850.00", 60, MissionStatus.ACTIVE, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 31));
        MissionResponse saraRisk = createMission(riskProject.id(), sara.id(), "Architecte SI", "920.00", 40, MissionStatus.PLANNED, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        MissionResponse clairePortal = createMission(portalProject.id(), claire.id(), "Business Analyst", "560.00", 80, MissionStatus.PLANNED, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 11, 30));
        MissionResponse omarDevops = createMission(devopsProject.id(), omar.id(), "QA Lead DevOps", "700.00", 100, MissionStatus.ACTIVE, LocalDate.of(2026, 5, 15), LocalDate.of(2026, 12, 15));
        MissionResponse mehdiBi = createMission(biProject.id(), mehdi.id(), "Data Engineer", "740.00", 90, MissionStatus.ACTIVE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 10, 31));
        MissionResponse claireRetail = createMission(retailProject.id(), claire.id(), "Support applicatif", "560.00", 70, MissionStatus.ENDED, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 6, 30));

        seedTimeEntries(ninaKyc, hugoKyc, saraRisk, clairePortal, omarDevops, mehdiBi);
        seedTimesheets();
        seedMonthlyWorkflow(ninaKyc, hugoKyc, saraRisk, clairePortal);
        seedExpenses(ninaKyc, hugoKyc, saraRisk, clairePortal, omarDevops, mehdiBi);
        seedExpenseReports();
        seedInvoices(kycProject, riskProject, portalProject, devopsProject, biProject, retailProject);
        seedAbsences(nina, hugo, sara, omar, claire, mehdi);
    }

    private void seedOperationalDemoDataFromExistingData() {
        MissionResponse ninaKyc = existingMission("PRJ-2026-101", "nina.dupont@tifssi-dev.example", "Lead Backend");
        MissionResponse hugoKyc = existingMission("PRJ-2026-101", "hugo.bernard@tifssi-dev.example", "Project Manager");
        MissionResponse saraRisk = existingMission("PRJ-2026-102", "sara.benhamou@tifssi-dev.example", "Architecte SI");
        MissionResponse clairePortal = existingMission("PRJ-2026-201", "claire.morel@tifssi-dev.example", "Business Analyst");
        MissionResponse omarDevops = existingMission("PRJ-2026-301", "omar.el.idrissi@tifssi-dev.example", "QA Lead DevOps");
        MissionResponse mehdiBi = existingMission("PRJ-2026-401", "mehdi.kaci@tifssi-dev.example", "Data Engineer");

        seedTimeEntries(ninaKyc, hugoKyc, saraRisk, clairePortal, omarDevops, mehdiBi);
        seedTimesheets();
        seedMonthlyWorkflow(ninaKyc, hugoKyc, saraRisk, clairePortal);
        seedExpenses(ninaKyc, hugoKyc, saraRisk, clairePortal, omarDevops, mehdiBi);
        seedExpenseReports();
        seedInvoices(
                existingProject("PRJ-2026-101"),
                existingProject("PRJ-2026-102"),
                existingProject("PRJ-2026-201"),
                existingProject("PRJ-2026-301"),
                existingProject("PRJ-2026-401"),
                existingProject("PRJ-2026-501")
        );
        seedAbsences(
                existingProfile("nina.dupont@tifssi-dev.example"),
                existingProfile("hugo.bernard@tifssi-dev.example"),
                existingProfile("sara.benhamou@tifssi-dev.example"),
                existingProfile("omar.el.idrissi@tifssi-dev.example"),
                existingProfile("claire.morel@tifssi-dev.example"),
                existingProfile("mehdi.kaci@tifssi-dev.example")
        );

        LOGGER.info("Loaded missing operational dev demo data: time entries, expenses, invoices and absences.");
    }

    private CompanyResponse createCompany(CompanySeed seed) {
        return companyService.create(new CompanyCreateRequest(
                seed.legalName(), seed.displayName(), seed.relationType(), seed.status(),
                seed.sector(), seed.managerName(), seed.agency(), seed.currentAction(),
                seed.actionDate(), seed.registrationNumber(), seed.vatNumber(), seed.websiteUrl(),
                seed.emailAddress(), seed.phoneNumber(), seed.billingAddress(), seed.cityName(),
                seed.postalCode(), "FR"
        ));
    }

    private ContactResponse createContact(Long companyId, String firstName, String lastName, String jobTitle, String email, String phone, boolean primary) {
        return contactService.create(new ContactCreateRequest(companyId, firstName, lastName, jobTitle, email, phone, primary));
    }

    private void createAction(
            Long companyId,
            Long contactId,
            ActionType type,
            String subject,
            String comment,
            LocalDate dueDate,
            ActionStatus status,
            String responsibleName,
            String nextStep
    ) {
        actionService.create(new ActionCreateRequest(companyId, contactId, type, subject, comment, dueDate, status, responsibleName, nextStep));
    }

    private NeedResponse createNeed(Long companyId, Long contactId, String reference, String title, NeedStatus status, String location, boolean remote, String rate, LocalDate startDate, LocalDate endDate) {
        return needService.create(new NeedCreateRequest(
                companyId, contactId, reference, title,
                "Besoin detecte pour %s avec qualification commerciale et suivi staffing.".formatted(title.toLowerCase()),
                status, startDate, endDate, location, remote, new BigDecimal(rate)
        ));
    }

    private Map<String, Skill> createSkills() {
        return List.of(
                        new SkillSeed("REACT", "React"), new SkillSeed("NODEJS", "Node.js"), new SkillSeed("AWS", "AWS"),
                        new SkillSeed("PYTHON", "Python"), new SkillSeed("DJANGO", "Django"), new SkillSeed("POSTGRESQL", "PostgreSQL"),
                        new SkillSeed("FIGMA", "Figma"), new SkillSeed("UX_RESEARCH", "UX Research"), new SkillSeed("DESIGN_SYSTEM", "Design System"),
                        new SkillSeed("SQL", "SQL"), new SkillSeed("POWER_BI", "Power BI"), new SkillSeed("EXCEL", "Excel"),
                        new SkillSeed("SAP_S4HANA", "SAP S/4HANA"), new SkillSeed("SAP_MM", "SAP MM"), new SkillSeed("SAP_SD", "SAP SD"),
                        new SkillSeed("SALESFORCE", "Salesforce"), new SkillSeed("APEX", "Apex"), new SkillSeed("LWC", "Lightning Web Components"),
                        new SkillSeed("DOCKER", "Docker"), new SkillSeed("CI_CD", "CI/CD"), new SkillSeed("AGILE", "Agile"),
                        new SkillSeed("JIRA", "Jira"), new SkillSeed("CONFLUENCE", "Confluence")
                ).stream()
                .map(this::createSkill)
                .collect(java.util.stream.Collectors.toMap(Skill::getSkillCode, skill -> skill));
    }

    private Skill createSkill(SkillSeed seed) {
        Skill skill = new Skill();
        skill.setSkillCode(seed.code());
        skill.setSkillName(seed.name());
        return skillRepository.save(skill);
    }

    private void associateSkills(ProfileResponse profileResponse, Map<String, Skill> skills, ProfileSkillSeed... profileSkillSeeds) {
        com.aafkir.tifssi.staffing.domain.model.Profile profile = profileRepository.getReferenceById(profileResponse.id());
        for (ProfileSkillSeed profileSkillSeed : profileSkillSeeds) {
            ProfileSkill profileSkill = new ProfileSkill();
            profileSkill.setProfile(profile);
            profileSkill.setSkill(skills.get(profileSkillSeed.skillCode()));
            profileSkill.setProficiencyLevel(profileSkillSeed.proficiencyLevel());
            profileSkill.setYearsOfExperience(profileSkillSeed.yearsOfExperience());
            profileSkill.setPrimarySkill(profileSkillSeed.primarySkill());
            profileSkillRepository.save(profileSkill);
        }
    }

    private ProfileResponse createProfile(ProfileType type, String firstName, String lastName, String phone, String jobTitle, String seniority, BigDecimal rate, LocalDate availability, boolean active) {
        String email = "%s.%s@tifssi-dev.example".formatted(toEmailSlug(firstName), toEmailSlug(lastName));
        return profileService.create(new ProfileCreateRequest(type, firstName, lastName, email, phone, jobTitle, seniority, active, rate, availability));
    }

    private String toEmailSlug(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".");
    }

    private ProjectResponse createProject(Long companyId, Long originNeedId, Long contactId, String code, String name, ProjectStatus status, String budget, LocalDate startDate, LocalDate endDate) {
        return projectService.create(new ProjectCreateRequest(
                companyId, originNeedId, contactId, code, name,
                "Projet lie au suivi societe et aux besoins detectes.",
                status, startDate, endDate, new BigDecimal(budget)
        ));
    }

    private MissionResponse createMission(Long projectId, Long profileId, String role, String rate, int allocation, MissionStatus status, LocalDate startDate, LocalDate endDate) {
        return missionService.create(new MissionCreateRequest(projectId, profileId, role, startDate, endDate, new BigDecimal(rate), allocation, status));
    }

    private ProfileResponse existingProfile(String emailAddress) {
        com.aafkir.tifssi.staffing.domain.model.Profile profile = profileRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new IllegalStateException("Missing dev profile " + emailAddress));
        return new ProfileResponse(
                profile.getId(),
                profile.getType(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmailAddress(),
                profile.getPhoneNumber(),
                profile.getJobTitle(),
                profile.getSeniorityLabel(),
                profile.isActive(),
                profile.getDefaultDailyRate(),
                profile.getAvailabilityDate(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private ProjectResponse existingProject(String projectCode) {
        com.aafkir.tifssi.projects.domain.model.Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new IllegalStateException("Missing dev project " + projectCode));
        return new ProjectResponse(
                project.getId(),
                project.getCompany().getId(),
                project.getOriginNeed() == null ? null : project.getOriginNeed().getId(),
                project.getContact() == null ? null : project.getContact().getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getBudgetAmount(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private MissionResponse existingMission(String projectCode, String profileEmailAddress, String roleName) {
        com.aafkir.tifssi.projects.domain.model.Mission mission = missionRepository
                .findByProjectProjectCodeAndProfileEmailAddressAndRoleName(projectCode, profileEmailAddress, roleName)
                .orElseThrow(() -> new IllegalStateException("Missing dev mission " + projectCode + " / " + profileEmailAddress + " / " + roleName));
        return new MissionResponse(
                mission.getId(),
                mission.getProject().getId(),
                mission.getProfile().getId(),
                mission.getRoleName(),
                mission.getStartDate(),
                mission.getEndDate(),
                mission.getDailyRate(),
                mission.getAllocationPercent(),
                mission.getStatus(),
                mission.getCreatedAt(),
                mission.getUpdatedAt()
        );
    }

    private void seedTimeEntries(MissionResponse... missions) {
        List<TimeEntrySeed> seeds = List.of(
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 8, 3), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 8, 4), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 8, 5), "0.50", TimeEntryUnitType.HALF_DAY, TimeEntryStatus.DRAFT, "Revue architecture"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 8, 6), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 8, 7), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 9, 1), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Cadrage fonctionnel"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 9, 2), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 9, 4), "0.50", TimeEntryUnitType.HALF_DAY, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[1], LocalDate.of(2026, 8, 3), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Preparation comite projet"),
                new TimeEntrySeed(missions[1], LocalDate.of(2026, 8, 4), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[1], LocalDate.of(2026, 8, 5), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Cadrage fonctionnel"),
                new TimeEntrySeed(missions[1], LocalDate.of(2026, 9, 1), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Suivi planning"),
                new TimeEntrySeed(missions[1], LocalDate.of(2026, 9, 3), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Preparation comite projet"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 8, 10), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Revue architecture"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 8, 11), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 8, 12), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Cadrage fonctionnel"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 9, 7), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 9, 8), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Revue architecture"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 8, 17), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Cadrage fonctionnel"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 8, 18), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 8, 20), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 9, 1), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Cadrage fonctionnel"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 9, 2), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[3], LocalDate.of(2026, 9, 3), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 8, 3), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 8, 4), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 8, 5), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 8, 6), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Revue architecture"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 8, 7), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Preparation comite projet"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 9, 7), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 9, 9), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 8, 24), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 8, 25), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 8, 26), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 8, 27), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 8, 28), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Preparation comite projet"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 9, 1), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 9, 2), "0.50", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Tests et recette"),
                new TimeEntrySeed(missions[5], LocalDate.of(2026, 9, 4), "1.00", TimeEntryUnitType.DAY, TimeEntryStatus.DRAFT, "Correction anomalies"),
                new TimeEntrySeed(missions[0], LocalDate.of(2026, 9, 7), "8.00", TimeEntryUnitType.HOUR, TimeEntryStatus.DRAFT, "Developpement API"),
                new TimeEntrySeed(missions[2], LocalDate.of(2026, 9, 9), "4.00", TimeEntryUnitType.HOUR, TimeEntryStatus.DRAFT, "Atelier client"),
                new TimeEntrySeed(missions[4], LocalDate.of(2026, 9, 10), "8.00", TimeEntryUnitType.HOUR, TimeEntryStatus.DRAFT, "Tests et recette")
        );

        seeds.forEach(this::createTimeEntry);
    }

    private void createTimeEntry(TimeEntrySeed seed) {
        if (timeEntryRepository.existsByMissionIdAndProfileIdAndWorkDateAndComment(
                seed.mission().id(),
                seed.mission().profileId(),
                seed.workDate(),
                seed.comment()
        )) {
            return;
        }

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setMission(missionRepository.getReferenceById(seed.mission().id()));
        timeEntry.setProfile(profileRepository.getReferenceById(seed.mission().profileId()));
        timeEntry.setWorkDate(seed.workDate());
        timeEntry.setQuantity(new BigDecimal(seed.quantity()));
        timeEntry.setUnitType(seed.unitType());
        timeEntry.setStatus(seed.status());
        timeEntry.setComment(seed.comment());
        Timesheet sheet = timesheetRepository.findByProfileIdAndYearAndMonth(seed.mission().profileId(), seed.workDate().getYear(), seed.workDate().getMonthValue()).orElseGet(() -> {
            Timesheet created = new Timesheet();
            created.setProfile(timeEntry.getProfile());
            created.setYear(seed.workDate().getYear());
            created.setMonth(seed.workDate().getMonthValue());
            return timesheetRepository.save(created);
        });
        timeEntry.setTimesheet(sheet);
        timeEntryRepository.save(timeEntry);
    }

    private void seedMonthlyWorkflow(MissionResponse... missions) {
        TimesheetStatus[] statuses = {TimesheetStatus.DRAFT, TimesheetStatus.SUBMITTED, TimesheetStatus.VALIDATED, TimesheetStatus.REJECTED};
        for (int i = 0; i < statuses.length; i++) {
            MissionResponse mission = missions[i];
            if (timesheetRepository.findByProfileIdAndYearAndMonth(mission.profileId(), 2026, 7).isPresent()) continue;
            Timesheet sheet = new Timesheet();
            sheet.setProfile(profileRepository.getReferenceById(mission.profileId()));
            sheet.setYear(2026);
            sheet.setMonth(7);
            sheet.setStatus(statuses[i]);
            if (statuses[i] != TimesheetStatus.DRAFT) sheet.setSubmittedAt(java.time.Instant.parse("2026-07-31T17:00:00Z"));
            if (statuses[i] == TimesheetStatus.VALIDATED) sheet.setValidatedAt(java.time.Instant.parse("2026-08-01T09:00:00Z"));
            if (statuses[i] == TimesheetStatus.REJECTED) {
                sheet.setRejectedAt(java.time.Instant.parse("2026-08-01T09:00:00Z"));
                sheet.setRejectionReason("Merci de compléter la journée du 21 juillet.");
            }
            timesheetRepository.save(sheet);
            for (int day : new int[]{20, 21}) {
                createTimeEntry(new TimeEntrySeed(mission, LocalDate.of(2026, 7, day), "1.00", TimeEntryUnitType.DAY,
                        TimeEntryStatus.DRAFT, "CRA mensuel DEV - saisie journalière"));
            }
        }
    }

    private void seedTimesheets() {
        timeEntryRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(e -> java.util.Map.entry(e.getProfile().getId(), java.time.YearMonth.from(e.getWorkDate()))))
                .forEach((key, entries) -> {
                    Timesheet sheet = timesheetRepository.findByProfileIdAndYearAndMonth(key.getKey(), key.getValue().getYear(), key.getValue().getMonthValue()).orElseGet(() -> {
                        Timesheet created = new Timesheet();
                        created.setProfile(profileRepository.getReferenceById(key.getKey()));
                        created.setYear(key.getValue().getYear()); created.setMonth(key.getValue().getMonthValue());
                        created.setStatus(TimesheetStatus.DRAFT);
                        if (created.getStatus() == TimesheetStatus.VALIDATED) created.setValidatedAt(java.time.Instant.now());
                        return timesheetRepository.save(created);
                    });
                    entries.stream().filter(e -> e.getTimesheet() == null).forEach(e -> e.setTimesheet(sheet));
                });
    }

    private void seedExpenses(MissionResponse... missions) {
        List<ExpenseSeed> seeds = List.of(
                new ExpenseSeed(missions[0], LocalDate.of(2026, 8, 5), ExpenseCategory.TRAVEL, "128.40", true, ExpenseStatus.DRAFT, "Train aller-retour"),
                new ExpenseSeed(missions[0], LocalDate.of(2026, 8, 6), ExpenseCategory.MEAL, "32.50", true, ExpenseStatus.DRAFT, "Repas client"),
                new ExpenseSeed(missions[0], LocalDate.of(2026, 9, 2), ExpenseCategory.TRAVEL, "46.00", false, ExpenseStatus.DRAFT, "Taxi gare client"),
                new ExpenseSeed(missions[1], LocalDate.of(2026, 8, 4), ExpenseCategory.MEAL, "28.90", true, ExpenseStatus.VALIDATED, "Dejeuner atelier client"),
                new ExpenseSeed(missions[1], LocalDate.of(2026, 9, 3), ExpenseCategory.OTHER, "18.00", false, ExpenseStatus.DRAFT, "Parking comite projet"),
                new ExpenseSeed(missions[2], LocalDate.of(2026, 8, 10), ExpenseCategory.TRAVEL, "164.00", true, ExpenseStatus.VALIDATED, "Train aller-retour"),
                new ExpenseSeed(missions[2], LocalDate.of(2026, 8, 11), ExpenseCategory.ACCOMMODATION, "189.00", true, ExpenseStatus.VALIDATED, "Hotel mission Paris"),
                new ExpenseSeed(missions[2], LocalDate.of(2026, 9, 8), ExpenseCategory.MEAL, "24.60", true, ExpenseStatus.REJECTED, "Repas client hors politique"),
                new ExpenseSeed(missions[3], LocalDate.of(2026, 8, 18), ExpenseCategory.MEAL, "21.80", false, ExpenseStatus.VALIDATED, "Repas equipe recette"),
                new ExpenseSeed(missions[3], LocalDate.of(2026, 8, 20), ExpenseCategory.TRAVEL, "72.00", true, ExpenseStatus.DRAFT, "Transport atelier"),
                new ExpenseSeed(missions[3], LocalDate.of(2026, 9, 1), ExpenseCategory.OFFICE_SUPPLY, "36.40", false, ExpenseStatus.DRAFT, "Support atelier"),
                new ExpenseSeed(missions[4], LocalDate.of(2026, 8, 3), ExpenseCategory.TRAVEL, "96.00", true, ExpenseStatus.VALIDATED, "Deplacement atelier client"),
                new ExpenseSeed(missions[4], LocalDate.of(2026, 8, 4), ExpenseCategory.MEAL, "19.70", false, ExpenseStatus.VALIDATED, "Repas mission"),
                new ExpenseSeed(missions[4], LocalDate.of(2026, 8, 6), ExpenseCategory.ACCOMMODATION, "142.00", true, ExpenseStatus.VALIDATED, "Hotel mission Nantes"),
                new ExpenseSeed(missions[4], LocalDate.of(2026, 9, 7), ExpenseCategory.TRAVEL, "54.00", true, ExpenseStatus.DRAFT, "Taxi client"),
                new ExpenseSeed(missions[5], LocalDate.of(2026, 8, 24), ExpenseCategory.TRAVEL, "112.00", true, ExpenseStatus.VALIDATED, "Train aller-retour"),
                new ExpenseSeed(missions[5], LocalDate.of(2026, 8, 25), ExpenseCategory.ACCOMMODATION, "118.00", true, ExpenseStatus.VALIDATED, "Hotel mission Bordeaux"),
                new ExpenseSeed(missions[5], LocalDate.of(2026, 8, 27), ExpenseCategory.MEAL, "34.20", false, ExpenseStatus.VALIDATED, "Repas client"),
                new ExpenseSeed(missions[5], LocalDate.of(2026, 9, 2), ExpenseCategory.TRAVEL, "67.50", true, ExpenseStatus.REJECTED, "Taxi gare client"),
                new ExpenseSeed(missions[5], LocalDate.of(2026, 9, 4), ExpenseCategory.OTHER, "22.00", false, ExpenseStatus.DRAFT, "Frais divers projet")
        );

        seeds.forEach(this::createExpense);
    }

    private void createExpense(ExpenseSeed seed) {
        if (expenseRepository.existsByMissionIdAndProfileIdAndExpenseDateAndComment(
                seed.mission().id(),
                seed.mission().profileId(),
                seed.expenseDate(),
                seed.comment()
        )) {
            return;
        }

        Expense expense = new Expense();
        expense.setMission(missionRepository.getReferenceById(seed.mission().id()));
        expense.setProfile(profileRepository.getReferenceById(seed.mission().profileId()));
        expense.setExpenseDate(seed.expenseDate());
        expense.setCategory(seed.category());
        expense.setAmount(new BigDecimal(seed.amount()));
        expense.setCurrency("EUR");
        expense.setBillable(seed.billable());
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setComment(seed.comment());
        ExpenseReport report = expenseReportRepository.findByProfileIdAndYearAndMonth(
                        seed.mission().profileId(), seed.expenseDate().getYear(), seed.expenseDate().getMonthValue())
                .orElseGet(() -> {
                    ExpenseReport created = new ExpenseReport();
                    created.setProfile(profileRepository.getReferenceById(seed.mission().profileId()));
                    created.setYear(seed.expenseDate().getYear());
                    created.setMonth(seed.expenseDate().getMonthValue());
                    return expenseReportRepository.save(created);
                });
        expense.setExpenseReport(report);
        expenseRepository.save(expense);
    }

    private void seedExpenseReports() {
        expenseReportRepository.findAll().forEach(report -> {
            String email = report.getProfile().getEmailAddress();
            if (email.startsWith("nina.")) report.setStatus(ExpenseReportStatus.DRAFT);
            else if (email.startsWith("hugo.")) report.setStatus(ExpenseReportStatus.SUBMITTED);
            else if (email.startsWith("sara.")) report.setStatus(ExpenseReportStatus.VALIDATED);
            else if (email.startsWith("claire.")) {
                report.setStatus(ExpenseReportStatus.REJECTED);
                report.setRejectionReason("Merci de joindre le justificatif manquant.");
            }
            expenseReportRepository.save(report);
        });
    }

    private void seedInvoices(ProjectResponse... projects) {
        List<InvoiceSeed> seeds = List.of(
                new InvoiceSeed(projects[0], "INV-2026-0801", LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30), InvoiceStatus.PAID, "25000.00", "Forfait migration KYC - aout"),
                new InvoiceSeed(projects[0], "INV-2026-0901", LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 30), InvoiceStatus.ISSUED, "40000.00", "Forfait migration KYC - septembre"),
                new InvoiceSeed(projects[0], "INV-2026-0902", LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 15), InvoiceStatus.DRAFT, "15000.00", "Complement ateliers onboarding"),
                new InvoiceSeed(projects[1], "INV-2026-0802", LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 19), InvoiceStatus.ISSUED, "8000.00", "Audit architecture risque"),
                new InvoiceSeed(projects[1], "INV-2026-0903", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 10, 10), InvoiceStatus.CANCELLED, "6000.00", "Atelier architecture annule"),
                new InvoiceSeed(projects[2], "INV-2026-0803", LocalDate.of(2026, 8, 28), LocalDate.of(2026, 9, 27), InvoiceStatus.DRAFT, "12000.00", "Cadrage portail patient"),
                new InvoiceSeed(projects[3], "INV-2026-0804", LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30), InvoiceStatus.PAID, "32000.00", "Run DevOps Kubernetes - aout"),
                new InvoiceSeed(projects[3], "INV-2026-0904", LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 30), InvoiceStatus.ISSUED, "28000.00", "Run DevOps Kubernetes - septembre"),
                new InvoiceSeed(projects[4], "INV-2026-0805", LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30), InvoiceStatus.PAID, "18000.00", "Squad BI finance - aout"),
                new InvoiceSeed(projects[5], "INV-2026-0601", LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 30), InvoiceStatus.PAID, "22000.00", "Run applicatif omnicanal - cloture")
        );

        seeds.forEach(this::createInvoice);
    }

    private void createInvoice(InvoiceSeed seed) {
        if (invoiceRepository.existsByInvoiceNumber(seed.invoiceNumber())) {
            return;
        }

        BigDecimal totalHt = new BigDecimal(seed.totalHt());
        BigDecimal vatRate = new BigDecimal("20.00");
        BigDecimal totalVat = totalHt.multiply(vatRate).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);

        Invoice invoice = new Invoice();
        invoice.setProject(projectRepository.getReferenceById(seed.project().id()));
        invoice.setInvoiceNumber(seed.invoiceNumber());
        invoice.setIssueDate(seed.issueDate());
        invoice.setDueDate(seed.dueDate());
        invoice.setStatus(seed.status());
        invoice.setCurrency("EUR");
        invoice.setTotalHt(totalHt);
        invoice.setTotalVat(totalVat);
        invoice.setTotalTtc(totalHt.add(totalVat));
        invoice.setNotes(seed.description());

        InvoiceLine line = new InvoiceLine();
        line.setInvoice(invoice);
        line.setLineType(InvoiceLineType.FIXED_FEE);
        line.setDescription(seed.description());
        line.setQuantity(BigDecimal.ONE.setScale(2));
        line.setUnit("FORFAIT");
        line.setUnitPrice(totalHt);
        line.setVatRate(vatRate);
        line.setTotalHt(totalHt);
        line.setTotalVat(totalVat);
        line.setTotalTtc(totalHt.add(totalVat));
        line.setSourceType(InvoiceLineSourceType.MANUAL);
        line.setDisplayOrder(1);
        invoice.getInvoiceLines().add(line);

        invoiceRepository.save(invoice);
    }

    private void seedAbsences(ProfileResponse... profiles) {
        List<AbsenceSeed> seeds = List.of(
                new AbsenceSeed(profiles[0], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 8, 14), LocalDate.of(2026, 8, 14), "1.00", AbsenceStatus.APPROVED, "Conge pose"),
                new AbsenceSeed(profiles[0], AbsenceType.RTT, LocalDate.of(2026, 9, 11), LocalDate.of(2026, 9, 11), "0.50", AbsenceStatus.SUBMITTED, "Demi-journee RTT"),
                new AbsenceSeed(profiles[1], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 21), "5.00", AbsenceStatus.APPROVED, "Conges ete"),
                new AbsenceSeed(profiles[1], AbsenceType.OTHER, LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 18), "1.00", AbsenceStatus.DRAFT, "Demande personnelle"),
                new AbsenceSeed(profiles[2], AbsenceType.SICK_LEAVE, LocalDate.of(2026, 8, 24), LocalDate.of(2026, 8, 25), "2.00", AbsenceStatus.APPROVED, "Arret maladie"),
                new AbsenceSeed(profiles[2], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 7), "3.00", AbsenceStatus.SUBMITTED, "Conges octobre"),
                new AbsenceSeed(profiles[3], AbsenceType.RTT, LocalDate.of(2026, 8, 28), LocalDate.of(2026, 8, 28), "1.00", AbsenceStatus.APPROVED, "RTT"),
                new AbsenceSeed(profiles[3], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 22), "2.00", AbsenceStatus.REJECTED, "Conge refuse planning"),
                new AbsenceSeed(profiles[4], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), "3.00", AbsenceStatus.APPROVED, "Conges ete"),
                new AbsenceSeed(profiles[4], AbsenceType.RTT, LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 25), "1.00", AbsenceStatus.SUBMITTED, "RTT fin septembre"),
                new AbsenceSeed(profiles[5], AbsenceType.OTHER, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 31), "1.00", AbsenceStatus.APPROVED, "Absence exceptionnelle"),
                new AbsenceSeed(profiles[5], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 16), "5.00", AbsenceStatus.SUBMITTED, "Conges octobre"),
                new AbsenceSeed(profiles[2], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 7), "1.00", AbsenceStatus.APPROVED, "Conge ponctuel"),
                new AbsenceSeed(profiles[3], AbsenceType.SICK_LEAVE, LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 14), "1.00", AbsenceStatus.SUBMITTED, "Maladie"),
                new AbsenceSeed(profiles[5], AbsenceType.RTT, LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 7), "0.50", AbsenceStatus.APPROVED, "Demi-journee RTT"),
                new AbsenceSeed(profiles[1], AbsenceType.OTHER, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2), "1.00", AbsenceStatus.DRAFT, "Absence previsionnelle"),
                new AbsenceSeed(profiles[0], AbsenceType.SICK_LEAVE, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 15), "1.00", AbsenceStatus.REJECTED, "Justificatif manquant"),
                new AbsenceSeed(profiles[4], AbsenceType.PAID_LEAVE, LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 31), "6.00", AbsenceStatus.SUBMITTED, "Conges fin annee")
        );

        seeds.forEach(this::createAbsence);
    }

    private void createAbsence(AbsenceSeed seed) {
        if (absenceRepository.existsByProfileIdAndStartDateAndEndDateAndComment(
                seed.profile().id(),
                seed.startDate(),
                seed.endDate(),
                seed.comment()
        )) {
            return;
        }

        Absence absence = new Absence();
        absence.setProfile(profileRepository.getReferenceById(seed.profile().id()));
        absence.setType(seed.type());
        absence.setStartDate(seed.startDate());
        absence.setEndDate(seed.endDate());
        absence.setQuantity(new BigDecimal(seed.quantity()));
        absence.setStatus(seed.status());
        absence.setComment(seed.comment());
        absenceRepository.save(absence);
    }

    private record CompanySeed(
            String legalName,
            String displayName,
            String relationType,
            String status,
            String sector,
            String managerName,
            String agency,
            String currentAction,
            LocalDate actionDate,
            String registrationNumber,
            String vatNumber,
            String websiteUrl,
            String emailAddress,
            String phoneNumber,
            String billingAddress,
            String cityName,
            String postalCode
    ) {
    }

    private record SkillSeed(String code, String name) {
    }

    private record ProfileSkillSeed(String skillCode, int proficiencyLevel, int yearsOfExperience, boolean primarySkill) {
    }

    private record TimeEntrySeed(MissionResponse mission, LocalDate workDate, String quantity, TimeEntryUnitType unitType, TimeEntryStatus status, String comment) {
    }

    private record ExpenseSeed(MissionResponse mission, LocalDate expenseDate, ExpenseCategory category, String amount, boolean billable, ExpenseStatus status, String comment) {
    }

    private record InvoiceSeed(ProjectResponse project, String invoiceNumber, LocalDate issueDate, LocalDate dueDate, InvoiceStatus status, String totalHt, String description) {
    }

    private record AbsenceSeed(ProfileResponse profile, AbsenceType type, LocalDate startDate, LocalDate endDate, String quantity, AbsenceStatus status, String comment) {
    }
}
