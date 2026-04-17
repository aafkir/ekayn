package com.aafkir.tifssi.staffing.api.mapper;

import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.SubmissionResponse;
import com.aafkir.tifssi.staffing.domain.model.Submission;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface SubmissionApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "need", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "notes", source = "comment")
    Submission toEntity(SubmissionCreateRequest request);

    @Mapping(target = "needId", source = "need.id")
    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "comment", source = "notes")
    SubmissionResponse toResponse(Submission submission);
}
