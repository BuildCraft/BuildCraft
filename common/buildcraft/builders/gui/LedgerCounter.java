package buildcraft.builders.gui;

import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.lib.gui.ledger.LedgerManager_Neptune;
import buildcraft.lib.gui.ledger.Ledger_Neptune;

import java.util.Optional;

public class LedgerCounter extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_6C_D4_1F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final ITileForSnapshotBuilder tile;

    public LedgerCounter(LedgerManager_Neptune manager, ITileForSnapshotBuilder tile) {
        super(manager);
        this.tile = tile;
        title = "gui.counter";

        appendText("Block left to break:", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(
            () ->
                String.valueOf(
                    Optional.ofNullable(tile.getBuilder())
                        .map(builder -> builder.leftToBreak)
                        .orElse(0)
                ),
            TEXT_COLOUR
        );
        appendText("Block left to place:", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(
            () ->
                String.valueOf(
                    Optional.ofNullable(tile.getBuilder())
                        .map(builder -> builder.leftToPlace)
                        .orElse(0)
                ),
            TEXT_COLOUR
        );
        calculateMaxSize();
    }

    @Override
    public int getColour() {
        return OVERLAY_COLOUR;
    }
}
