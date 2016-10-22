/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;

import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.StringUtilBC;

public class Blueprint extends BlueprintBase {
    public LinkedList<SchematicEntity> entities = new LinkedList<>();

    public Blueprint() {
        super();
        id.extension = "bpt";
    }

    public Blueprint(BlockPos size) {
        super(size);
        id.extension = "bpt";
    }

    @Override
    public void rotateLeft(BptContext context) {
        for (SchematicEntity e : entities) {
            e.rotateLeft(context);
        }

        super.rotateLeft(context);
    }

    @Override
    public void translateToBlueprint(Vec3d transform) {
        super.translateToBlueprint(transform);

        for (SchematicEntity e : entities) {
            e.translateToBlueprint(transform);
        }
    }

    @Override
    public void translateToWorld(Vec3d transform) {
        super.translateToWorld(transform);

        for (SchematicEntity e : entities) {
            e.translateToWorld(transform);
        }
    }

    @Override
    public void readFromWorld(IBuilderContext context, TileEntity anchorTile, BlockPos pos) {
        BptContext bptContext = (BptContext) context;
        IBlockState state = anchorTile.getWorld().getBlockState(pos);

        if (context.world().isAirBlock(pos)) {
            // Although no schematic will be returned for the block "air" by
            // the registry, there can be other blocks considered as air. This
            // will make sure that they don't get recorded.
            return;
        }

        SchematicBlock slot = SchematicRegistry.INSTANCE.createSchematicBlock(state);

        if (slot == null) {
            return;
        }

        BlockPos contextPos = pos.subtract(context.surroundingBox().min());

        slot.state = state;

        if (!SchematicRegistry.INSTANCE.isSupported(state)) {
            return;
        }

        try {
            slot.initializeFromObjectAt(context, pos);
            slot.storeRequirements(context, pos);
            set(contextPos, slot);
        } catch (Throwable t) {
            // Defensive code against errors in implementers
            t.printStackTrace();
            BCLog.logger.throwing(t);
        }

        switch (slot.getBuildingPermission()) {
            case ALL:
                break;
            case CREATIVE_ONLY:
                if (bptContext.readConfiguration.allowCreative) {
                    if (buildingPermission == BuildingPermission.ALL) {
                        buildingPermission = BuildingPermission.CREATIVE_ONLY;
                    }
                } else {
                    set(contextPos, null);
                }
                break;
            case NONE:
                buildingPermission = BuildingPermission.NONE;
                break;
        }
    }

