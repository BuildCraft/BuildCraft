package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;
import cofh.api.energy.IEnergyHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.PipeIconProvider;

public class PowerAdapterPluggable extends PipePluggable implements IEnergyHandler {
    private IPipeTile container;

    public class PowerAdapterPluggableRenderer implements IPipePluggableRenderer {
        private float zFightOffset = 1 / 4096.0F;

        @Override
        public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, EnumFacing side, PipePluggable pipePluggable,
                ITextureStates blockStateMachine, int renderPass, BlockPos pos) {
            if (renderPass != 0) {
                return;
            }

            float[][] zeroState = new float[3][2];

            // X START - END
            zeroState[0][0] = 0.25F;
            zeroState[0][1] = 0.75F;
            // Y START - END
            zeroState[1][0] = 0.000F;
            zeroState[1][1] = 0.125F;
            // Z START - END
            zeroState[2][0] = 0.25F;
            zeroState[2][1] = 0.75F;

            blockStateMachine.getTextureState().setToStack(
                BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal())); // Structure
                                                                                                                                  // Pipe

            float[][] rotated = MatrixTranformations.deepClone(zeroState);
            MatrixTranformations.transform(rotated, side);

            renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
            renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);

            // X START - END
            zeroState[0][0] = 0.25F + 0.125F / 2 + zFightOffset;
            zeroState[0][1] = 0.75F - 0.125F / 2 + zFightOffset;
            // Y START - END
            zeroState[1][0] = 0.25F;
            zeroState[1][1] = 0.25F + 0.125F;
            // Z START - END
            zeroState[2][0] = 0.25F + 0.125F / 2;
            zeroState[2][1] = 0.75F - 0.125F / 2;

            blockStateMachine.getTextureState().setToStack(
                BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal())); // Structure
                                                                                                                                  // Pipe
            rotated = MatrixTranformations.deepClone(zeroState);
            MatrixTranformations.transform(rotated, side);

            renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
            renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);
        }
    }

    public PowerAdapterPluggable() {

    }

    @Override
    public void validate(IPipeTile pipe, EnumFacing direction) {
        this.container = pipe;
    }

    @Override
    public void invalidate() {
        this.container = null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {

    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public ItemStack[] getDropItems(IPipeTile pipe) {
        return new ItemStack[] { new ItemStack(BuildCraftTransport.plugItem) };
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
        float[][] bounds = new float[3][2];
        // X START - END
        bounds[0][0] = 0.1875F;
        bounds[0][1] = 0.8125F;
        // Y START - END
        bounds[1][0] = 0.000F;
        bounds[1][1] = 0.251F;
        // Z START - END
        bounds[2][0] = 0.1875F;
        bounds[2][1] = 0.8125F;

        MatrixTranformations.transform(bounds, side);
        return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public IPipePluggableRenderer getRenderer() {
        return new PowerAdapterPluggableRenderer();
    }

    @Override
    public void writeData(ByteBuf data) {

    }

    @Override
    public void readData(ByteBuf data) {

    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        int maxR = Math.min(40, maxReceive);
        if (container instanceof IEnergyHandler) {
            int energyCanReceive = ((IEnergyHandler) container).receiveEnergy(from, maxR, true);
            if (!simulate) {
                return ((IEnergyHandler) container).receiveEnergy(from, energyCanReceive, false);
            } else {
                return energyCanReceive;
            }
        }
        return 0;
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        if (container instanceof IEnergyHandler) {
            return ((IEnergyHandler) container).getEnergyStored(from);
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        if (container instanceof IEnergyHandler) {
            return ((IEnergyHandler) container).getMaxEnergyStored(from);
        } else {
            return 0;
        }
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }
}
