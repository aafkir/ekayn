package com.aafkir.tifssi.crm.application.service;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.CompanyPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.mapper.CompanyApiMapper;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyApiMapper companyApiMapper;
    private final EntityValidationService entityValidationService;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyApiMapper companyApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.companyRepository = companyRepository;
        this.companyApiMapper = companyApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(companyApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return companyApiMapper.toResponse(getCompany(id));
    }

    public CompanyResponse create(CompanyCreateRequest request) {
        Company company = companyApiMapper.toEntity(request);
        entityValidationService.validate(company);
        return companyApiMapper.toResponse(companyRepository.save(company));
    }

    public CompanyResponse patch(Long id, CompanyPatchRequest request) {
        Company company = getCompany(id);

        if (JsonNullableUtils.isDefined(request.getLegalName())) {
            company.setLegalName(JsonNullableUtils.unwrap(request.getLegalName()));
        }
        if (JsonNullableUtils.isDefined(request.getDisplayName())) {
            company.setDisplayName(JsonNullableUtils.unwrap(request.getDisplayName()));
        }
        if (JsonNullableUtils.isDefined(request.getRegistrationNumber())) {
            company.setRegistrationNumber(JsonNullableUtils.unwrap(request.getRegistrationNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getVatNumber())) {
            company.setVatNumber(JsonNullableUtils.unwrap(request.getVatNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getWebsiteUrl())) {
            company.setWebsiteUrl(JsonNullableUtils.unwrap(request.getWebsiteUrl()));
        }
        if (JsonNullableUtils.isDefined(request.getEmailAddress())) {
            company.setEmailAddress(JsonNullableUtils.unwrap(request.getEmailAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getPhoneNumber())) {
            company.setPhoneNumber(JsonNullableUtils.unwrap(request.getPhoneNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getBillingAddress())) {
            company.setBillingAddress(JsonNullableUtils.unwrap(request.getBillingAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getCityName())) {
            company.setCityName(JsonNullableUtils.unwrap(request.getCityName()));
        }
        if (JsonNullableUtils.isDefined(request.getPostalCode())) {
            company.setPostalCode(JsonNullableUtils.unwrap(request.getPostalCode()));
        }
        if (JsonNullableUtils.isDefined(request.getCountryCode())) {
            company.setCountryCode(JsonNullableUtils.unwrap(request.getCountryCode()));
        }

        entityValidationService.validate(company);
        return companyApiMapper.toResponse(companyRepository.save(company));
    }

    public void delete(Long id) {
        companyRepository.delete(getCompany(id));
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }
}

