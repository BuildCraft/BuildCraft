package buildcraft.transport.plug;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class PluggableFacade extends PipePluggable {
    public static final int SIZE = 2;
    public final IBlockState state;
    public final boolean isHollow;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, IBlockState state, boolean isHollow) {
        super(definition, holder, side);
        this.state = state;
        this.isHollow = isHollow;
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        try {
            state = NBTUtilBC.readEntireBlockState(nbt.getCompoundTag("state"));
        } catch (LoadingException e) {
            throw new RuntimeException(e);
        }
        isHollow = nbt.getBoolean("isHollow");
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("state", NBTUtilBC.writeEntireBlockState(state));
        nbt.setBoolean("isHollow", isHollow);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        try {
            state = NBTUtilBC.readEntireBlockState(buffer.readCompoundTag());
        } catch (LoadingException | IOException e) {
            throw new RuntimeException(e);
        }
        isHollow = buf.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeCompoundTag(NBTUtilBC.writeEntireBlockState(state));
        buf.writeBoolean(isHollow);
    }

    // Pluggable methods

    @Override
    public AxisAlignedBB getBoundingBox() {
        return RotationUtil.rotateAABB(new AxisAlignedBB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, SIZE / 16D, 16 / 16D), side);
    }

    @Override
    public boolean isBlocking() {
        return !isHollow;
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        switch (layer) {
            case CUTOUT:
            case TRANSLUCENT:
                return new KeyPlugFacade(layer, side, state, isHollow);
            default:
                return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(int tintIndex) {
        return Minecraft.getMinecraft().getBlockColors().colorMultiplier(
                state,
                holder.getPipeWorld(),
                holder.getPipePos(),
                tintIndex / 6
        );
    }
}
