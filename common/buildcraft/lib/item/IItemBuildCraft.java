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
package buildcraft.lib.item;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;

import gnu.trove.map.hash.TIntObjectHashMap;

public interface IItemBuildCraft {
    String id();

    default void init() {
        Item thisItem = (Item) this;
        thisItem.setUnlocalizedName(TagManager.getTag(id(), EnumTagType.UNLOCALIZED_NAME));
        thisItem.setRegistryName(TagManager.getTag(id(), EnumTagType.REGISTRY_NAME));
        thisItem.setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id(), EnumTagType.CREATIVE_TAB)));
    }

    /** Sets up all of the model information for this item. This is called multiple times, and you *must* make sure that
     * you add all the same values each time. Use {@link #addVariant(TIntObjectHashMap, int, String)} to help get
     * everything correct. */
    @SideOnly(Side.CLIENT)
    default void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "");
    }

    default void addVariant(TIntObjectHashMap<ModelResourceLocation> variants, int meta, String suffix) {
        String tag = TagManager.getTag(id(), EnumTagType.MODEL_LOCATION);
        variants.put(meta, new ModelResourceLocation(tag + suffix, "inventory"));
    }

    @SideOnly(Side.CLIENT)
    default void postRegisterClient() {
        Item thisItem = (Item) this;
        TIntObjectHashMap<ModelResourceLocation> variants = new TIntObjectHashMap<>();
        addModelVariants(variants);
        for (ModelResourceLocation variant : variants.values(new ModelResourceLocation[variants.size()])) {
            if (ItemManager.DEBUG) {
                BCLog.logger.info("[lib.item][" + thisItem.getRegistryName() + "] Registering a variant " + variant);
            }
            ModelBakery.registerItemVariants(thisItem, variant);
        }
    }
}
