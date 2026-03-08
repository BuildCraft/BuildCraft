/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import buildcraft.lib.registry.TagManager;

public interface IItemBuildCraft {
    String getIdBC();

    default void init() {
//        Item thisItem = (Item) this;
//        thisItem.setUnlocalizedName(TagManager.getTag(id(), TagManager.EnumTagType.UNLOCALIZED_NAME));
        this.setUnlocalizedName("item." + TagManager.getTag(this.getIdBC(), TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
//        thisItem.setRegistryName(TagManager.getTag(id(), TagManager.EnumTagType.REGISTRY_NAME));
//        thisItem.setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id(), TagManager.EnumTagType.CREATIVE_TAB)));
    }

    public abstract void setUnlocalizedName(String unlocalizedName);

    // Calen: not still used in 1.18.2
//    /** Sets up all of the model information for this item. This is called multiple times, and you *must* make sure that
//     * you add all the same values each time. Use {@link #addVariant(TIntObjectHashMap, int, String)} to help get
//     * everything correct. */
//    @SideOnly(Side.CLIENT)
//    default void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        addVariant(variants, 0, "");
//    }

    // Calen: not still used in 1.18.2
//    default void addVariant(TIntObjectHashMap<ModelResourceLocation> variants, int meta, String suffix) {
//        String tag = TagManager.getTag(id(), EnumTagType.MODEL_LOCATION);
//        variants.put(meta, new ModelResourceLocation(tag + suffix, "inventory"));
//    }

    // Calen: not still used in 1.18.2
//    @SideOnly(Side.CLIENT)
//    default void registerVariants() {
//        Item thisItem = (Item) this;
//        TIntObjectHashMap<ModelResourceLocation> variants = new TIntObjectHashMap<>();
//        addModelVariants(variants);
//        for (int key : variants.keys()) {
//            ModelResourceLocation variant = variants.get(key);
//            if (RegistryConfig.DEBUG) {
//                BCLog.logger.info("[lib.registry][" + thisItem.getRegistryName() + "] Registering a variant " + variant
//                        + " for damage " + key);
//            }
//            ModelLoader.setCustomModelResourceLocation(thisItem, key, variant);
//        }
//    }
}
