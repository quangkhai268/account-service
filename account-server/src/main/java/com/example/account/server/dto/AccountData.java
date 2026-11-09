package com.example.account.server.dto;

/** Một phần tử của mảng data trong response. */
public class AccountData {

    private String acctNo;
    private String acctType;
    private String acctTitle;
    private String currCode;
    private String branchCode;
    private String ledgerBalance;
    private String minBalance;
    /** 0 Active, 1 Inactive, 2 Dormant, 3 Escheat, 4 Closed, 5 Close Out in Process. */
    private String accStatus;

    public String getAcctNo() { return acctNo; }
    public void setAcctNo(String acctNo) { this.acctNo = acctNo; }
    public String getAcctType() { return acctType; }
    public void setAcctType(String acctType) { this.acctType = acctType; }
    public String getAcctTitle() { return acctTitle; }
    public void setAcctTitle(String acctTitle) { this.acctTitle = acctTitle; }
    public String getCurrCode() { return currCode; }
    public void setCurrCode(String currCode) { this.currCode = currCode; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(String ledgerBalance) { this.ledgerBalance = ledgerBalance; }
    public String getMinBalance() { return minBalance; }
    public void setMinBalance(String minBalance) { this.minBalance = minBalance; }
    public String getAccStatus() { return accStatus; }
    public void setAccStatus(String accStatus) { this.accStatus = accStatus; }
}
