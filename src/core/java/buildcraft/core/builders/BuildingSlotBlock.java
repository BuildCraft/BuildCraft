/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.Position;
import buildcraft.builders.BuildCraftBuilders;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;

public class BuildingSlotBlock extends BuildingSlot {

    public BlockPos pos;
    public SchematicBlockBase schematic;

    public enum Mode {
        ClearIfInvalid,
        Build
    }

    public Mode mode = Mode.Build;

    public int buildStage = 0;

    @Override
    public SchematicBlockBase getSchematic() {
        if (schematic == null) {
            return new SchematicMask(false);
        } else {
            return schematic;
        }
    }

    @Override
    public void writeToWorld(IBuilderContext context) {
        if (mode == Mode.ClearIfInvalid) {
            if (!getSchematic().isAlreadyBuilt(context, pos)) {
                if (BuildCraftBuilders.dropBrokenBlocks) {
                    BlockUtils.breakBlock((WorldServer) context.world(), pos);
                } else {
                    context.world().setBlockToAir(pos);
                }
            }
        } else {
            try {
                getSchematic().placeInWorld(context, pos, stackConsumed);

                // This is slightly hackish, but it's a very important way to verify
                // the stored requirements.

                if (!context.world().isAirBlock(pos) && getSchematic().getBuildingPermission() == BuildingPermission.ALL
                    && getSchematic() instanceof SchematicBlock) {
                    SchematicBlock sb = (SchematicBlock) getSchematic();
                    // Copy the old array of stored requirements.
                    ItemStack[] oldRequirementsArray = sb.storedRequirements;
                    List<ItemStack> oldRequirements = Arrays.asList(oldRequirementsArray);
                    sb.storedRequirements = new ItemStack[0];
                    sb.storeRequirements(context, pos);
                    for (ItemStack s : sb.storedRequirements) {
                        boolean contains = false;
                        for (ItemStack ss : oldRequirements) {
                            if (StackHelper.isMatchingItem(s, ss)) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            BCLog.logger
                                .warn("Blueprint has MISMATCHING REQUIREMENTS! Potential corrupted/hacked blueprint! Removed mismatched block.");
                            BCLog.logger.warn("Location: " + pos + " - ItemStack: " + s.toString());
                            context.world().removeTileEntity(pos);
                            context.world().setBlockToAir(pos);
                            return;
                        }
                    }
                    // Restore the stored requirements.
                    sb.storedRequirements = oldRequirementsArray;
                }

                // Once the schematic has been written, we're going to issue
                // calls
                // to various functions, in particular updating the tile entity.
                // If these calls issue problems, in order to avoid corrupting
                // the world, we're logging the problem and setting the block to
                // air.

                TileEntity e = context.world().getTileEntity(pos);

                if (e != null && e instanceof IUpdatePlayerListBox) {
                    ((IUpdatePlayerListBox) e).update();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                context.world().setBlockToAir(pos);
            }
        }
    }

    @Override
    public void postProcessing(IBuilderContext context) {
        getSchematic().postProcessing(context, pos);
    }

    @Override
    public LinkedList<ItemStack> getRequirements(IBuilderContext context) {
        if (mode == Mode.ClearIfInvalid) {
            return new LinkedList<ItemStack>();
        } else {
            LinkedList<ItemStack> req = new LinkedList<ItemStack>();

            getSchematic().getRequirementsForPlacement(context, req);

            return req;
        }
    }

    @Override
    public Position getDestination() {
        return new Position(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    @Override
    public void writeCompleted(IBuilderContext context, double complete) {
        // TODO (TEST) Make sure that you can use pos.hashCode() to use the position properly!
        if (mode == Mode.ClearIfInvalid) {
            context.world().sendBlockBreakProgress(pos.hashCode(), pos, (int) (complete * 10.0F) - 1);
        }
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context) {
        return schematic.isAlreadyBuilt(context, pos);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        nbt.setByte("mode", (byte) mode.ordinal());
        nbt.setTag("pos", NBTUtils.writeBlockPos(pos));

        if (schematic != null) {
            NBTTagCompound schematicNBT = new NBTTagCompound();
            SchematicFactory.getFactory(schematic.getClass()).saveSchematicToWorldNBT(schematicNBT, schematic, registry);
            nbt.setTag("schematic", schematicNBT);
        }

        NBTTagList nbtStacks = new NBTTagList();

        if (stackConsumed != null) {
            for (ItemStack stack : stackConsumed) {
                NBTTagCompound nbtStack = new NBTTagCompound();
                stack.writeToNBT(nbtStack);
                nbtStacks.appendTag(nbtStack);
            }
        }

        nbt.setTag("stackConsumed", nbtStacks);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException {
        mode = Mode.values()[nbt.getByte("mode")];
        pos = NBTUtils.readBlockPos(nbt);

        if (nbt.hasKey("schematic")) {
            schematic = (SchematicBlockBase) SchematicFactory.createSchematicFromWorldNBT(nbt.getCompoundTag("schematic"), registry);
        }

        stackConsumed = new LinkedList<ItemStack>();

        NBTTagList nbtStacks = nbt.getTagList("stackConsumed", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbtStacks.tagCount(); ++i) {
            stackConsumed.add(ItemStack.loadItemStackFromNBT(nbtStacks.getCompoundTagAt(i)));
        }

    }

    @Override
    public LinkedList<ItemStack> getStacksToDisplay() {
        if (mode == Mode.ClearIfInvalid) {
            return stackConsumed;
        } else {
            return getSchematic().getStacksToDisplay(stackConsumed);
        }
    }

    @Override
    public int getEnergyRequirement() {
        return schematic.getEnergyRequirement(stackConsumed);
    }

    @Override
    public int buildTime() {
        if (schematic == null) {
            return 1;
        } else {
            return schematic.buildTime();
        }
    }

}
