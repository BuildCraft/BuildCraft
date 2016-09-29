package buildcraft.transport.plug;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.api_move.PluggableDefinition.IPluggableCreator;
import buildcraft.transport.api_move.PluggableDefinition.IPluggableLoader;
import buildcraft.transport.client.model.key.KeyPlugBlocker;

public class PluggableBlocker extends PipePluggable {
    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    public static final IPluggableCreator CREATOR = (holder, side) -> new PluggableBlocker(BCTransportPlugs.blocker, holder, side);
    public static final IPluggableLoader LOADER = (holder, side, nbt) -> CREATOR.createPluggable(holder, side);

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;
        BOXES[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(0.25, ll, 0.25, 0.75, lu, 0.75);
        BOXES[EnumFacing.UP.getIndex()] = new AxisAlignedBB(0.25, ul, 0.25, 0.75, uu, 0.75);
        BOXES[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(0.25, 0.25, ll, 0.75, 0.75, lu);
        BOXES[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(0.25, 0.25, ul, 0.75, 0.75, uu);
        BOXES[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(ll, 0.25, 0.25, lu, 0.75, 0.75);
        BOXES[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(ul, 0.25, 0.25, uu, 0.75, 0.75);
    }

    public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        super(definition, holder, side);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return BOXES[side.getIndex()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void onRemove(List<ItemStack> toDrop) {
        toDrop.add(new ItemStack(BCTransportItems.plugStop));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) return new KeyPlugBlocker(side);
        return null;
    }
}
