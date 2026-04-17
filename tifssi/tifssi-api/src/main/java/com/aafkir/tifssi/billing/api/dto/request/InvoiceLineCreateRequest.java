package com.aafkir.tifssi.billing.api.dto.request;

import com.aafkir.tifssi.billing.domain.enums.InvoiceLineType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(
        description = "Payload d'ajout d'une ligne manuelle a une facture.",
        example = """
                {
                  "lineType": "FIXED_FEE",
                  "description": "Setup package",
                  "quantity": 1.00,
                  "unit": "PACKAGE",
                  "unitPrice": 1000.00,
                  "vatRate": 20.00
                }
                """
)
public record InvoiceLineCreateRequest(
        @NotNull InvoiceLineType lineType,
        @NotBlank @Size(max = 255) String description,
        @NotNull @Positive BigDecimal quantity,
        @NotBlank @Size(max = 30) String unit,
        @NotNull BigDecimal unitPrice,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal vatRate
) {
}
