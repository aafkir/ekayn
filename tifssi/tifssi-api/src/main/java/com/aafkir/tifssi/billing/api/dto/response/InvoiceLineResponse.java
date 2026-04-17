package com.aafkir.tifssi.billing.api.dto.response;

import com.aafkir.tifssi.billing.domain.enums.InvoiceLineSourceType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(
        description = "Representation detaillee d'une ligne de facture.",
        example = """
                {
                  "id": 10,
                  "invoiceId": 1,
                  "lineType": "TIME",
                  "description": "Mission Developer - 2.00 DAY",
                  "quantity": 2.00,
                  "unit": "DAY",
                  "unitPrice": 800.00,
                  "vatRate": 20.00,
                  "totalHt": 1600.00,
                  "totalVat": 320.00,
                  "totalTtc": 1920.00,
                  "sourceType": "TIME_ENTRY",
                  "sourceId": 5,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record InvoiceLineResponse(
        Long id,
        Long invoiceId,
        InvoiceLineType lineType,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal vatRate,
        BigDecimal totalHt,
        BigDecimal totalVat,
        BigDecimal totalTtc,
        InvoiceLineSourceType sourceType,
        Long sourceId,
        Instant createdAt,
        Instant updatedAt
) {
}
