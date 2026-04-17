package com.aafkir.tifssi.crm.application.service;

import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.api.mapper.ContactApiMapper;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyService companyService;
    private final ContactApiMapper contactApiMapper;
    private final EntityValidationService entityValidationService;

    public ContactService(
            ContactRepository contactRepository,
            CompanyService companyService,
            ContactApiMapper contactApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.contactRepository = contactRepository;
        this.companyService = companyService;
        this.contactApiMapper = contactApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findAll() {
        return contactRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(contactApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) {
        return contactApiMapper.toResponse(getContact(id));
    }

    public ContactResponse create(ContactCreateRequest request) {
        Contact contact = contactApiMapper.toEntity(request);
        contact.setCompany(companyService.getCompany(request.companyId()));
        entityValidationService.validate(contact);
        return contactApiMapper.toResponse(contactRepository.save(contact));
    }

    public ContactResponse patch(Long id, ContactPatchRequest request) {
        Contact contact = getContact(id);

        if (JsonNullableUtils.isDefined(request.getCompanyId())) {
            Long companyId = JsonNullableUtils.unwrap(request.getCompanyId());
            Company company = companyId == null ? null : companyService.getCompany(companyId);
            contact.setCompany(company);
        }
        if (JsonNullableUtils.isDefined(request.getFirstName())) {
            contact.setFirstName(JsonNullableUtils.unwrap(request.getFirstName()));
        }
        if (JsonNullableUtils.isDefined(request.getLastName())) {
            contact.setLastName(JsonNullableUtils.unwrap(request.getLastName()));
        }
        if (JsonNullableUtils.isDefined(request.getJobTitle())) {
            contact.setJobTitle(JsonNullableUtils.unwrap(request.getJobTitle()));
        }
        if (JsonNullableUtils.isDefined(request.getEmailAddress())) {
            contact.setEmailAddress(JsonNullableUtils.unwrap(request.getEmailAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getPhoneNumber())) {
            contact.setPhoneNumber(JsonNullableUtils.unwrap(request.getPhoneNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getPrimaryContact())) {
            Boolean primaryContact = JsonNullableUtils.unwrap(request.getPrimaryContact());
            if (primaryContact == null) {
                throw new IllegalArgumentException("primaryContact cannot be null.");
            }
            contact.setPrimaryContact(primaryContact);
        }

        entityValidationService.validate(contact);
        return contactApiMapper.toResponse(contactRepository.save(contact));
    }

    public void delete(Long id) {
        contactRepository.delete(getContact(id));
    }

    private Contact getContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
    }
}
