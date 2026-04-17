package com.aafkir.tifssi.shared.infrastructure.bootstrap;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.application.service.ContactService;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.application.service.NeedService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataLoader.class);

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final NeedRepository needRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final MissionRepository missionRepository;
    private final CompanyService companyService;
    private final ContactService contactService;
    private final NeedService needService;
    private final ProfileService profileService;
    private final ProjectService projectService;
    private final MissionService missionService;

    public DemoDataLoader(
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            NeedRepository needRepository,
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            MissionRepository missionRepository,
            CompanyService companyService,
            ContactService contactService,
            NeedService needService,
            ProfileService profileService,
            ProjectService projectService,
            MissionService missionService
    ) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.needRepository = needRepository;
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.missionRepository = missionRepository;
        this.companyService = companyService;
        this.contactService = contactService;
        this.needService = needService;
        this.profileService = profileService;
        this.projectService = projectService;
        this.missionService = missionService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hasExistingBusinessData()) {
            LOGGER.info("Skipping demo data loading because business data already exists.");
            return;
        }

        seedDemoData();
        LOGGER.info("Loaded demo data set: 2 companies, 3 contacts, 3 needs, 5 profiles, 2 projects, 3 missions.");
    }

    private boolean hasExistingBusinessData() {
        return companyRepository.count() > 0
                || contactRepository.count() > 0
                || needRepository.count() > 0
                || profileRepository.count() > 0
                || projectRepository.count() > 0
                || missionRepository.count() > 0;
    }

    private void seedDemoData() {
        CompanyResponse acme = companyService.create(new CompanyCreateRequest(
                "Acme Conseil",
                "Acme",
                "RCS-123456",
                "FR12345678901",
                "https://acme.example",
                "contact@acme.example",
                "+33102030405",
                "12 rue de Paris",
                "Paris",
                "75001",
                "FR"
        ));
        CompanyResponse globex = companyService.create(new CompanyCreateRequest(
                "Globex Industrie",
                "Globex",
                "RCS-654321",
                "FR10987654321",
                "https://globex.example",
                "hello@globex.example",
                "+33411223344",
                "8 avenue des Alpes",
                "Lyon",
                "69002",
                "FR"
        ));

        ContactResponse leaMartin = contactService.create(new ContactCreateRequest(
                acme.id(),
                "Lea",
                "Martin",
                "Directrice achats",
                "lea.martin@acme.example",
                "+33601020304",
                true
        ));
        ContactResponse karimBenali = contactService.create(new ContactCreateRequest(
                acme.id(),
                "Karim",
                "Benali",
                "Responsable IT",
                "karim.benali@acme.example",
                "+33611121314",
                false
        ));
        ContactResponse soniaLeroy = contactService.create(new ContactCreateRequest(
                globex.id(),
                "Sonia",
                "Leroy",
                "Directrice transformation",
                "sonia.leroy@globex.example",
                "+33622232425",
                true
        ));

        NeedResponse staffingPlatformNeed = needService.create(new NeedCreateRequest(
                acme.id(),
                leaMartin.id(),
                "NEED-2026-001",
                "Plateforme staffing",
                "Renfort pour construire une plateforme Spring Boot de staffing.",
                NeedStatus.WON,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 12, 31),
                "Paris",
                true,
                new BigDecimal("650.00")
        ));
        NeedResponse erpMigrationNeed = needService.create(new NeedCreateRequest(
                globex.id(),
                soniaLeroy.id(),
                "NEED-2026-002",
                "Migration ERP finance",
                "Accompagnement de la migration ERP finance et reporting.",
                NeedStatus.WON,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 11, 30),
                "Lyon",
                false,
                new BigDecimal("820.00")
        ));
        needService.create(new NeedCreateRequest(
                acme.id(),
                karimBenali.id(),
                "NEED-2026-003",
                "Support run N2",
                "Besoin de support applicatif avec astreinte legere.",
                NeedStatus.OPEN,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 9, 30),
                "Lille",
                true,
                new BigDecimal("520.00")
        ));

        ProfileResponse ninaDupont = profileService.create(new ProfileCreateRequest(
                ProfileType.INTERNAL,
                "Nina",
                "Dupont",
                "nina.dupont@tifssi.example",
                "+33630313233",
                "Lead Backend",
                "Expert",
                true,
                new BigDecimal("750.00"),
                LocalDate.of(2026, 4, 15)
        ));
        ProfileResponse hugoBernard = profileService.create(new ProfileCreateRequest(
                ProfileType.INTERNAL,
                "Hugo",
                "Bernard",
                "hugo.bernard@tifssi.example",
                "+33634353637",
                "Project Manager",
                "Senior",
                true,
                new BigDecimal("820.00"),
                LocalDate.of(2026, 4, 22)
        ));
        ProfileResponse saraBenhamou = profileService.create(new ProfileCreateRequest(
                ProfileType.EXTERNAL,
                "Sara",
                "Benhamou",
                "sara.benhamou@freelance.example",
                "+33638394041",
                "ERP Consultant",
                "Senior",
                true,
                new BigDecimal("900.00"),
                LocalDate.of(2026, 5, 10)
        ));
        profileService.create(new ProfileCreateRequest(
                ProfileType.EXTERNAL,
                "Omar",
                "El Idrissi",
                "omar.idrissi@freelance.example",
                "+33642434445",
                "QA Lead",
                "Confirmed",
                true,
                new BigDecimal("680.00"),
                LocalDate.of(2026, 5, 20)
        ));
        profileService.create(new ProfileCreateRequest(
                ProfileType.INTERNAL,
                "Claire",
                "Morel",
                "claire.morel@tifssi.example",
                "+33646474849",
                "Support Analyst",
                "Intermediate",
                true,
                new BigDecimal("540.00"),
                LocalDate.of(2026, 6, 1)
        ));

        ProjectResponse staffingProject = projectService.create(new ProjectCreateRequest(
                acme.id(),
                staffingPlatformNeed.id(),
                leaMartin.id(),
                "PRJ-2026-001",
                "Plateforme staffing",
                "Projet client pour industrialiser le staffing et la facturation.",
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("128500.00")
        ));
        ProjectResponse erpProject = projectService.create(new ProjectCreateRequest(
                globex.id(),
                erpMigrationNeed.id(),
                soniaLeroy.id(),
                "PRJ-2026-002",
                "Migration ERP finance",
                "Projet de migration ERP et de reprise des flux finance.",
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 11, 30),
                new BigDecimal("185000.00")
        ));

        missionService.create(new MissionCreateRequest(
                staffingProject.id(),
                ninaDupont.id(),
                "Lead Backend",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 10, 31),
                new BigDecimal("780.00"),
                100,
                MissionStatus.ACTIVE
        ));
        missionService.create(new MissionCreateRequest(
                staffingProject.id(),
                hugoBernard.id(),
                "Project Manager",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("850.00"),
                50,
                MissionStatus.ACTIVE
        ));
        missionService.create(new MissionCreateRequest(
                erpProject.id(),
                saraBenhamou.id(),
                "ERP Consultant",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 11, 30),
                new BigDecimal("920.00"),
                80,
                MissionStatus.ACTIVE
        ));
    }
}
