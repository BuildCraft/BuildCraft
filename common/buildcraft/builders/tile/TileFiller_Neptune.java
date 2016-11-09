package buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.builders.bpt.TickingBlueprintBuilder;
import buildcraft.builders.bpt.TickingBlueprintBuilder.EnumBuilderMessage;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.bpt.Template;
import buildcraft.lib.bpt.vanilla.SchematicAir;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileFiller_Neptune extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final int NET_BOX = 10;
    public static final int NET_CLEAR = 11;
    public static final int NET_BUILD = 12;
    public static final int NET_ANIM_ITEM = 13;
    public static final int NET_ANIM_BLOCK = 14;
    public static final int NET_ANIM_FLUID = 15;
    public static final int NET_ANIM_POWER = 16;
    public static final int NET_ANIM_STATE = 17;

    public final IItemHandlerModifiable invResources = itemManager.addInvHandler("resources", 27, EnumAccess.NONE, EnumPipePart.VALUES);

    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);

    public final TickingBlueprintBuilder tickingBuilder = new TickingBlueprintBuilder(this::sendMessage, this::getSchematic);
    private BuilderAccessor accessor = null;

    private Box box = null;
    private Template currentTpl = null;
    private int cooldown = 0;

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        EnumFacing thisFacing = getWorld().getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity inFront = getWorld().getTileEntity(getPos().offset(thisFacing.getOpposite()));
        if (inFront instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) inFront;
            BlockPos min = provider.min();
            BlockPos max = provider.max();
            if (min != null && max != null && !min.equals(max)) {
                box = new Box(min, max);
                tickingBuilder.box = box;
                provider.removeFromWorld();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        if (worldObj.isRemote) {
            tickingBuilder.tick(Side.CLIENT);
            // client stuffs
        } else {
            if (accessor != null) {
                accessor.tick();
            }
            if (box != null && tickingBuilder.tick(Side.SERVER)) {
                cooldown--;
                if (cooldown <= 0) {
                    cooldown = 300;
                    if (accessor != null) {
                        accessor.releaseAll();
                    }
                    accessor = new BuilderAccessor(this, tickingBuilder);
                    tickingBuilder.reset(box, EnumAxisOrder.XZY.getMaxToMinOrder(), accessor);
                }
            }
        }
    }

    private SchematicBlock getSchematic(BlockPos bptPos) {
        // TODO: use the current template for a schematic
        return SchematicAir.INSTANCE;
    }

    // Networking

    private void sendMessage(EnumBuilderMessage type, IPayloadWriter writer) {
        final int id;
        if (type == EnumBuilderMessage.ANIMATION_BLOCK) id = NET_ANIM_BLOCK;
        else if (type == EnumBuilderMessage.ANIMATION_ITEM) id = NET_ANIM_ITEM;
        else if (type == EnumBuilderMessage.ANIMATION_FLUID) id = NET_ANIM_FLUID;
        else if (type == EnumBuilderMessage.ANIMATION_POWER) id = NET_ANIM_POWER;
        else if (type == EnumBuilderMessage.ANIMATION_STATE) id = NET_ANIM_STATE;
        else if (type == EnumBuilderMessage.BOX) id = NET_BOX;
        else if (type == EnumBuilderMessage.BUILD) id = NET_BUILD;
        else if (type == EnumBuilderMessage.CLEAR) id = NET_CLEAR;
        else throw new IllegalArgumentException("Unknown type " + type);
        createAndSendMessage(id, writer);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_BOX, buffer, side);
                writePayload(NET_ANIM_STATE, buffer, side);
            } else if (id == NET_BOX) {
                tickingBuilder.writePayload(EnumBuilderMessage.BOX, buffer, side);
            } else if (id == NET_ANIM_STATE) {
                tickingBuilder.writePayload(EnumBuilderMessage.ANIMATION_STATE, buffer, side);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                readPayload(NET_ANIM_STATE, buffer, side, ctx);
            } else if (id == NET_BOX) {
                tickingBuilder.readPayload(EnumBuilderMessage.BOX, buffer, side);
            } else if (id == NET_CLEAR || id == NET_BUILD) {
                BlockPos changeAt = buffer.readBlockPos();
                double x = changeAt.getX() + 0.5;
                double y = changeAt.getY() + 0.5;
                double z = changeAt.getZ() + 0.5;
                EnumParticleTypes type = id == NET_CLEAR ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.CLOUD;
                worldObj.spawnParticle(type, x, y, z, 0, 0, 0);
            }
            // All animation types
            else if (id == NET_ANIM_ITEM) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_ITEM, buffer, side);
            else if (id == NET_ANIM_BLOCK) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_BLOCK, buffer, side);
            else if (id == NET_ANIM_FLUID) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_FLUID, buffer, side);
            else if (id == NET_ANIM_POWER) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_POWER, buffer, side);
            else if (id == NET_ANIM_STATE) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_STATE, buffer, side);
        }
    }

    // Read-write

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        NBTTagCompound boxTag = nbt.getCompoundTag("box");
        if (!boxTag.hasNoTags()) {
            box = new Box();
            box.initialize(boxTag);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (box != null) {
            nbt.setTag("box", box.writeToNBT());
        }

        return nbt;
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public Box getBox() {
        return tickingBuilder.box;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), getBox());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("cooldown = " + cooldown);
        left.add("box = " + box);
    }
}
