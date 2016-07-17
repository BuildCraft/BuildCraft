package buildcraft.lib.gui.ledger;

public class LedgerInfo extends Ledger_Neptune {
    
    public final String infoTextType;
    public final int numTips;
    public LedgerInfo(LedgerManager_Neptune manager, String infoTextType, int numTips) {
        super(manager);
        this.infoTextType = infoTextType;
        this.numTips = numTips;
    }
    @Override
    public int getColour() {
        return 0;
    }
}
