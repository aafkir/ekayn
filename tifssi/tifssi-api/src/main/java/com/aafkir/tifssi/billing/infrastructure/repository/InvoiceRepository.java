package com.aafkir.tifssi.billing.infrastructure.repository;

import com.aafkir.tifssi.billing.domain.model.Invoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAllByProjectIdOrderByIssueDateAscIdAsc(Long projectId);
}
