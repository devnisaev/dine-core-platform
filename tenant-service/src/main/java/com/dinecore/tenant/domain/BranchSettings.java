package com.dinecore.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "branch_settings")
public class BranchSettings {

    @Id
    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "tax_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "service_charge_pct", nullable = false, precision = 8, scale = 4)
    private BigDecimal serviceChargePct;

    @Column(name = "receipt_footer_text", nullable = false, length = 500)
    private String receiptFooterText;

    protected BranchSettings() {
    }

    public BranchSettings(UUID branchId, BigDecimal taxRate, BigDecimal serviceChargePct, String receiptFooterText) {
        this.branchId = branchId;
        this.taxRate = taxRate;
        this.serviceChargePct = serviceChargePct;
        this.receiptFooterText = receiptFooterText;
    }

    public void update(BigDecimal taxRate, BigDecimal serviceChargePct, String receiptFooterText) {
        this.taxRate = taxRate;
        this.serviceChargePct = serviceChargePct;
        this.receiptFooterText = receiptFooterText;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getServiceChargePct() {
        return serviceChargePct;
    }

    public String getReceiptFooterText() {
        return receiptFooterText;
    }
}
