package buildcraft.silicon.tile;

public class TileProgrammingTable extends TileLaserTableBase {
    @Override
    public boolean hasWork() {
        return true;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
