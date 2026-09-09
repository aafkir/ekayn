package com.aafkir.tifssi.timesheets.api.mapper;
import com.aafkir.tifssi.timesheets.api.dto.response.TimesheetResponse;
import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import java.util.List;
import org.mapstruct.*;
@Mapper(config=com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig.class, componentModel="spring", injectionStrategy=InjectionStrategy.CONSTRUCTOR, uses=TimeEntryApiMapper.class)
public interface TimesheetApiMapper {
 @Mapping(target="profileId", source="profile.id") TimesheetResponse toResponse(Timesheet entity);
}
