/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;

public class SchematicMinecart extends SchematicEntity {

    private Item baseItem;

    public SchematicMinecart(Item baseItem) {
        this.baseItem = baseItem;
    }

    @Override
    public void translateToBlueprint(Vec3 transform) {
        super.translateToBlueprint(transform);

        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3 pos = new Vec3(nbttaglist.getDoubleAt(0) - 0.5, nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2) - 0.5);
        entityNBT.setTag("Pos", this.newDoubleNBTList(pos.xCoord, pos.yCoord, pos.zCoord));
    }

    @Override
    public void translateToWorld(Vec3 transform) {
        super.translateToWorld(transform);

        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3 pos = new Vec3(nbttaglist.getDoubleAt(0) + 0.5, nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2) + 0.5);
        entityNBT.setTag("Pos", this.newDoubleNBTList(pos.xCoord, pos.yCoord, pos.zCoord));
    }

    @Override
    public void readFromWorld(IBuilderContext context, Entity entity) {
        super.readFromWorld(context, entity);

        storedRequirements = new ItemStack[1];
        storedRequirements[0] = new ItemStack(baseItem);
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context) {
        NBTTagList nbttaglist = entityNBT.getTagList("Pos", 6);
        Vec3 newPosition = new Vec3(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));

        for (Object o : context.world().loadedEntityList) {
            Entity e = (Entity) o;

            Vec3 existingPositon = new Vec3(e.posX, e.posY, e.posZ);

            if (e instanceof EntityMinecart) {
                if (existingPositon.distanceTo(newPosition) < 0.1F) {
                    return true;
                }
            }
        }

        return false;
    }

}
