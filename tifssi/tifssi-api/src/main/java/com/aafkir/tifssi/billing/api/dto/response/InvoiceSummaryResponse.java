package com.aafkir.tifssi.billing.api.dto.response;

import com.aafkir.tifssi.billing.domain.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(
        description = "Representation synthetique d'une facture.",
        example = """
                {
                  "id": 1,
                  "projectId": 1,
                  "invoiceNumber": "INV-2026-001",
                  "issueDate": "2026-07-01",
                  "dueDate": "2026-07-31",
                  "status": "DRAFT",
                  "totalHt": 2750.00,
                  "totalVat": 550.00,
                  "totalTtc": 3300.00,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T10:00:00Z"
                }
                """
)
public record InvoiceSummaryResponse(
        Long id,
        Long projectId,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        BigDecimal totalHt,
        BigDecimal totalVat,
        BigDecimal totalTtc,
        Instant createdAt,
        Instant updatedAt
) {
}
