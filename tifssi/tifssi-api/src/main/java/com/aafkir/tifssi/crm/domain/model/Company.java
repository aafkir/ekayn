package com.aafkir.tifssi.crm.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "company", schema = "crm")
public class Company extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(name = "legal_name", nullable = false, length = 150)
    private String legalName;

    @Size(max = 150)
    @Column(name = "display_name", length = 150)
    private String displayName;

    @Size(max = 30)
    @Column(name = "relation_type", length = 30)
    private String relationType;

    @Size(max = 30)
    @Column(name = "company_status", length = 30)
    private String status;

    @Size(max = 100)
    @Column(name = "sector", length = 100)
    private String sector;

    @Size(max = 150)
    @Column(name = "manager_name", length = 150)
    private String managerName;

    @Size(max = 100)
    @Column(name = "agency", length = 100)
    private String agency;

    @Size(max = 150)
    @Column(name = "current_action", length = 150)
    private String currentAction;

    @Column(name = "action_date")
    private LocalDate actionDate;

    @Size(max = 50)
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Size(max = 50)
    @Column(name = "vat_number", length = 50)
    private String vatNumber;

    @Size(max = 255)
    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Email
    @Size(max = 150)
    @Column(name = "email_address", length = 150)
    private String emailAddress;

    @Size(max = 50)
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Size(max = 100)
    @Column(name = "city_name", length = 100)
    private String cityName;

    @Size(max = 20)
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Size(max = 2)
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
}
