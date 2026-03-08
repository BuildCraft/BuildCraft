/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugLens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PluggableLens extends PipePluggable {
    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = Shapes.box(ul, min, min, uu, max, max);
    }

    public final DyeColor colour;
    public final boolean isFilter;

    // Manual constructor (called by the specific item pluggable code)

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, DyeColor colour,
                         boolean isFilter) {
        super(def, holder, side);
        this.colour = colour;
        this.isFilter = isFilter;
    }

    // Saving + Loading

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(def, holder, side);
        if (nbt.contains("colour")) {
            colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        } else {
            colour = DyeColor.byId(nbt.getByte("c"));
        }
        isFilter = nbt.getBoolean("f");
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("colour", NBTUtilBC.writeEnum(colour));
        nbt.putBoolean("f", isFilter);
        return nbt;
    }

    // Networking

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        colour = MessageUtil.readEnumOrNull(buf, DyeColor.class);
        isFilter = buf.readBoolean();
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        MessageUtil.writeEnumOrNull(buf, colour);
        buf.writeBoolean(isFilter);
    }

    // Pluggable methods

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.plugLens.get().getStack(colour, isFilter);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PluggableModelKey getModelRenderKey(RenderType layer) {
//        switch (layer) {
//            case CUTOUT:
//            case TRANSLUCENT:
//                return new KeyPlugLens(layer, side, colour, isFilter);
//            default:
//                return null;
//        }
        if (layer == RenderType.cutout() || layer == RenderType.translucent()) {
            return new KeyPlugLens(layer, side, colour, isFilter);
        } else {
            return null;
        }
    }

    @PipeEventHandler
    public void tryInsert(PipeEventItem.TryInsert tryInsert) {
        if (isFilter && tryInsert.from == side) {
            DyeColor itemColour = tryInsert.colour;
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

    /** Called from either *this* pipe, or the neighbouring pipe as given in compareSide. */
    void sideCheckAnyPos(PipeEventItem.SideCheck event, Direction compareSide) {
        // Note that this should *never* use "this.side" as it may be wrong!
        if (isFilter) {
            if (event.colour == colour) {
                event.increasePriority(compareSide);
            } else if (event.colour != null) {
                if (compareSide == side) {
                    event.disallow(compareSide);
                }
            } else {
                event.decreasePriority(compareSide);
            }
        }
    }

    @PipeEventHandler
    public void beforeInsert(PipeEventItem.OnInsert event) {
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
