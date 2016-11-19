/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt;

import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class MultiBlueprint<B extends BlueprintBase> implements INBTSerializable<NBTTagCompound> {
    private final B[] blueprints;
    private final Function<NBTTagCompound, B> loader;

    public static MultiBlueprint<Blueprint> createMultiBlueprint(Blueprint[] blueprints) {
        return new MultiBlueprint<>(blueprints, Blueprint::new);
    }

    public static MultiBlueprint<Template> createMultiTemplate(Template[] templates) {
        return new MultiBlueprint<>(templates, Template::new);
    }

    public static MultiBlueprint<?> loadMultiBlueprint(NBTTagCompound nbt) {
        String type = nbt.getString("type");
        int size = nbt.getInteger("subs");
        if ("bpt".equals(type)) {
            MultiBlueprint<Blueprint> multi = new MultiBlueprint<>(new Blueprint[size], Blueprint::new);
            multi.deserializeNBT(nbt);
            return multi;
        } else if ("tpl".equals(type)) {
            MultiBlueprint<Template> multi = new MultiBlueprint<>(new Template[size], Template::new);
            multi.deserializeNBT(nbt);
            return multi;
        } else {
            return null;// Invalid blueprint!
        }
    }

    protected MultiBlueprint(B[] blueprints, Function<NBTTagCompound, B> loader) {
        this.blueprints = blueprints;
        this.loader = loader;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("type", blueprints.getClass() == Blueprint[].class ? "bpt" : "tpl");
        nbt.setInteger("count", blueprints.length);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < blueprints.length; i++) {
            NBTTagCompound inside = list.getCompoundTagAt(i);
            blueprints[i] = loader.apply(inside);
        }
    }
}