    @Override
    public void readEntitiesFromWorld(IBuilderContext context, TileEntity anchorTile) {
        BptContext bptContext = (BptContext) context;
        // Should this be used somewhere?
        Vec3d transform = new Vec3d(0, 0, 0).subtract(Utils.convert(context.surroundingBox().min()));

        for (Object o : context.world().loadedEntityList) {
            Entity e = (Entity) o;

            if (context.surroundingBox().contains(new Vec3d(e.posX, e.posY, e.posZ))) {
                SchematicEntity s = SchematicRegistry.INSTANCE.createSchematicEntity(e.getClass());

                if (s != null) {
                    s.readFromWorld(context, e);
                    switch (s.getBuildingPermission()) {
                        case ALL:
                            entities.add(s);
                            break;
                        case CREATIVE_ONLY:
                            if (bptContext.readConfiguration.allowCreative) {
                                if (buildingPermission == BuildingPermission.ALL) {
                                    buildingPermission = BuildingPermission.CREATIVE_ONLY;
                                }
                                entities.add(s);
                            }
                            break;
                        case NONE:
                            buildingPermission = BuildingPermission.NONE;
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void saveContents(NBTTagCompound nbt) {
        NBTTagCompound nbtContents = new NBTTagCompound();

        for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ORIGIN, size.subtract(Utils.POS_ONE))) {
            SchematicBlockBase schematic = null;
            NBTTagCompound cpt = new NBTTagCompound();
            try {
                schematic = get(pos);

                if (schematic != null) {
                    schematic.idsToBlueprint(mapping);
                    schematic.writeSchematicToNBT(cpt, mapping);
                    /* We don't use the index of the current for loop because we shouldn't rely on the behaviour of
                     * BlockPos.getAllInBox */
                    nbtContents.setTag(StringUtilBC.blockPosToShortString(pos), cpt);
                }
            } catch (Throwable t) {
                CrashReport crash;
                if (t instanceof ReportedException) {
                    crash = ((ReportedException) t).getCrashReport();
                } else {
                    crash = new CrashReport("Failed to save the contents of a blueprint!", t);
                }
                CrashReportCategory cat = crash.makeCategory("Block Being Saved");
                cat.addCrashSection("Block Position (In schematic)", pos);
                cat.addCrashSection("Schematic type", schematic == null ? "~~NULL~~" : schematic.getClass());
                mapping.addToCrashReport(crash.makeCategory("Mapping Registry"));
                throw new ReportedException(crash);
            }
        }

        nbt.setTag("contents", nbtContents);

        NBTTagList entitiesNBT = new NBTTagList();

        for (SchematicEntity s : entities) {
            NBTTagCompound subNBT = new NBTTagCompound();
            s.idsToBlueprint(mapping);
            s.writeSchematicToNBT(subNBT, mapping);
            entitiesNBT.appendTag(subNBT);
        }

        nbt.setTag("entities", entitiesNBT);

        NBTTagCompound contextNBT = new NBTTagCompound();
        mapping.write(contextNBT);
        nbt.setTag("idMapping", contextNBT);
    }

    private void loadSingleSchematicFromNBT(BlockPos pos, NBTTagCompound cpt) {
        if (cpt.hasKey("blockId")) {
            Block block;

            try {
                block = mapping.getBlockForId(cpt.getInteger("blockId"));
            } catch (MappingNotFoundException e) {
                block = null;
                isComplete = false;
            }

            if (block != null) {
                int meta = cpt.getInteger("blockMeta");
                SchematicBlockBase schematic = SchematicRegistry.INSTANCE.createSchematicBlock(block.getStateFromMeta(meta));
                if (schematic != null) {
                    schematic.readSchematicFromNBT(cpt, mapping);

                    if (!schematic.doNotUse()) {
                        schematic.idsToWorld(mapping);

                        switch (schematic.getBuildingPermission()) {
                            case ALL:
                                break;
                            case CREATIVE_ONLY:
                                if (buildingPermission == BuildingPermission.ALL) {
                                    buildingPermission = BuildingPermission.CREATIVE_ONLY;
                                }
                                break;
                            case NONE:
                                buildingPermission = BuildingPermission.NONE;
                                break;
                        }
                    } else {
                        schematic = null;
                        isComplete = false;
                    }
                }
                set(pos, schematic);
            } else {
                set(pos, null);
                isComplete = false;
            }
        } else {
            set(pos, null);
        }
    }

    @Override
    public void loadContents(NBTTagCompound nbt) throws BptError {
        mapping.read(nbt.getCompoundTag("idMapping"));

        NBTBase base = nbt.getTag("contents");
        if (base instanceof NBTTagCompound) {
            NBTTagCompound contents = (NBTTagCompound) base;
            for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ORIGIN, size.subtract(Utils.POS_ONE))) {
                NBTTagCompound single = contents.getCompoundTag(StringUtilBC.blockPosToShortString(pos));
                loadSingleSchematicFromNBT(pos, single);
            }
        } else {// 1.7.10 back-compat
            NBTTagList nbtContents = nbt.getTagList("contents", Constants.NBT.TAG_COMPOUND);

            int index = 0;

            for (int x = 0; x < size.getX(); ++x) {
                for (int y = 0; y < size.getY(); ++y) {
                    for (int z = 0; z < size.getZ(); ++z) {
                        NBTTagCompound cpt = nbtContents.getCompoundTagAt(index);
                        loadSingleSchematicFromNBT(new BlockPos(x, y, z), cpt);
                        index++;
                    }
                }
            }
        }

        NBTTagList entitiesNBT = nbt.getTagList("entities", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < entitiesNBT.tagCount(); ++i) {
            NBTTagCompound cpt = entitiesNBT.getCompoundTagAt(i);

            if (cpt.hasKey("entityId")) {
                Class<? extends Entity> entity;

                try {
                    entity = mapping.getEntityForId(cpt.getInteger("entityId"));
                } catch (MappingNotFoundException e) {
                    entity = null;
                    isComplete = false;
                }

                if (entity != null) {
                    SchematicEntity s = SchematicRegistry.INSTANCE.createSchematicEntity(entity);
                    s.readSchematicFromNBT(cpt, mapping);
                    s.idsToWorld(mapping);
                    entities.add(s);
                } else {
                    isComplete = false;
                }
            }
        }
    }

    @Override
    public ItemStack getStack() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation("BuildCraft|Builders:blueprintItem"));
        if (item == null) throw new Error("Could not find the blueprint item! Did you attempt to use this without buildcraft builders installed?");
        ItemStack stack = new ItemStack(item, 1, 1);
        NBTTagCompound nbt = NBTUtils.getItemData(stack);
        id.write(nbt);
        nbt.setString("author", author);
        nbt.setString("name", id.name);
        nbt.setByte("permission", (byte) buildingPermission.ordinal());
        nbt.setBoolean("isComplete", isComplete);

        return stack;
    }
}
