package buildcraft.transport.plug;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.PipePluggable;
import buildcraft.api.transport.neptune.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugLens;

public class PluggableLens extends PipePluggable {
    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(min, ll, min, max, lu, max);
        BOXES[EnumFacing.UP.getIndex()] = new AxisAlignedBB(min, ul, min, max, uu, max);
        BOXES[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(min, min, ll, max, max, lu);
        BOXES[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(min, min, ul, max, max, uu);
        BOXES[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(ll, min, min, lu, max, max);
        BOXES[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(ul, min, min, uu, max, max);
    }

    public final EnumDyeColor colour;
    public final boolean isFilter;

    // Manual constructor (called by the specific item pluggable code)

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, EnumFacing side, EnumDyeColor colour, boolean isFilter) {
        super(def, holder, side);
        this.colour = colour;
        this.isFilter = isFilter;
    }

    // Saving + Loading

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        if (nbt.hasKey("colour")) {
            colour = NBTUtilBC.readEnum(nbt.getTag("colour"), EnumDyeColor.class);
        } else {
            colour = EnumDyeColor.byMetadata(nbt.getByte("c"));
        }
        isFilter = nbt.getBoolean("f");
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("colour", NBTUtilBC.writeEnum(colour));
        nbt.setBoolean("f", isFilter);
        return nbt;
    }

    // Networking

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        colour = MessageUtil.readEnumOrNull(buf, EnumDyeColor.class);
        isFilter = buf.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        MessageUtil.writeEnumOrNull(buf, colour);
        buf.writeBoolean(isFilter);
    }

    // Pluggable methods

    @Override
    public AxisAlignedBB getBoundingBox() {
        return BOXES[side.getIndex()];
    }

    @Override
    public ItemStack getPickStack() {
        return BCTransportItems.plugLens.getStack(colour, isFilter);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        switch (layer) {
            case CUTOUT:
            case TRANSLUCENT:
                return new KeyPlugLens(layer, side, colour, isFilter);
            default:
                return null;
        }
    }

    @PipeEventHandler
    public void tryInsert(PipeEventItem.TryInsert tryInsert) {
        if (isFilter && tryInsert.from == side) {
            EnumDyeColor itemColour = tryInsert.colour;
            if (itemColour != null && itemColour != colour) {
                tryInsert.cancel();
            }
        }
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck event) {
        if (isFilter) {
            if (event.colour == colour) {
                event.increasePriority(side);
            } else if (event.colour != null) {
                event.disallow(side);
            } else {
                event.decreasePriority(side);
            }
        }
    }

    @PipeEventHandler
    public void beforeInsert(PipeEventItem.BeforeInsert event) {
        if (!isFilter) {
            if (event.from == side) {
                event.colour = colour;
            }
        }
    }

    @PipeEventHandler
    public void reachEnd(PipeEventItem.ReachEnd event) {
        if (!isFilter) {
            if (event.to == side) {
                event.colour = colour;
            }
        }
    }
}
