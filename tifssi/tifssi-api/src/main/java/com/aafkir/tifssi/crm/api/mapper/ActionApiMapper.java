package com.aafkir.tifssi.crm.api.mapper;

import com.aafkir.tifssi.crm.api.dto.request.ActionCreateRequest;
import com.aafkir.tifssi.crm.api.dto.response.ActionResponse;
import com.aafkir.tifssi.crm.domain.model.Action;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ActionApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "contact", ignore = true)
    Action toEntity(ActionCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "contactId", source = "contact.id")
    ActionResponse toResponse(Action action);
}
