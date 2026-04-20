/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.tiles.IHasWork;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.craft.IAutoCraft;
import ct.buildcraft.lib.tile.craft.WorkbenchCrafting;
import ct.buildcraft.lib.tile.item.ItemHandlerFiltered;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;

public abstract class TileAutoWorkbenchBase extends TileBC_Neptune
    implements IHasWork, IMjRedstoneReceiver, IAutoCraft {

    /** A redstone engine generates <code> 1 * {@link MjAPI#MJ}</code> per tick. This makes it a lot slower without one
     * powering it. */
    private static final long POWER_GEN_PASSIVE = MjAPI.MJ / 5;

    /** It takes 10 seconds to craft an item. */
    private static final long POWER_REQUIRED = POWER_GEN_PASSIVE * 20 * 10;

    private static final long POWER_LOST = POWER_GEN_PASSIVE * 10;

    private static final ResourceLocation ADVANCEMENT_AUTOCRAFT = new ResourceLocation("buildcraftfactory:lazy_crafting");

    // TODO: Store output in the next slot!
    // (Can be used to differentiate between different recipes)
    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterialFilter;
    public final ItemHandlerFiltered invMaterials;
    public final ItemHandlerSimple invResult;
    protected final WorkbenchCrafting crafting;
    /** The amount of power that is stored until crafting can begin. When this reaches the minimum power required it
     * will craft the current recipe. */
    private long powerStored;
    private long powerStoredLast;

    public ItemStack resultClient = ItemStack.EMPTY;

    public TileAutoWorkbenchBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int width, int height) {
    	super(type, pos, state);
        int slots = width * height;
        invBlueprint = itemManager.addInvHandler("blueprint", slots, EnumAccess.PHANTOM);
        invMaterialFilter = itemManager.addInvHandler("material_filter", slots, EnumAccess.PHANTOM);
        invMaterials = new ItemHandlerFiltered(invMaterialFilter, true);
        invMaterials.setCallback(itemManager.callback);
        itemManager.addInvHandler("materials", invMaterials, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = itemManager.addInvHandler("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(width, height, this, invBlueprint, invMaterials, invResult);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, this, EnumPipePart.VALUES);
        caps.addProvider(new MjCapabilityHelper(this));
        caps.addProvider(itemManager);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (!ItemStack.isSame(before, after)) {
            crafting.onInventoryChange(handler);
        }
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }
        boolean didChange = crafting.tick();
        if (crafting.canCraft()) {
            if (powerStored >= POWER_REQUIRED) {
                if (crafting.craft()) {
                    // This is used for #hasWork(), to ensure that it doesn't return
                    // false for the one tick in between crafts.
                    powerStored = crafting.canCraft() ? 1 : 0;
//                    AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_AUTOCRAFT);
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
            createFilters();
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_TICK) {
                buffer.writeLong(powerStored);
            } else if (id == NET_GUI_DATA) {
            	resultClient = crafting.getAssumedResult();
                //buffer.writeItem(crafting.getAssumedResult());
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_TICK) {
                powerStoredLast = powerStored;
                powerStored = buffer.readLong();
                if (powerStored < 10) {
                    // properly handle crafting finishes
                    powerStoredLast = powerStored;
                }
            }/* else if (id == NET_GUI_DATA) {
                //resultClient = buffer.readItem();
            }*/
        }
    }

    @Override
    public boolean hasWork() {
        return powerStored > 0;
    }

    public double getProgress(float partialTicks) {
        return MathUtil.interp(partialTicks, powerStoredLast, powerStored) / POWER_REQUIRED;
    }

    public WorkbenchCrafting getWorkbenchCrafting() {
        return crafting;
    }

    private void createFilters() {
        if (crafting.getAssumedResult().isEmpty()) {
            for (int s = 0; s < invMaterialFilter.getSlots(); s++) {
                invMaterialFilter.setStackInSlot(s, ItemStack.EMPTY);
            }
            return;
        }
        NonNullList<ItemStack> uniqueStacks = NonNullList.create();
        int slotCount = invBlueprint.getSlots();
        int[] requirements = new int[slotCount];
        for (int s = 0; s < slotCount; s++) {
            ItemStack bptStack = invBlueprint.getStackInSlot(s);
            if (!bptStack.isEmpty()) {
                boolean foundMatch = false;
                for (int i = 0; i < uniqueStacks.size(); i++) {
                    if (StackUtil.canMerge(bptStack, uniqueStacks.get(i))) {
                        foundMatch = true;
                        requirements[i]++;
                        break;
                    }
                }
                if (!foundMatch) {
                    requirements[uniqueStacks.size()] = 1;
                    uniqueStacks.add(bptStack);
                }
            }
        }
        int uniqueSlotCount = uniqueStacks.size();
        int[] slotAllocationCount = new int[uniqueSlotCount];
        Arrays.fill(slotAllocationCount, 1);
        int slotsLeft = slotCount - uniqueSlotCount;
        for (int i = 0; i < slotsLeft; i++) {
            int smallestDifference = Integer.MAX_VALUE;
            int smallestDifferenceIndex = 0;

            for (int s = 0; s < uniqueSlotCount; s++) {
                ItemStack stack = uniqueStacks.get(s);
                int uniqueCountTotal = stack.getMaxStackSize() * slotAllocationCount[s];

                int difference = uniqueCountTotal / requirements[s];
                if (difference < smallestDifference) {
                    smallestDifference = difference;
                    smallestDifferenceIndex = s;
                }
            }
            slotAllocationCount[smallestDifferenceIndex]++;
        }

        int realIndex = 0;
        for (int s = 0; s < uniqueSlotCount; s++) {
            ItemStack stack = uniqueStacks.get(s).copy();
            stack.setCount(1);
            for (int i = 0; i < slotAllocationCount[s]; i++) {
                invMaterialFilter.setStackInSlot(realIndex, stack);
                realIndex++;
            }
        }
        if (realIndex != slotCount) {
            throw new IllegalStateException("Somehow the balanced formula wasn't perfectly balanced!");
        }
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
    public long receivePower(long microJoules, FluidAction simulate) {
        long req = getPowerRequested();
        long taken = Math.min(req, microJoules);
        if (simulate.execute()) {
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
