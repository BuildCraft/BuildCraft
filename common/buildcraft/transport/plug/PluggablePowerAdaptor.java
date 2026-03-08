package buildcraft.transport.plug;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluggablePowerAdaptor extends PipePluggable {

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double ll = 0 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = VoxelShapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = VoxelShapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = VoxelShapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = VoxelShapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = VoxelShapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = VoxelShapes.box(ul, min, min, uu, max, max);
    }

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
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
    @OnlyIn(Dist.CLIENT)
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
