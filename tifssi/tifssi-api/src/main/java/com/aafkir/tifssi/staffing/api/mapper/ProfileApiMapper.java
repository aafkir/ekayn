package com.aafkir.tifssi.staffing.api.mapper;

import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProfileApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileSkills", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    Profile toEntity(ProfileCreateRequest request);

    ProfileResponse toResponse(Profile profile);
}
