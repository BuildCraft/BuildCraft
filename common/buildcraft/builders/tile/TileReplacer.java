/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

import java.util.Date;

public class TileReplacer extends TileBC_Neptune implements ITickable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("replacer");

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler("snapshot", 1, ItemHandlerManager.EnumAccess.NONE);
    public final ItemHandlerSimple invSchematicFrom = itemManager.addInvHandler("schematicFrom", 1, ItemHandlerManager.EnumAccess.NONE);
    public final ItemHandlerSimple invSchematicTo = itemManager.addInvHandler("schematicTo", 1, ItemHandlerManager.EnumAccess.NONE);

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }
        if (!invSnapshot.getStackInSlot(0).isEmpty() &&
            !invSchematicFrom.getStackInSlot(0).isEmpty() &&
            !invSchematicTo.getStackInSlot(0).isEmpty()) {
            Header header = BCBuildersItems.snapshot.getHeader(invSnapshot.getStackInSlot(0));
            if (header != null) {
                Snapshot snapshot = GlobalSavedDataSnapshots.get(world).getSnapshot(header.key);
                if (snapshot instanceof Blueprint) {
                    Blueprint blueprint = (Blueprint) snapshot;
                    try {
                        ISchematicBlock<?> from = SchematicBlockManager.readFromNBT(
                            NBTUtilBC.getItemData(invSchematicFrom.getStackInSlot(0))
                                .getCompoundTag(ItemSchematicSingle.NBT_KEY)
                        );
                        ISchematicBlock<?> to = SchematicBlockManager.readFromNBT(
                            NBTUtilBC.getItemData(invSchematicTo.getStackInSlot(0))
                                .getCompoundTag(ItemSchematicSingle.NBT_KEY)
                        );
                        Blueprint newBlueprint = blueprint.copy();
                        newBlueprint.replace(from, to);
                        newBlueprint.computeKey();
                        GlobalSavedDataSnapshots.get(world).addSnapshot(newBlueprint);
                        invSnapshot.setStackInSlot(
                            0,
                            BCBuildersItems.snapshot.getUsed(
                                EnumSnapshotType.BLUEPRINT,
                                new Header(
                                    blueprint.key,
                                    getOwner().getId(),
                                    new Date(),
                                    header.name
                                )
                            )
                        );
                        invSchematicFrom.setStackInSlot(0, ItemStack.EMPTY);
                        invSchematicTo.setStackInSlot(0, ItemStack.EMPTY);
                    } catch (InvalidInputDataException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
