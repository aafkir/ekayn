package com.aafkir.tifssi.expenses.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.expenses.api.dto.request.ExpenseReportRejectRequest;
import com.aafkir.tifssi.expenses.api.mapper.ExpenseReportApiMapper;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseReportRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseReportServiceTest {
    @Mock ExpenseReportRepository repository;
    @Mock ExpenseReportApiMapper mapper;
    @Mock ProfileRepository profiles;
    @InjectMocks ExpenseReportService service;

    @Test
    void rejectRequiresReasonAndOnlySubmittedCanTransition() {
        ExpenseReport report = new ExpenseReport();
        report.setStatus(ExpenseReportStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(report));
        assertThatThrownBy(() -> service.reject(1L, new ExpenseReportRejectRequest(" ", 4L)))
                .isInstanceOf(IllegalArgumentException.class);

        report.setStatus(ExpenseReportStatus.DRAFT);
        assertThatThrownBy(() -> service.validate(1L, 4L))
                .isInstanceOf(com.aafkir.tifssi.expenses.application.exception.ExpenseReportStateException.class);
    }
}
