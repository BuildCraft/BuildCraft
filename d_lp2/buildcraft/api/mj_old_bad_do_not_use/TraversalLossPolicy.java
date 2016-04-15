package buildcraft.api._mj;

public final class TraversalLossPolicy {
    public enum LossType {
        NONE,
        PERCENTAGE,
        SUBTRACTIVE,
        MULT_THEN_SUB,
        SUB_THEN_MULT;
    }

    private static final TraversalLossPolicy NO_LOSS = new TraversalLossPolicy(LossType.NONE, 0, 0);

    public static TraversalLossPolicy getNoLossInstance() {
        return NO_LOSS;
    }

    /** Creates a policy that will only let through <code>(1-percentLoss) * in </code> watts. A negative value means you
     * somehow gain power, zero means that you have no loss ({@link #getNoLossInstance()} is a better alternative)
     * 
     * @param percentLoss The percentage of power that is lost going through transporters. */
    public static TraversalLossPolicy createPolicyPercentage(double percentLoss) {
        return new TraversalLossPolicy(LossType.PERCENTAGE, percentLoss, 0);
    }

    /** Creates a policy that will only let through <code>in - valueLoss</code> watts. A negative value means you
     * somehow gain power, zero means there is no loss ({@link #getNoLossInstance()} is a better alternative)
     * 
     * @param valueLoss The value of power that is lost going through transporters. */
    public static TraversalLossPolicy createPolicySubtractive(int valueLoss) {
        return new TraversalLossPolicy(LossType.SUBTRACTIVE, 0, valueLoss);
    }

    public static TraversalLossPolicy createPolicyMultThenAdd(double percentLoss, int valueLoss) {
        return new TraversalLossPolicy(LossType.MULT_THEN_SUB, percentLoss, valueLoss);
    }

    public static TraversalLossPolicy createPolicyAddThenMult(int valueLoss, double percentLoss) {
        return new TraversalLossPolicy(LossType.SUB_THEN_MULT, percentLoss, valueLoss);
    }

    private final LossType lossType;
    private final double percentLoss;
    private final int valueLoss;

    private TraversalLossPolicy(LossType lossType, double percentLoss, int valueLoss) {
        this.lossType = lossType;
        this.percentLoss = percentLoss;
        this.valueLoss = valueLoss;
    }

    public LossType getLossType() {
        return lossType;
    }

    public int getOutput(int wattsIn) {
        if (lossType == LossType.PERCENTAGE) {
            return (int) (wattsIn * (1 - percentLoss));
        } else if (lossType == LossType.SUBTRACTIVE) {
            return wattsIn - valueLoss;
        } else return wattsIn;
    }

    public int getInput(int wattsOut) {
        if (lossType == LossType.PERCENTAGE) {
            return (int) (wattsOut / (1 - percentLoss));
        } else if (lossType == LossType.SUBTRACTIVE) {
            return wattsOut + valueLoss;
        } else return wattsOut;
    }
}
