package com.aafkir.tifssi.crm.application.service;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.CompanyPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.mapper.CompanyApiMapper;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final NeedRepository needRepository;
    private final ProjectRepository projectRepository;
    private final CompanyApiMapper companyApiMapper;
    private final EntityValidationService entityValidationService;

    public CompanyService(
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            NeedRepository needRepository,
            ProjectRepository projectRepository,
            CompanyApiMapper companyApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.needRepository = needRepository;
        this.projectRepository = projectRepository;
        this.companyApiMapper = companyApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return toResponse(getCompany(id));
    }

    public CompanyResponse create(CompanyCreateRequest request) {
        Company company = companyApiMapper.toEntity(request);
        entityValidationService.validate(company);
        return toResponse(companyRepository.save(company));
    }

    public CompanyResponse patch(Long id, CompanyPatchRequest request) {
        Company company = getCompany(id);

        if (JsonNullableUtils.isDefined(request.getName())) {
            company.setDisplayName(JsonNullableUtils.unwrap(request.getName()));
        }
        if (JsonNullableUtils.isDefined(request.getLegalName())) {
            company.setLegalName(JsonNullableUtils.unwrap(request.getLegalName()));
        }
        if (JsonNullableUtils.isDefined(request.getDisplayName())) {
            company.setDisplayName(JsonNullableUtils.unwrap(request.getDisplayName()));
        }
        if (JsonNullableUtils.isDefined(request.getRelationType())) {
            company.setRelationType(JsonNullableUtils.unwrap(request.getRelationType()));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            company.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }
        if (JsonNullableUtils.isDefined(request.getSector())) {
            company.setSector(JsonNullableUtils.unwrap(request.getSector()));
        }
        if (JsonNullableUtils.isDefined(request.getManager())) {
            company.setManagerName(JsonNullableUtils.unwrap(request.getManager()));
        }
        if (JsonNullableUtils.isDefined(request.getManagerName())) {
            company.setManagerName(JsonNullableUtils.unwrap(request.getManagerName()));
        }
        if (JsonNullableUtils.isDefined(request.getAgency())) {
            company.setAgency(JsonNullableUtils.unwrap(request.getAgency()));
        }
        if (JsonNullableUtils.isDefined(request.getCurrentAction())) {
            company.setCurrentAction(JsonNullableUtils.unwrap(request.getCurrentAction()));
        }
        if (JsonNullableUtils.isDefined(request.getNextAction())) {
            company.setCurrentAction(JsonNullableUtils.unwrap(request.getNextAction()));
        }
        if (JsonNullableUtils.isDefined(request.getActionDate())) {
            company.setActionDate(JsonNullableUtils.unwrap(request.getActionDate()));
        }
        if (JsonNullableUtils.isDefined(request.getNextActionDate())) {
            company.setActionDate(JsonNullableUtils.unwrap(request.getNextActionDate()));
        }
        if (JsonNullableUtils.isDefined(request.getRegistrationNumber())) {
            company.setRegistrationNumber(JsonNullableUtils.unwrap(request.getRegistrationNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getSiret())) {
            company.setRegistrationNumber(JsonNullableUtils.unwrap(request.getSiret()));
        }
        if (JsonNullableUtils.isDefined(request.getVatNumber())) {
            company.setVatNumber(JsonNullableUtils.unwrap(request.getVatNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getWebsiteUrl())) {
            company.setWebsiteUrl(JsonNullableUtils.unwrap(request.getWebsiteUrl()));
        }
        if (JsonNullableUtils.isDefined(request.getWebsite())) {
            company.setWebsiteUrl(JsonNullableUtils.unwrap(request.getWebsite()));
        }
        if (JsonNullableUtils.isDefined(request.getEmailAddress())) {
            company.setEmailAddress(JsonNullableUtils.unwrap(request.getEmailAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getEmail())) {
            company.setEmailAddress(JsonNullableUtils.unwrap(request.getEmail()));
        }
        if (JsonNullableUtils.isDefined(request.getPhoneNumber())) {
            company.setPhoneNumber(JsonNullableUtils.unwrap(request.getPhoneNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getPhone())) {
            company.setPhoneNumber(JsonNullableUtils.unwrap(request.getPhone()));
        }
        if (JsonNullableUtils.isDefined(request.getBillingAddress())) {
            company.setBillingAddress(JsonNullableUtils.unwrap(request.getBillingAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getAddress())) {
            company.setBillingAddress(JsonNullableUtils.unwrap(request.getAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getCityName())) {
            company.setCityName(JsonNullableUtils.unwrap(request.getCityName()));
        }
        if (JsonNullableUtils.isDefined(request.getCity())) {
            company.setCityName(JsonNullableUtils.unwrap(request.getCity()));
        }
        if (JsonNullableUtils.isDefined(request.getPostalCode())) {
            company.setPostalCode(JsonNullableUtils.unwrap(request.getPostalCode()));
        }
        if (JsonNullableUtils.isDefined(request.getCountryCode())) {
            company.setCountryCode(JsonNullableUtils.unwrap(request.getCountryCode()));
        }
        if (JsonNullableUtils.isDefined(request.getCountry())) {
            company.setCountryCode(JsonNullableUtils.unwrap(request.getCountry()));
        }

        entityValidationService.validate(company);
        return toResponse(companyRepository.save(company));
    }

    public void delete(Long id) {
        companyRepository.delete(getCompany(id));
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    private CompanyResponse toResponse(Company company) {
        Contact primaryContact = contactRepository.findFirstByCompanyIdAndPrimaryContactTrueOrderByIdAsc(company.getId())
                .orElse(null);
        String primaryContactName = primaryContact == null
                ? null
                : "%s %s".formatted(primaryContact.getFirstName(), primaryContact.getLastName());
        String primaryContactRole = primaryContact == null ? null : primaryContact.getJobTitle();
        List<String> tags = new ArrayList<>();
        if (company.getSector() != null && !company.getSector().isBlank()) {
            tags.add(company.getSector());
        }
        if (company.getAgency() != null && !company.getAgency().isBlank()) {
            tags.add(company.getAgency());
        }

        return new CompanyResponse(
                company.getId(),
                company.getLegalName(),
                company.getDisplayName(),
                company.getDisplayName(),
                company.getRelationType(),
                company.getStatus(),
                company.getSector(),
                company.getManagerName(),
                company.getManagerName(),
                company.getAgency(),
                company.getCurrentAction(),
                company.getCurrentAction(),
                company.getActionDate(),
                company.getActionDate(),
                primaryContactName,
                primaryContactName,
                primaryContactName,
                primaryContactRole,
                primaryContactRole,
                Math.toIntExact(contactRepository.countByCompanyId(company.getId())),
                Math.toIntExact(needRepository.countByCompanyId(company.getId())),
                Math.toIntExact(projectRepository.countByCompanyId(company.getId())),
                tags,
                company.getRegistrationNumber(),
                company.getRegistrationNumber(),
                company.getVatNumber(),
                company.getWebsiteUrl(),
                company.getWebsiteUrl(),
                company.getEmailAddress(),
                company.getEmailAddress(),
                company.getPhoneNumber(),
                company.getPhoneNumber(),
                company.getBillingAddress(),
                company.getBillingAddress(),
                company.getCityName(),
                company.getCityName(),
                company.getPostalCode(),
                company.getCountryCode(),
                company.getCountryCode(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
