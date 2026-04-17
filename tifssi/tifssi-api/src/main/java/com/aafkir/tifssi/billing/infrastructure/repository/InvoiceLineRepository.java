package com.aafkir.tifssi.billing.infrastructure.repository;

import com.aafkir.tifssi.billing.domain.model.InvoiceLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {

    List<InvoiceLine> findAllByInvoiceIdOrderByDisplayOrderAscIdAsc(Long invoiceId);
}
