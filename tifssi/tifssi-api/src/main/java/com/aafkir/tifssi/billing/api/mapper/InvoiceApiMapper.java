package com.aafkir.tifssi.billing.api.mapper;

import com.aafkir.tifssi.billing.api.dto.request.InvoiceCreateRequest;
import com.aafkir.tifssi.billing.api.dto.request.InvoiceLineCreateRequest;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceLineResponse;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceSummaryResponse;
import com.aafkir.tifssi.billing.domain.model.Invoice;
import com.aafkir.tifssi.billing.domain.model.InvoiceLine;
import com.aafkir.tifssi.shared.infrastructure.config.CentralMapperConfig;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = CentralMapperConfig.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface InvoiceApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "totalHt", ignore = true)
    @Mapping(target = "totalVat", ignore = true)
    @Mapping(target = "totalTtc", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "invoiceLines", ignore = true)
    Invoice toEntity(InvoiceCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "totalHt", ignore = true)
    @Mapping(target = "totalVat", ignore = true)
    @Mapping(target = "totalTtc", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    InvoiceLine toEntity(InvoiceLineCreateRequest request);

    @Mapping(target = "projectId", source = "project.id")
    InvoiceSummaryResponse toSummaryResponse(Invoice invoice);

    @Mapping(target = "invoiceId", source = "invoice.id")
    InvoiceLineResponse toLineResponse(InvoiceLine invoiceLine);
}
