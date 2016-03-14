/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.core.lib.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public class SchematicHanging extends SchematicEntity {

    private Item baseItem;

    public SchematicHanging(Item baseItem) {
        this.baseItem = baseItem;
    }

    @Override
    public void translateToBlueprint(Vec3 transform) {
        super.translateToBlueprint(transform);

        Vec3 pos = new Vec3(entityNBT.getInteger("TileX"), entityNBT.getInteger("TileY"), entityNBT.getInteger("TileZ"));
        pos = pos.add(transform);
        Vec3i floored = Utils.convertFloor(pos);
        entityNBT.setInteger("TileX", floored.getX());
        entityNBT.setInteger("TileY", floored.getY());
        entityNBT.setInteger("TileZ", floored.getZ());
    }

    @Override
    public void translateToWorld(Vec3 transform) {
        super.translateToWorld(transform);

        Vec3 pos = new Vec3(entityNBT.getInteger("TileX"), entityNBT.getInteger("TileY"), entityNBT.getInteger("TileZ"));
        pos = pos.add(transform);
        Vec3i floored = Utils.convertFloor(pos);
        entityNBT.setInteger("TileX", floored.getX());
        entityNBT.setInteger("TileY", floored.getY());
        entityNBT.setInteger("TileZ", floored.getZ());
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        super.rotateLeft(context);

        Vec3 pos = new Vec3(entityNBT.getInteger("TileX"), entityNBT.getInteger("TileY"), entityNBT.getInteger("TileZ"));
        pos = context.rotatePositionLeft(pos);
        entityNBT.setInteger("TileX", (int) pos.xCoord);
        entityNBT.setInteger("TileY", (int) pos.yCoord);
        entityNBT.setInteger("TileZ", (int) pos.zCoord);

        if (entityNBT.hasKey("Facing")) {
            int direction = entityNBT.getByte("Facing");
            direction = direction < 3 ? direction + 1 : 0;
            entityNBT.setByte("Facing", (byte) direction);
        } else if (entityNBT.hasKey("Direction")) {
            int direction = entityNBT.getByte("Direction");
            direction = direction < 3 ? direction + 1 : 0;
            entityNBT.setByte("Direction", (byte) direction);
        }
    }

    @Override
    public void readFromWorld(IBuilderContext context, Entity entity) {
        super.readFromWorld(context, entity);

        if (baseItem == Items.item_frame) {
            NBTTagCompound tag = entityNBT.getCompoundTag("Item");
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

            if (stack != null) {
                storedRequirements = new ItemStack[2];
                storedRequirements[0] = new ItemStack(baseItem);
                storedRequirements[1] = stack;
            } else {
                storedRequirements = new ItemStack[1];
                storedRequirements[0] = new ItemStack(baseItem);
            }
        } else {
            storedRequirements = new ItemStack[1];
            storedRequirements[0] = new ItemStack(baseItem);
        }
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context) {
        Vec3 newPosition = new Vec3(entityNBT.getInteger("TileX"), entityNBT.getInteger("TileY"), entityNBT.getInteger("TileZ"));

        int dir = entityNBT.getByte("Facing");

        for (Object o : context.world().loadedEntityList) {
            Entity e = (Entity) o;

            if (e instanceof EntityHanging) {
                EntityHanging h = (EntityHanging) e;
                Vec3 existingPosition = new Vec3(h.chunkCoordX, h.chunkCoordY, h.chunkCoordZ);

                if (existingPosition.distanceTo(newPosition) < 0.1F && EnumFacing.getFront(dir) == ((EntityHanging) e).facingDirection) {
                    return true;
                }
            }
        }

        return false;
    }
}
