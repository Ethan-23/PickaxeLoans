package io.github.ethan23.pickaxeLoans.model;

public class LoanDeal {

    private CostType costType;
    private double upfrontCost;
    private int xpTaxPercent;
    private int energyTaxPercent;
    private long loanDurationMillis;

    public LoanDeal(CostType costType, double upfrontCost, int xpTaxPercent, int energyTaxPercent, long loanDurationMillis) {
        this.costType = costType;
        this.upfrontCost = upfrontCost;
        this.xpTaxPercent = xpTaxPercent;
        this.energyTaxPercent = energyTaxPercent;
        this.loanDurationMillis = loanDurationMillis;
    }

    public LoanDeal() {
        this.costType = CostType.MONEY;
        this.upfrontCost = 1_000.0;
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

    public double getUpfrontCost() {
        return upfrontCost;
    }

    public void setUpfrontCost(double upfrontCost) {
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
