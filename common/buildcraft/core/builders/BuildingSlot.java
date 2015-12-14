/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.Schematic;

public abstract class BuildingSlot {

    public List<ItemStack> stackConsumed;

    public boolean reserved = false;

    public boolean built = false;

    /** @return True if this slot was successfully placed into the world, false if it was not (And any item contents
     *         should be dropped) */
    public boolean writeToWorld(IBuilderContext context) {
        return false;
    }

    public void writeCompleted(IBuilderContext context, double complete) {}

    public void postProcessing(IBuilderContext context) {}

    public List<ItemStack> getRequirements(IBuilderContext context) {
        return new ArrayList<ItemStack>();
    }

    public abstract Vec3 getDestination();

    public void addStackConsumed(ItemStack stack) {
        if (stackConsumed == null) {
            stackConsumed = new LinkedList<ItemStack>();
        }

        stackConsumed.add(stack);
    }

    public List<ItemStack> getStacksToDisplay() {
        return getSchematic().getStacksToDisplay(stackConsumed);
    }

    public abstract boolean isAlreadyBuilt(IBuilderContext context);

    public abstract Schematic getSchematic();

    public abstract void writeToNBT(NBTTagCompound nbt, MappingRegistry registry);

    public abstract void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException;

    public abstract int getEnergyRequirement();

    public abstract int buildTime();

}
