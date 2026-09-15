package com.aafkir.tifssi.expenses.api.mapper;

import com.aafkir.tifssi.expenses.api.dto.response.ExpenseReportResponse;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import org.mapstruct.*;

@Mapper(config=com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig.class, componentModel="spring", injectionStrategy=InjectionStrategy.CONSTRUCTOR, uses=ExpenseApiMapper.class)
public interface ExpenseReportApiMapper {
    @Mapping(target="profileId", source="profile.id")
    @Mapping(target="totalAmount", expression="java(entity.getExpenses().stream().map(com.aafkir.tifssi.expenses.domain.model.Expense::getCurrency).distinct().count() == 1 ? entity.getExpenses().stream().map(com.aafkir.tifssi.expenses.domain.model.Expense::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add) : null)")
    @Mapping(target="currency", expression="java(entity.getExpenses().stream().map(com.aafkir.tifssi.expenses.domain.model.Expense::getCurrency).distinct().count() == 1 ? entity.getExpenses().get(0).getCurrency() : null)")
    ExpenseReportResponse toResponse(ExpenseReport entity);
}
