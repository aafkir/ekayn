package com.aafkir.tifssi.billing.domain.model;

import com.aafkir.tifssi.billing.domain.enums.InvoiceLineSourceType;
import com.aafkir.tifssi.billing.domain.enums.InvoiceLineType;
import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice_line", schema = "billing")
public class InvoiceLine extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_line_invoice"))
    private Invoice invoice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 20)
    private InvoiceLineType lineType;

    @NotBlank
    @Size(max = 255)
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotBlank
    @Size(max = 30)
    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @NotNull
    @Column(name = "total_ht", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalHt;

    @NotNull
    @Column(name = "total_vat", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalVat;

    @NotNull
    @Column(name = "total_ttc", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTtc;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private InvoiceLineSourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "display_order")
    private Integer displayOrder;
}
