package com.aafkir.tifssi.crm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.api.mapper.ContactApiMapper;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
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
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private ContactApiMapper contactApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private ContactService contactService;

    private Company company;
    private Contact contact;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        contact = new Contact();
        contact.setId(1L);
        contact.setCompany(company);
        contact.setFirstName("Lea");
        contact.setLastName("Martin");
        contact.setJobTitle("Directrice achats");
        contact.setEmailAddress("lea.martin@acme.example");
        contact.setPhoneNumber("+33601020304");
        contact.setPrimaryContact(true);
    }

    @Test
    void createShouldAttachCompanyAndPersistContact() {
        ContactCreateRequest request = new ContactCreateRequest(
                10L,
                "Lea",
                "Martin",
                "Directrice achats",
                "lea.martin@acme.example",
                "+33601020304",
                true
        );
        Contact mappedContact = new Contact();
        mappedContact.setFirstName(request.firstName());
        mappedContact.setLastName(request.lastName());
        mappedContact.setPrimaryContact(request.primaryContact());

        when(contactApiMapper.toEntity(request)).thenReturn(mappedContact);
        when(companyService.getCompany(10L)).thenReturn(company);
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            savedContact.setId(42L);
            return savedContact;
        });
        when(contactApiMapper.toResponse(any(Contact.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ContactResponse response = contactService.create(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.companyId()).isEqualTo(10L);
        assertThat(response.primaryContact()).isTrue();
        verify(entityValidationService).validate(mappedContact);
    }

    @Test
    void patchShouldUpdateCompanyAndMutableFields() {
        Company anotherCompany = new Company();
        anotherCompany.setId(11L);
        anotherCompany.setLegalName("Globex Industrie");

        ContactPatchRequest request = new ContactPatchRequest();
        request.setCompanyId(JsonNullable.of(11L));
        request.setJobTitle(JsonNullable.of("Directrice des achats groupe"));
        request.setPhoneNumber(JsonNullable.of("+33605060708"));
        request.setPrimaryContact(JsonNullable.of(false));

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(companyService.getCompany(11L)).thenReturn(anotherCompany);
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contactApiMapper.toResponse(any(Contact.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ContactResponse response = contactService.patch(1L, request);

        assertThat(response.companyId()).isEqualTo(11L);
        assertThat(response.jobTitle()).isEqualTo("Directrice des achats groupe");
        assertThat(response.phoneNumber()).isEqualTo("+33605060708");
        assertThat(response.primaryContact()).isFalse();
        verify(entityValidationService).validate(contact);
    }

    @Test
    void patchShouldRejectNullPrimaryContact() {
        ContactPatchRequest request = new ContactPatchRequest();
        request.setPrimaryContact(JsonNullable.of(null));

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        assertThatThrownBy(() -> contactService.patch(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("primaryContact cannot be null.");

        verify(contactRepository, never()).save(any(Contact.class));
    }

    private ContactResponse toResponse(Contact target) {
        return new ContactResponse(
                target.getId(),
                target.getCompany() == null ? null : target.getCompany().getId(),
                target.getFirstName(),
                target.getLastName(),
                target.getJobTitle(),
                target.getEmailAddress(),
                target.getPhoneNumber(),
                target.isPrimaryContact(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
