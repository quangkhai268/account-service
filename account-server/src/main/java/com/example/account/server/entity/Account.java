package com.example.account.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @Column(name = "ACCT_NO", length = 12, nullable = false)
    private String acctNo;

    @Column(name = "CIF_NO", length = 20)
    private String cifNo;

    @Column(name = "ID_NO", length = 40)
    private String idNo;

    @Column(name = "ACCT_TYPE", length = 4)
    private String acctType;

    @Column(name = "ACCT_TITLE", length = 255)
    private String acctTitle;

    @Column(name = "CURR_CODE", length = 3)
    private String currCode;

    @Column(name = "BRANCH_CODE", length = 6)
    private String branchCode;

    @Column(name = "LEDGER_BALANCE", precision = 19, scale = 2)
    private BigDecimal ledgerBalance;

    @Column(name = "MIN_BALANCE", precision = 19, scale = 2)
    private BigDecimal minBalance;

    @Column(name = "ACC_STATUS", length = 1)
    private String accStatus;

    public String getAcctNo() { return acctNo; }
    public void setAcctNo(String acctNo) { this.acctNo = acctNo; }
    public String getCifNo() { return cifNo; }
    public void setCifNo(String cifNo) { this.cifNo = cifNo; }
    public String getIdNo() { return idNo; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public String getAcctType() { return acctType; }
    public void setAcctType(String acctType) { this.acctType = acctType; }
    public String getAcctTitle() { return acctTitle; }
    public void setAcctTitle(String acctTitle) { this.acctTitle = acctTitle; }
    public String getCurrCode() { return currCode; }
    public void setCurrCode(String currCode) { this.currCode = currCode; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public BigDecimal getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(BigDecimal ledgerBalance) { this.ledgerBalance = ledgerBalance; }
    public BigDecimal getMinBalance() { return minBalance; }
    public void setMinBalance(BigDecimal minBalance) { this.minBalance = minBalance; }
    public String getAccStatus() { return accStatus; }
    public void setAccStatus(String accStatus) { this.accStatus = accStatus; }
}
