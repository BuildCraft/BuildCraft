package ct.buildcraft.transport.plug;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.transport.BCTransportItems;
import ct.buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class PluggablePowerAdaptor extends PipePluggable {

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double ll = 0D;
        double lu = 4D;
        double ul = 12D;
        double uu = 16D;

        double min = 3D;
        double max = 13D;

        BOXES[Direction.DOWN.get3DDataValue()] = Block.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = Block.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = Block.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = Block.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = Block.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = Block.box(ul, min, min, uu, max, max);
    }

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugPowerAdaptor.get());
    }

    @Override
    @Nullable
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) {
            return new KeyPlugPowerAdaptor(side);
        }
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_REDSTONE_RECEIVER) {
            return holder.getPipe().getBehaviour().getCapability(cap, side);
        }
        return LazyOptional.empty();
    }
}
