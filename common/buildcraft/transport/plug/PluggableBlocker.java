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
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.client.model.key.KeyPlugBlocker;

public class PluggableBlocker extends PipePluggable {
    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(min, ll, min, max, lu, max);
        BOXES[EnumFacing.UP.getIndex()] = new AxisAlignedBB(min, ul, min, max, uu, max);
        BOXES[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(min, min, ll, max, max, lu);
        BOXES[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(min, min, ul, max, max, uu);
        BOXES[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(ll, min, min, lu, max, max);
        BOXES[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(ul, min, min, uu, max, max);
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
        toDrop.add(new ItemStack(BCTransportItems.plugBlocker));
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugBlocker);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) return new KeyPlugBlocker(side);
        return null;
    }
}
