package com.aafkir.tifssi.timesheets.api.mapper;

import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryCreateRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntryResponse;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TimeEntryApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mission", ignore = true)
    @Mapping(target = "profile", ignore = true)
    TimeEntry toEntity(TimeEntryCreateRequest request);

    @Mapping(target = "missionId", source = "mission.id")
    @Mapping(target = "profileId", source = "profile.id")
    TimeEntryResponse toResponse(TimeEntry timeEntry);
}
