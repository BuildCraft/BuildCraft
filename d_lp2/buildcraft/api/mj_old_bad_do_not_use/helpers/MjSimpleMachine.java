package buildcraft.api._mj.helpers;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api._mj.IConnectionLogic;
import buildcraft.api._mj.IMjMachine;
import buildcraft.api._mj.MjAPI;
import buildcraft.api._mj.MjMachineIdentifier;

public abstract class MjSimpleMachine implements IMjMachine {
    protected static final EnumFacing[] ALL_FACES = { null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH };

    public final TileEntity tile;
    private final IConnectionLogic logic;
    private boolean refresh = false;
    private EnumFacing[] availableFaces;
    private ImmutableSet<MjMachineIdentifier> identifiers = ImmutableSet.of();

    public MjSimpleMachine(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces) {
        this.tile = tile;
        this.logic = logic;
        availableFaces = faces;
        if (tile.hasWorldObj()) initIdentifiers();
    }

    /** You should only call this if the tile has a world object. */
    private void initIdentifiers() {
        ImmutableSet.Builder<MjMachineIdentifier> idents = ImmutableSet.builder();
        int dim = tile.getWorld().provider.getDimension();
        if (availableFaces == null || availableFaces.length == 0) {
            idents.add(new MjMachineIdentifier(dim, tile.getPos(), null));
        } else {
            for (EnumFacing face : availableFaces) {
                idents.add(new MjMachineIdentifier(dim, tile.getPos(), face));
            }
        }
        identifiers = idents.build();
    }

    public void setAvailableFaces(EnumFacing[] arr) {
        availableFaces = arr;
        refresh = true;
    }

    public boolean canUpdate() {
        return tile.hasWorldObj();
    }

    public void refreshMachine() {
        refresh = true;
    }

    public void tick() {
        if (!canUpdate()) return;
        if (refresh || identifiers.isEmpty()) initIdentifiers();
        if (refresh) {
            refresh = false;
            MjAPI.NET_INSTANCE.refreshMachine(this);
        }
    }

    @Override
    public IConnectionLogic getConnectionLogic() {
        return logic;
    }

    @Override
    public Set<MjMachineIdentifier> getIdentifiers() {
        return identifiers;
    }
}
