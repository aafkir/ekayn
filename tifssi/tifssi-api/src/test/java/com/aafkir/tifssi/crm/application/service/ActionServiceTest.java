package com.aafkir.tifssi.crm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private ActionRepository actionRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ActionApiMapper actionApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private ActionService actionService;

    private Company company;
    private Contact contact;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        contact = new Contact();
        contact.setId(20L);
        contact.setCompany(company);
        contact.setFirstName("Lea");
        contact.setLastName("Martin");
        contact.setPrimaryContact(true);
    }

    @Test
    void createShouldResolveRelationsAndPersistAction() {
        ActionCreateRequest request = new ActionCreateRequest(
                10L,
                20L,
                ActionType.CALL,
                "Relance apres demo",
                "Valider la prochaine etape commerciale.",
                LocalDate.of(2026, 6, 12),
                ActionStatus.TODO,
                "Nadia Mercier",
                "Planifier un atelier"
        );
        Action mappedAction = new Action();
        mappedAction.setType(request.type());
        mappedAction.setSubject(request.subject());
        mappedAction.setComment(request.comment());
        mappedAction.setDueDate(request.dueDate());
        mappedAction.setStatus(request.status());
        mappedAction.setResponsibleName(request.responsibleName());
        mappedAction.setNextStep(request.nextStep());

        when(actionApiMapper.toEntity(request)).thenReturn(mappedAction);
        when(companyService.getCompany(10L)).thenReturn(company);
        when(contactRepository.findById(20L)).thenReturn(Optional.of(contact));
        when(actionRepository.save(any(Action.class))).thenAnswer(invocation -> {
            Action savedAction = invocation.getArgument(0);
            savedAction.setId(42L);
            return savedAction;
        });
        when(actionApiMapper.toResponse(any(Action.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ActionResponse response = actionService.create(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.companyId()).isEqualTo(10L);
        assertThat(response.contactId()).isEqualTo(20L);
        assertThat(response.type()).isEqualTo(ActionType.CALL);
        verify(entityValidationService).validate(mappedAction);
    }

    @Test
    void patchShouldRejectContactFromAnotherCompany() {
        Company anotherCompany = new Company();
        anotherCompany.setId(11L);
        anotherCompany.setLegalName("Globex Industrie");

        Contact anotherContact = new Contact();
        anotherContact.setId(21L);
        anotherContact.setCompany(anotherCompany);
        anotherContact.setFirstName("Karim");
        anotherContact.setLastName("Benali");

        Action action = new Action();
        action.setId(100L);
        action.setCompany(company);
        action.setContact(contact);
        action.setType(ActionType.MEETING);
        action.setSubject("Point projet");
        action.setStatus(ActionStatus.TODO);

        ActionPatchRequest request = new ActionPatchRequest();
        request.setContactId(JsonNullable.of(21L));

        when(actionRepository.findById(100L)).thenReturn(Optional.of(action));
        when(contactRepository.findById(21L)).thenReturn(Optional.of(anotherContact));

        assertThatThrownBy(() -> actionService.patch(100L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Contact must belong to the same company as the action.");

        verify(actionRepository, never()).save(any(Action.class));
    }

    private ActionResponse toResponse(Action target) {
        return new ActionResponse(
                target.getId(),
                target.getCompany() == null ? null : target.getCompany().getId(),
                target.getContact() == null ? null : target.getContact().getId(),
                target.getType(),
                target.getSubject(),
                target.getComment(),
                target.getDueDate(),
                target.getStatus(),
                target.getResponsibleName(),
                target.getNextStep(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
