package com.finance_control.brazilian_market.model;

/**
 * Investment subtypes enum.
 * Extracted from Investment.java to reduce class fan-out complexity.
 */
public enum InvestmentSubtype {
    // Stock subtypes
    ORDINARY("Ordinary Share"),
    PREFERRED("Preferred Share"),
    UNIT("Unit"),

    // FII subtypes
    TIJOLO("Brick (Real Estate)"),
    PAPEL("Paper (Securities)"),
    HIBRIDO("Hybrid"),
    FUNDO_DE_FUNDOS("Fund of Funds"),

    // Bond subtypes
    CDB("Certificate of Bank Deposit"),
    RDB("Bank Deposit Receipt"),
    LCI("Real Estate Credit Bill"),
    LCA("Agribusiness Credit Bill"),
    LF("Financial Letter"),
    DEBENTURE("Debenture"),
    TESOURO_DIRETO("Treasury Direct"),

    // Generic
    OTHER("Other");

    private final String description;

    InvestmentSubtype(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

