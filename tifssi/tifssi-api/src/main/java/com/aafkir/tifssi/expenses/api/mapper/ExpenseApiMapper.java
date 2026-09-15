package com.aafkir.tifssi.expenses.api.mapper;

import com.aafkir.tifssi.expenses.api.dto.request.ExpenseCreateRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseResponse;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ExpenseApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mission", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "expenseReport", ignore = true)
    Expense toEntity(ExpenseCreateRequest request);

    @Mapping(target = "missionId", source = "mission.id")
    @Mapping(target = "profileId", source = "profile.id")
    ExpenseResponse toResponse(Expense expense);
}
