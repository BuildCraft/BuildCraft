/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.utils.NBTUtils;

public class Blueprint extends BlueprintBase {
    public LinkedList<SchematicEntity> entities = new LinkedList<SchematicEntity>();

    public Blueprint() {
        super();

        id.extension = "bpt";
    }

    public Blueprint(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);

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
    public void translateToBlueprint(Vec3 transform) {
        super.translateToBlueprint(transform);

        for (SchematicEntity e : entities) {
            e.translateToBlueprint(transform);
        }
    }

    @Override
    public void translateToWorld(Vec3 transform) {
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

        int posX = (int) (pos.getX() - context.surroundingBox().pMin().xCoord);
        int posY = (int) (pos.getY() - context.surroundingBox().pMin().yCoord);
        int posZ = (int) (pos.getZ() - context.surroundingBox().pMin().zCoord);

        slot.state = state;

        if (!SchematicRegistry.INSTANCE.isSupported(state)) {
            return;
        }

        try {
            slot.initializeFromObjectAt(context, pos);
            slot.storeRequirements(context, pos);
            contents[posX][posY][posZ] = slot;
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
                    contents[posX][posY][posZ] = null;
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
        Vec3 transform = new Vec3(0, 0, 0).subtract(context.surroundingBox().pMin());

        for (Object o : context.world().loadedEntityList) {
            Entity e = (Entity) o;

            if (context.surroundingBox().contains(new Vec3(e.posX, e.posY, e.posZ))) {
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
        NBTTagList nbtContents = new NBTTagList();

        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    NBTTagCompound cpt = new NBTTagCompound();

                    if (contents[x][y][z] != null) {
                        contents[x][y][z].idsToBlueprint(mapping);
                        contents[x][y][z].writeSchematicToNBT(cpt, mapping);
                    }

                    nbtContents.appendTag(cpt);
                }
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

    @Override
    public void loadContents(NBTTagCompound nbt) throws BptError {
        mapping.read(nbt.getCompoundTag("idMapping"));

        NBTTagList nbtContents = nbt.getTagList("contents", Constants.NBT.TAG_COMPOUND);

        int index = 0;

        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    NBTTagCompound cpt = nbtContents.getCompoundTagAt(index);
                    index++;

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
                            contents[x][y][z] = SchematicRegistry.INSTANCE.createSchematicBlock(block.getStateFromMeta(meta));
                            if (contents[x][y][z] != null) {
                                contents[x][y][z].readSchematicFromNBT(cpt, mapping);

                                if (!contents[x][y][z].doNotUse()) {
                                    contents[x][y][z].idsToWorld(mapping);

                                    switch (contents[x][y][z].getBuildingPermission()) {
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
                                    contents[x][y][z] = null;
                                    isComplete = false;
                                }
                            }
                        } else {
                            contents[x][y][z] = null;
                            isComplete = false;
                        }
                    } else {
                        contents[x][y][z] = null;
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
        Item item = (Item) Item.itemRegistry.getObject("BuildCraft|Builders:blueprintItem");
        if (item == null) {
            throw new Error("Could not find the blueprint item! Did you attempt to use this without buildcraft builders installed?");
        }
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
