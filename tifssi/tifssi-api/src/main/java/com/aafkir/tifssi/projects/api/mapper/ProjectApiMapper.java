package com.aafkir.tifssi.projects.api.mapper;

import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProjectApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "originNeed", ignore = true)
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "missions", ignore = true)
    Project toEntity(ProjectCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "originNeedId", source = "originNeed.id")
    @Mapping(target = "contactId", source = "contact.id")
    ProjectResponse toResponse(Project project);
}
