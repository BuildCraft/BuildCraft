/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.*;
import buildcraft.api.core.BCLog;
import buildcraft.core.blueprints.IndexRequirementMap;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;

public class BuildingSlotBlock extends BuildingSlot {

    public BlockPos pos;
    public SchematicBlockBase schematic;

    // TODO: Remove this ugly hack
    public IndexRequirementMap internalRequirementRemovalListener;

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
    public boolean writeToWorld(IBuilderContext context) {
        if (internalRequirementRemovalListener != null) {
            internalRequirementRemovalListener.remove(this);
        }

        if (mode == Mode.ClearIfInvalid) {
            if (!getSchematic().isAlreadyBuilt(context, pos)) {
                context.world().sendBlockBreakProgress(pos.hashCode(), pos, -1);
                if (BuildCraftBuilders.dropBrokenBlocks) {
                    BlockUtils.breakBlock((WorldServer) context.world(), pos);
                    return false;
                } else {
                    context.world().setBlockToAir(pos);
                    return true;
                }
            }
        } else {
            try {
                getSchematic().placeInWorld(context, pos, stackConsumed);

                // This is also slightly hackish, but that's what you get when
                // you're unable to break an API too much.
                if (!getSchematic().isAlreadyBuilt(context, pos)) {
                    if (context.world().isAirBlock(pos)) {
                        return false;
                    } else if (!(getSchematic() instanceof SchematicBlock) || !context.world().getBlockState(pos).getBlock().isAssociatedBlock(
                            ((SchematicBlock) getSchematic()).state.getBlock())) {
                        BCLog.logger.warn(
                                "Placed block does not match expectations! Most likely a bug in BuildCraft or a supported mod. Removed mismatched block.");
                        IBlockState state = context.world().getBlockState(pos);
                        BCLog.logger.warn("Location: " + pos + " - Block: " + Block.blockRegistry.getNameForObject(state.getBlock()) + "@" + state);
                        context.world().removeTileEntity(pos);
                        context.world().setBlockToAir(pos);
                        return false;
                    } else {
                        return true;
                    }
                }

                // This is slightly hackish, but it's a very important way to verify
                // the stored requirements for anti-cheating purposes.
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
                            if (getSchematic().isItemMatchingRequirement(s, ss)) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            BCLog.logger.warn(
                                    "Blueprint has MISMATCHING REQUIREMENTS! Potential corrupted/hacked blueprint! Removed mismatched block.");
                            BCLog.logger.warn("Location: " + pos + " - ItemStack: " + s.toString());
                            context.world().removeTileEntity(pos);
                            context.world().setBlockToAir(pos);
                            return true;
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

                if (e != null && e instanceof ITickable) {
                    ((ITickable) e).update();
                }

                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                context.world().setBlockToAir(pos);
                return false;
            }
        }

        return false;
    }

    @Override
    public void postProcessing(IBuilderContext context) {
        getSchematic().postProcessing(context, pos);
    }

    @Override
    public List<ItemStack> getRequirements(IBuilderContext context) {
        if (mode == Mode.ClearIfInvalid) {
            return new ArrayList<ItemStack>();
        } else {
            List<ItemStack> req = new ArrayList<ItemStack>();

            getSchematic().getRequirementsForPlacement(context, req);

            return req;
        }
    }

    @Override
    public Vec3 getDestination() {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    @Override
    public void writeCompleted(IBuilderContext context, double complete) {
        // TODO (TEST) Make sure that you can use pos.hashCode() to use the position properly!
        if (mode == Mode.ClearIfInvalid) {
            int progress;
            if (context.world().isAirBlock(pos)) progress = -1;
            else progress = (int) (complete * 10.0F) - 1;
            context.world().sendBlockBreakProgress(pos.hashCode(), pos, progress);
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

        stackConsumed = new ArrayList<ItemStack>();

        NBTTagList nbtStacks = nbt.getTagList("stackConsumed", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbtStacks.tagCount(); ++i) {
            stackConsumed.add(ItemStack.loadItemStackFromNBT(nbtStacks.getCompoundTagAt(i)));
        }

    }

    @Override
    public List<ItemStack> getStacksToDisplay() {
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
