package buildcraft.transport;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.item.ItemFacade;

import io.netty.buffer.ByteBuf;

public class FacadePluggable extends PipePluggable implements IFacadePluggable {
    public static final class FacadePluggableRenderer extends BuildCraftBakedModel implements IPipePluggableStaticRenderer {
        public static final IPipePluggableStaticRenderer INSTANCE = new FacadePluggableRenderer();

        private FacadePluggableRenderer() {
            super(null, null, null);// We only extend BuildCraftBakedModel to get the model functions
        }

        @Override
        public List<BakedQuad> renderStaticPluggable(IPipe pipe, PipePluggable pluggable, EnumFacing face) {
            List<BakedQuad> quads = Lists.newArrayList();
            IFacadePluggable facade = (IFacadePluggable) pluggable;

            // Use the particle texture for the block. Not ideal, but we have NO way of getting the actual
            // texture of the block without hackery...
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(facade
                    .getCurrentState());

            float[] uvs = new float[4];
            uvs[U_MIN] = sprite.getMinU();
            uvs[U_MAX] = sprite.getMaxU();
            uvs[V_MIN] = sprite.getMinV();
            uvs[V_MAX] = sprite.getMaxV();

            Vector3f outer = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f inner = Utils.convertFloat(Utils.convert(face, 1 / 16d).addVector(0.5, 0.5, 0.5));

            bakeFace(quads, face, outer, new Vector3f(0.5f, 0.5f, 0.5f), uvs);
            bakeFace(quads, face, inner, new Vector3f(0.5f, 0.5f, 0.5f), uvs);
            return quads;
        }
    }

    public ItemFacade.FacadeState[] states;
    private ItemFacade.FacadeState activeState;

    // Client sync
    private IBlockState state;
    private boolean transparent, renderAsHollow;

    public FacadePluggable(ItemFacade.FacadeState[] states) {
        this.states = states;
        prepareStates();
    }

    public FacadePluggable() {}

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        FacadePluggable other = (FacadePluggable) o;
        return other.state != state || other.transparent != transparent || other.renderAsHollow != renderAsHollow;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (states != null) {
            nbt.setTag("states", ItemFacade.FacadeState.writeArray(states));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("states")) {
            states = ItemFacade.FacadeState.readArray(nbt.getTagList("states", Constants.NBT.TAG_COMPOUND));
        }
    }

    @Override
    public ItemStack[] getDropItems(IPipeTile pipe) {
        if (states != null) {
            return new ItemStack[] { ItemFacade.getFacade(states) };
        } else {
            return new ItemStack[] { ItemFacade.getFacade(new ItemFacade.FacadeState(getCurrentState(), null, isHollow())) };
        }
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return !isHollow();
    }

    @Override
    public IBlockState getCurrentState() {
        prepareStates();
        return activeState == null ? state : activeState.state;
    }

    @Override
    public boolean isTransparent() {
        prepareStates();
        return activeState == null ? transparent : activeState.transparent;
    }

    public boolean isHollow() {
        prepareStates();
        return activeState == null ? renderAsHollow : activeState.hollow;
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
        float[][] bounds = new float[3][2];
        // X START - END
        bounds[0][0] = 0.0F;
        bounds[0][1] = 1.0F;
        // Y START - END
        bounds[1][0] = 0.0F;
        bounds[1][1] = TransportConstants.FACADE_THICKNESS;
        // Z START - END
        bounds[2][0] = 0.0F;
        bounds[2][1] = 1.0F;

        MatrixTranformations.transform(bounds, side);
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public boolean isSolidOnSide(IPipeTile pipe, EnumFacing direction) {
        return !isHollow();
    }

    @Override
    public IPipePluggableStaticRenderer getRenderer() {
        return FacadePluggableRenderer.INSTANCE;
    }

    @Override
    public void writeData(ByteBuf data) {
        prepareStates();

        if (activeState == null || activeState.state == null) {
            data.writeShort(0);
        } else {
            data.writeShort(Block.getIdFromBlock(activeState.state.getBlock()));
        }

        data.writeByte((activeState != null && activeState.transparent ? 128 : 0) | (activeState != null && activeState.hollow ? 64 : 0)
            | (activeState == null ? 0 : activeState.state.getBlock().getMetaFromState(activeState.state)));
    }

    @Override
    public void readData(ByteBuf data) {
        int blockId = data.readUnsignedShort();
        Block block;
        if (blockId > 0) {
            block = Block.getBlockById(blockId);
        } else {
            block = null;
        }

        int flags = data.readUnsignedByte();

        int meta = flags & 0x0F;
        state = block.getStateFromMeta(meta);
        transparent = (flags & 0x80) > 0;
        renderAsHollow = (flags & 0x40) > 0;
    }

    private void prepareStates() {
        if (activeState == null) {
            activeState = states != null && states.length > 0 ? states[0] : null;
        }
    }

    protected void setActiveState(int id) {
        if (id >= 0 && id < states.length) {
            activeState = states[id];
        }
    }
}
