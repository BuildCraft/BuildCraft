package buildcraft.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.BuildCraftTransport;

import io.netty.buffer.ByteBuf;

public class PlugPluggable extends PipePluggable {
    private static final class PlugPluggableRenderer implements IPipePluggableStaticRenderer {
        public static final IPipePluggableStaticRenderer INSTANCE = new PlugPluggableRenderer();
        private float zFightOffset = 1 / 4096.0F;

        @Override
        public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
                EnumFacing face) {
            // TODO Auto-generated method stub
            return null;
        }

        // TODO (PASS 0): Fix this!
        // @Override
        // public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, EnumFacing side, PipePluggable
        // pipePluggable,
        // ITextureStates blockStateMachine, int renderPass, BlockPos pos) {
        // if (renderPass != 0) {
        // return;
        // }
        //
        // float[][] zeroState = new float[3][2];
        //
        // // X START - END
        // zeroState[0][0] = 0.25F + zFightOffset;
        // zeroState[0][1] = 0.75F - zFightOffset;
        // // Y START - END
        // zeroState[1][0] = 0.125F;
        // zeroState[1][1] = 0.251F;
        // // Z START - END
        // zeroState[2][0] = 0.25F + zFightOffset;
        // zeroState[2][1] = 0.75F - zFightOffset;
        //
        // blockStateMachine.getTextureState().setToStack(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipePlug.ordinal()));
        // // Structure
        // // Pipe
        //
        // float[][] rotated = MatrixTranformations.deepClone(zeroState);
        // MatrixTranformations.transform(rotated, side);
        //
        // renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
        // rotated[2][1]);
        // renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);
        //
        // // X START - END
        // zeroState[0][0] = 0.25F + 0.125F / 2 + zFightOffset;
        // zeroState[0][1] = 0.75F - 0.125F / 2 + zFightOffset;
        // // Y START - END
        // zeroState[1][0] = 0.25F;
        // zeroState[1][1] = 0.25F + 0.125F;
        // // Z START - END
        // zeroState[2][0] = 0.25F + 0.125F / 2;
        // zeroState[2][1] = 0.75F - 0.125F / 2;
        //
        // rotated = MatrixTranformations.deepClone(zeroState);
        // MatrixTranformations.transform(rotated, side);
        //
        // renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
        // rotated[2][1]);
        // renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);
        // }
    }

    public PlugPluggable() {

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
        bounds[0][0] = 0.25F;
        bounds[0][1] = 0.75F;
        // Y START - END
        bounds[1][0] = 0.125F;
        bounds[1][1] = 0.251F;
        // Z START - END
        bounds[2][0] = 0.25F;
        bounds[2][1] = 0.75F;

        MatrixTranformations.transform(bounds, side);
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public IPipePluggableStaticRenderer getRenderer() {
        return PlugPluggableRenderer.INSTANCE;
    }

    @Override
    public void writeData(ByteBuf data) {

    }

    @Override
    public void readData(ByteBuf data) {

    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        return false;
    }
}
