/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.lib.bpt;

import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class MultiBlueprint<B extends BlueprintBase> implements INBTSerializable<NBTTagCompound> {
    private final B[] blueprints;
    private final String type;
    private final Function<NBTTagCompound, B> loader;

    public static MultiBlueprint<Blueprint> createMultiBlueprint(Blueprint[] blueprints) {
        return new MultiBlueprint<>("bpt", blueprints, Blueprint::new);
    }

    public static MultiBlueprint<Template> createMultiTemplate(Template[] templates) {
        return new MultiBlueprint<>("tpl", templates, Template::new);
    }

    public static MultiBlueprint<?> loadMultiBlueprint(NBTTagCompound nbt) {
        String type = nbt.getString("type");
        int size = nbt.getInteger("subs");
        if ("bpt".equals(type)) {
            MultiBlueprint<Blueprint> multi = new MultiBlueprint<>("bpt", new Blueprint[size], Blueprint::new);
            multi.deserializeNBT(nbt);
            return multi;
        } else if ("tpl".equals(type)) {
            MultiBlueprint<Template> multi = new MultiBlueprint<>("tpl", new Template[size], Template::new);
            multi.deserializeNBT(nbt);
            return multi;
        } else {
            return null;// Invalid blueprint!
        }
    }

    protected MultiBlueprint(String type, B[] blueprints, Function<NBTTagCompound, B> loader) {
        this.type = type;
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
