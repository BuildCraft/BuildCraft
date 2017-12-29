/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.craft.IAutoCraft;
import buildcraft.lib.tile.craft.WorkbenchCrafting;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public abstract class TileAutoWorkbenchBase extends TileBC_Neptune implements ITickable, IHasWork, IMjRedstoneReceiver, IAutoCraft {

    /** A redstone engine generates <code> 1 * {@link MjAPI#MJ}</code> per tick. This makes it a lot slower without one
     * powering it. */
    private static final long POWER_GEN_PASSIVE = MjAPI.MJ / 5;

    /** It takes 10 seconds to craft an item. */
    private static final long POWER_REQUIRED = POWER_GEN_PASSIVE * 20 * 10;

    private static final long POWER_LOST = POWER_GEN_PASSIVE * 10;

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResult;
    private final WorkbenchCrafting crafting;

    /** The amount of power that is stored until crafting can begin. When this reaches the minimum power required it
     * will craft the current recipe. */
    private long powerStored;
    private long powerStoredLast;

    public ItemStack resultClient = ItemStack.EMPTY;

    public TileAutoWorkbenchBase(int width, int height) {
        int slots = width * height;
        invBlueprint = itemManager.addInvHandler("blueprint", slots, EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", slots, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = itemManager.addInvHandler("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(width, height, this, invBlueprint, invMaterials, invResult);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, this, EnumPipePart.VALUES);
        caps.addProvider(new MjCapabilityHelper(this));
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        if (!ItemStack.areItemStacksEqual(before, after)) {
            crafting.onInventoryChange(handler);
        }
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }
        boolean didChange = crafting.tick();
        if (crafting.canCraft()) {
            if (powerStored >= POWER_REQUIRED) {
                if (crafting.craft()) {
                    // This is used for #hasWork(), to ensure that it doesn't return
                    // false for the one tick in between crafts.
                    powerStored = crafting.canCraft() ? 1 : 0;
                }
            } else {
                powerStored += POWER_GEN_PASSIVE;
            }
        } else if (powerStored >= POWER_LOST) {
            powerStored -= POWER_LOST;
        } else {
            powerStored = 0;
        }
        if (didChange) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_GUI_TICK) {
                buffer.writeLong(powerStored);
            } else if (id == NET_GUI_DATA) {
                buffer.writeItemStack(crafting.getAssumedResult());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_GUI_TICK) {
                powerStoredLast = powerStored;
                powerStored = buffer.readLong();
                if (powerStored < 10) {
                    // properly handle crafting finishes
                    powerStoredLast = powerStored;
                }
            } else if (id == NET_GUI_DATA) {
                resultClient = buffer.readItemStack();
            }
        }
    }

    @Override
    public boolean hasWork() {
        return powerStored > 0;
    }

    public double getProgress(float partialTicks) {
        return MathUtil.interp(partialTicks, powerStoredLast, powerStored) / POWER_REQUIRED;
    }

    public InventoryCrafting getWorkbenchCrafting() {
        return crafting;
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return POWER_REQUIRED - powerStored;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        long req = getPowerRequested();
        long taken = Math.min(req, microJoules);
        if (!simulate) {
            powerStored += taken;
        }
        return microJoules - taken;
    }

    // IAutoCraft

    @Override
    public ItemStack getCurrentRecipeOutput() {
        return crafting.getAssumedResult();
    }

    @Override
    public ItemHandlerSimple getInvBlueprint() {
        return invBlueprint;
    }
}
