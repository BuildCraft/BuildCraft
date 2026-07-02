/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.tile;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.gui.MenuReplacer;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileReplacer extends TileBC_Neptune implements MenuProvider {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("replacer");

    private static final StackInsertionFunction SINGLE_STACK_INSERTER = StackInsertionFunction.getInsertionFunction(1);

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler(
        "snapshot",
        1,
        TileReplacer::isUsedBlueprint,
        SINGLE_STACK_INSERTER,
        ItemHandlerManager.EnumAccess.NONE
    );
    public final ItemHandlerSimple invSchematicFrom = itemManager.addInvHandler(
        "schematicFrom",
        1,
        TileReplacer::isValidSingleSchematic,
        SINGLE_STACK_INSERTER,
        ItemHandlerManager.EnumAccess.NONE
    );
    public final ItemHandlerSimple invSchematicTo = itemManager.addInvHandler(
        "schematicTo",
        1,
        TileReplacer::isValidSingleSchematic,
        SINGLE_STACK_INSERTER,
        ItemHandlerManager.EnumAccess.NONE
    );

    private int lastSkippedInputFingerprint;

    public TileReplacer(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.REPLACER_TILE_BC8.get(), pos, state);
    }

    @Override
    public void update() {
        if (level == null || level.isClientSide) {
            return;
        }

        ItemStack snapshotStack = invSnapshot.getStackInSlot(0);
        ItemStack fromStack = invSchematicFrom.getStackInSlot(0);
        ItemStack toStack = invSchematicTo.getStackInSlot(0);
        if (snapshotStack.isEmpty() || fromStack.isEmpty() || toStack.isEmpty()) {
            return;
        }

        int inputFingerprint = getInputFingerprint(snapshotStack, fromStack, toStack);
        if (inputFingerprint == lastSkippedInputFingerprint) {
            return;
        }

        Header header = ItemSnapshot.getHeader(snapshotStack);
        if (header == null) {
            lastSkippedInputFingerprint = inputFingerprint;
            return;
        }

        Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
        if (!(snapshot instanceof Blueprint blueprint)) {
            lastSkippedInputFingerprint = inputFingerprint;
            return;
        }

        try {
            ISchematicBlock from = ItemSchematicSingle.getSchematic(fromStack);
            ISchematicBlock to = ItemSchematicSingle.getSchematic(toStack);
            if (from == null || to == null) {
                lastSkippedInputFingerprint = inputFingerprint;
                return;
            }
            if (Blueprint.schematicMatchesForReplacement(from, to)) {
                lastSkippedInputFingerprint = inputFingerprint;
                return;
            }

            Blueprint newBlueprint = blueprint.copy();
            int replacedBlocks = newBlueprint.replace(from, to);
            if (replacedBlocks <= 0) {
                lastSkippedInputFingerprint = inputFingerprint;
                return;
            }

            newBlueprint.computeKey();
            GlobalSavedDataSnapshots.get(level).addSnapshot(newBlueprint);
            invSnapshot.setStackInSlot(
                0,
                ItemSnapshot.getUsed(
                    EnumSnapshotType.BLUEPRINT,
                    new Header(
                        newBlueprint.key,
                        getOwnerId(header),
                        new Date(),
                        header.name
                    )
                )
            );
            invSchematicFrom.extractItem(0, 1, false);
            invSchematicTo.extractItem(0, 1, false);
            lastSkippedInputFingerprint = 0;
            setChanged();
        } catch (InvalidInputDataException e) {
            lastSkippedInputFingerprint = inputFingerprint;
            BCLog.logger.warn("Invalid schematic in replacer at " + worldPosition + ": " + e.getMessage());
        } catch (RuntimeException e) {
            lastSkippedInputFingerprint = inputFingerprint;
            BCLog.logger.warn("Failed to replace blueprint blocks at " + worldPosition, e);
        }
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        lastSkippedInputFingerprint = 0;
    }

    private static boolean isUsedBlueprint(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemSnapshot &&
            ItemSnapshot.EnumItemSnapshotType.getFromStack(stack) == ItemSnapshot.EnumItemSnapshotType.BLUEPRINT_USED;
    }

    private static boolean isValidSingleSchematic(int slot, @Nonnull ItemStack stack) {
        return ItemSchematicSingle.isValidUsed(stack);
    }

    private static int getInputFingerprint(ItemStack snapshotStack, ItemStack fromStack, ItemStack toStack) {
        CompoundTag tag = new CompoundTag();
        tag.put("snapshot", snapshotStack.save(new CompoundTag()));
        tag.put("from", fromStack.save(new CompoundTag()));
        tag.put("to", toStack.save(new CompoundTag()));
        return tag.hashCode();
    }

    private UUID getOwnerId(Header fallbackHeader) {
        return getOwner() != null && getOwner().getId() != null ? getOwner().getId() : fallbackHeader.owner;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MenuReplacer(id, inv, invSnapshot, invSchematicFrom, invSchematicTo, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }
}
