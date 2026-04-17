package com.aafkir.tifssi.crm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.CompanyPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.mapper.CompanyApiMapper;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyApiMapper companyApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private CompanyService companyService;

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setLegalName("Acme Conseil");
        company.setDisplayName("Acme");
        company.setWebsiteUrl("https://acme.example");
        company.setCityName("Paris");
        company.setCountryCode("FR");
    }

    @Test
    void createShouldValidateAndPersistMappedCompany() {
        CompanyCreateRequest request = new CompanyCreateRequest(
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
        );
        Company mappedCompany = new Company();
        mappedCompany.setLegalName(request.legalName());
        mappedCompany.setDisplayName(request.displayName());
        mappedCompany.setWebsiteUrl(request.websiteUrl());

        when(companyApiMapper.toEntity(request)).thenReturn(mappedCompany);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company savedCompany = invocation.getArgument(0);
            savedCompany.setId(42L);
            return savedCompany;
        });
        when(companyApiMapper.toResponse(any(Company.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CompanyResponse response = companyService.create(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.legalName()).isEqualTo("Acme Conseil");
        assertThat(response.displayName()).isEqualTo("Acme");
        verify(entityValidationService).validate(mappedCompany);
        verify(companyRepository).save(mappedCompany);
    }

    @Test
    void patchShouldUpdateDefinedFieldsOnly() {
        CompanyPatchRequest request = new CompanyPatchRequest();
        request.setDisplayName(JsonNullable.of("Acme Groupe"));
        request.setWebsiteUrl(JsonNullable.of("https://group.acme.example"));
        request.setCityName(JsonNullable.of("Lyon"));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyApiMapper.toResponse(any(Company.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CompanyResponse response = companyService.patch(1L, request);

        assertThat(response.legalName()).isEqualTo("Acme Conseil");
        assertThat(response.displayName()).isEqualTo("Acme Groupe");
        assertThat(response.websiteUrl()).isEqualTo("https://group.acme.example");
        assertThat(response.cityName()).isEqualTo("Lyon");
        verify(entityValidationService).validate(company);
    }

    @Test
    void getCompanyShouldThrowWhenCompanyDoesNotExist() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompany(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company with id 99 was not found.");
    }

    private CompanyResponse toResponse(Company target) {
        return new CompanyResponse(
                target.getId(),
                target.getLegalName(),
                target.getDisplayName(),
                target.getRegistrationNumber(),
                target.getVatNumber(),
                target.getWebsiteUrl(),
                target.getEmailAddress(),
                target.getPhoneNumber(),
                target.getBillingAddress(),
                target.getCityName(),
                target.getPostalCode(),
                target.getCountryCode(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
