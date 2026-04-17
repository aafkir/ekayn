package com.aafkir.tifssi.projects.application.service;

import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.MissionPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.MissionResponse;
import com.aafkir.tifssi.projects.api.mapper.MissionApiMapper;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final ProjectService projectService;
    private final ProfileService profileService;
    private final MissionApiMapper missionApiMapper;
    private final EntityValidationService entityValidationService;

    public MissionService(
            MissionRepository missionRepository,
            ProjectService projectService,
            ProfileService profileService,
            MissionApiMapper missionApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.missionRepository = missionRepository;
        this.projectService = projectService;
        this.profileService = profileService;
        this.missionApiMapper = missionApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> findAll() {
        return missionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(missionApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse findById(Long id) {
        return missionApiMapper.toResponse(getMission(id));
    }

    public MissionResponse create(MissionCreateRequest request) {
        Mission mission = missionApiMapper.toEntity(request);
        mission.setProject(projectService.getProject(request.projectId()));
        mission.setProfile(profileService.getProfile(request.profileId()));
        validateMissionDates(mission);
        entityValidationService.validate(mission);
        return missionApiMapper.toResponse(missionRepository.save(mission));
    }

    public MissionResponse patch(Long id, MissionPatchRequest request) {
        Mission mission = getMission(id);

        if (JsonNullableUtils.isDefined(request.getProjectId())) {
            Long projectId = JsonNullableUtils.unwrap(request.getProjectId());
            Project project = projectId == null ? null : projectService.getProject(projectId);
            mission.setProject(project);
        }
        if (JsonNullableUtils.isDefined(request.getProfileId())) {
            Long profileId = JsonNullableUtils.unwrap(request.getProfileId());
            Profile profile = profileId == null ? null : profileService.getProfile(profileId);
            mission.setProfile(profile);
        }
        if (JsonNullableUtils.isDefined(request.getRoleName())) {
            mission.setRoleName(JsonNullableUtils.unwrap(request.getRoleName()));
        }
        if (JsonNullableUtils.isDefined(request.getStartDate())) {
            mission.setStartDate(JsonNullableUtils.unwrap(request.getStartDate()));
        }
        if (JsonNullableUtils.isDefined(request.getEndDate())) {
            mission.setEndDate(JsonNullableUtils.unwrap(request.getEndDate()));
        }
        if (JsonNullableUtils.isDefined(request.getDailyRate())) {
            mission.setDailyRate(JsonNullableUtils.unwrap(request.getDailyRate()));
        }
        if (JsonNullableUtils.isDefined(request.getAllocationPercent())) {
            mission.setAllocationPercent(JsonNullableUtils.unwrap(request.getAllocationPercent()));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            mission.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }

        validateMissionDates(mission);
        entityValidationService.validate(mission);
        return missionApiMapper.toResponse(missionRepository.save(mission));
    }

    public void delete(Long id) {
        missionRepository.delete(getMission(id));
    }

    public Mission getMission(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission", id));
    }

    private void validateMissionDates(Mission mission) {
        if (mission.getStartDate() != null
                && mission.getEndDate() != null
                && mission.getEndDate().isBefore(mission.getStartDate())) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }
    }
}
