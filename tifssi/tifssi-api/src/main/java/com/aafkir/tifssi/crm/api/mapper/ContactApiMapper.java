package com.aafkir.tifssi.crm.api.mapper;

import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ContactApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    Contact toEntity(ContactCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    ContactResponse toResponse(Contact contact);
}
