package com.aafkir.tifssi.staffing.api.mapper;

import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.domain.model.Need;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface NeedApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "needSkills", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "project", ignore = true)
    Need toEntity(NeedCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "contactId", source = "contact.id")
    @Mapping(target = "projectId", source = "project.id")
    NeedResponse toResponse(Need need);
}
