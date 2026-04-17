package com.aafkir.tifssi.absences.api.mapper;

import com.aafkir.tifssi.absences.api.dto.request.AbsenceCreateRequest;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceResponse;
import com.aafkir.tifssi.absences.domain.model.Absence;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AbsenceApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    Absence toEntity(AbsenceCreateRequest request);

    @Mapping(target = "profileId", source = "profile.id")
    AbsenceResponse toResponse(Absence absence);
}
