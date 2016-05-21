package buildcraft.factory.tile;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase {

    public TileAutoWorkbenchItems() {
        super(9);
    }

    @Override
    protected WorkbenchCrafting createCrafting() {
        return new WorkbenchCrafting(3, 3);
    }
}
