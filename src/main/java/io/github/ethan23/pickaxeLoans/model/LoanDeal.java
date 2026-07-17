package io.github.ethan23.pickaxeLoans.model;

public class LoanDeal {

    private CostType costType;
    private long upfrontCost;
    private int xpTaxPercent;
    private int energyTaxPercent;
    private long loanDurationMillis;

    public LoanDeal() {
        this.costType = CostType.MONEY;
        this.upfrontCost = 1_000;
        this.xpTaxPercent = 0;
        this.energyTaxPercent = 0;
        this.loanDurationMillis = 30 * 60 * 1000;
    }

    public CostType getCostType() {
        return costType;
    }

    public void setCostType(CostType costType) {
        this.costType = costType;
    }

    public long getUpfrontCost() {
        return upfrontCost;
    }

    public void setUpfrontCost(long upfrontCost) {
        this.upfrontCost = upfrontCost;
    }

    public int getXpTaxPercent() {
        return xpTaxPercent;
    }

    public void setXpTaxPercent(int xpTaxPercent) {
        this.xpTaxPercent = xpTaxPercent;
    }

    public int getEnergyTaxPercent() {
        return energyTaxPercent;
    }

    public void setEnergyTaxPercent(int energyTaxPercent) {
        this.energyTaxPercent = energyTaxPercent;
    }

    public long getLoanDurationMillis() {
        return loanDurationMillis;
    }

    public void setLoanDurationMillis(long loanDurationMillis) {
        this.loanDurationMillis = loanDurationMillis;
    }
}
