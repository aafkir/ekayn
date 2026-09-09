package com.aafkir.tifssi.crm.application.service;

import com.aafkir.tifssi.crm.api.dto.request.ActionCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ActionPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.ActionResponse;
import com.aafkir.tifssi.crm.api.mapper.ActionApiMapper;
import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import com.aafkir.tifssi.crm.domain.model.Action;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ActionRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActionService {

    private final ActionRepository actionRepository;
    private final CompanyService companyService;
    private final ContactRepository contactRepository;
    private final ActionApiMapper actionApiMapper;
    private final EntityValidationService entityValidationService;

    public ActionService(
            ActionRepository actionRepository,
            CompanyService companyService,
            ContactRepository contactRepository,
            ActionApiMapper actionApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.actionRepository = actionRepository;
        this.companyService = companyService;
        this.contactRepository = contactRepository;
        this.actionApiMapper = actionApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<ActionResponse> findAll(Long companyId, Long contactId, ActionStatus status, ActionType type) {
        Company company = companyId == null ? null : companyService.getCompany(companyId);
        Contact contact = contactId == null ? null : resolveContact(contactId);
        if (company != null && contact != null && !contact.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("Contact must belong to the same company as the action.");
        }

        return actionRepository.findAll(buildSpecification(companyId, contactId, status, type), Sort.by(Sort.Direction.ASC, "dueDate", "id"))
                .stream()
                .map(actionApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActionResponse findById(Long id) {
        return actionApiMapper.toResponse(getAction(id));
    }

    public ActionResponse create(ActionCreateRequest request) {
        Action action = actionApiMapper.toEntity(request);
        action.setCompany(companyService.getCompany(request.companyId()));
        action.setContact(resolveContact(request.contactId()));

        validateActionRelations(action);
        entityValidationService.validate(action);
        return actionApiMapper.toResponse(actionRepository.save(action));
    }

    public ActionResponse patch(Long id, ActionPatchRequest request) {
        Action action = getAction(id);

        if (JsonNullableUtils.isDefined(request.getCompanyId())) {
            Long companyId = JsonNullableUtils.unwrap(request.getCompanyId());
            Company company = companyId == null ? null : companyService.getCompany(companyId);
            action.setCompany(company);
        }
        if (JsonNullableUtils.isDefined(request.getContactId())) {
            action.setContact(resolveContact(JsonNullableUtils.unwrap(request.getContactId())));
        }
        if (JsonNullableUtils.isDefined(request.getType())) {
            action.setType(JsonNullableUtils.unwrap(request.getType()));
        }
        if (JsonNullableUtils.isDefined(request.getSubject())) {
            action.setSubject(JsonNullableUtils.unwrap(request.getSubject()));
        }
        if (JsonNullableUtils.isDefined(request.getComment())) {
            action.setComment(JsonNullableUtils.unwrap(request.getComment()));
        }
        if (JsonNullableUtils.isDefined(request.getDueDate())) {
            action.setDueDate(JsonNullableUtils.unwrap(request.getDueDate()));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            action.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }
        if (JsonNullableUtils.isDefined(request.getResponsibleName())) {
            action.setResponsibleName(JsonNullableUtils.unwrap(request.getResponsibleName()));
        }
        if (JsonNullableUtils.isDefined(request.getNextStep())) {
            action.setNextStep(JsonNullableUtils.unwrap(request.getNextStep()));
        }

        validateActionRelations(action);
        entityValidationService.validate(action);
        return actionApiMapper.toResponse(actionRepository.save(action));
    }

    public void delete(Long id) {
        actionRepository.delete(getAction(id));
    }

    public Action getAction(Long id) {
        return actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action", id));
    }

    private Contact resolveContact(Long contactId) {
        if (contactId == null) {
            return null;
        }
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));
    }

    private void validateActionRelations(Action action) {
        if (action.getCompany() == null) {
            throw new IllegalArgumentException("companyId cannot be null.");
        }
        if (action.getContact() != null
                && !action.getContact().getCompany().getId().equals(action.getCompany().getId())) {
            throw new IllegalArgumentException("Contact must belong to the same company as the action.");
        }
    }

    private Specification<Action> buildSpecification(Long companyId, Long contactId, ActionStatus status, ActionType type) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }
            if (contactId != null) {
                predicates.add(criteriaBuilder.equal(root.get("contact").get("id"), contactId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
