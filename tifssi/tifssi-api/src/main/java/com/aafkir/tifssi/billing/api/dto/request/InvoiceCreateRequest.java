package com.aafkir.tifssi.billing.api.dto.request;

import com.aafkir.tifssi.billing.domain.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une facture pour un projet.",
        example = """
                {
                  "invoiceNumber": "INV-2026-001",
                  "issueDate": "2026-07-01",
                  "dueDate": "2026-07-31",
                  "status": "DRAFT"
                }
                """
)
public record InvoiceCreateRequest(
        @NotBlank @Size(max = 50) String invoiceNumber,
        @NotNull LocalDate issueDate,
        LocalDate dueDate,
        @NotNull InvoiceStatus status
) {
}
