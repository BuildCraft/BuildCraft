/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.MigrationManager;
import buildcraft.lib.RegistryHelper;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BlockBCBase_Neptune extends Block {
    private static List<BlockBCBase_Neptune> registeredBlocks = new ArrayList<>();

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public BlockBCBase_Neptune(Material material, String id) {
        super(material);
        this.id = id;
        setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));
    }

    public static <B extends BlockBCBase_Neptune> B register(B block) {
        return register(block, false, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, boolean force) {
        return register(block, force, ItemBlockBC_Neptune::new);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, Function<B, ItemBlockBC_Neptune> itemBlockConstructor) {
        return register(block, false, itemBlockConstructor);
    }

    public static <B extends BlockBCBase_Neptune> B register(B block, boolean force, Function<B, ItemBlockBC_Neptune> itemBlockConstructor) {
        if (RegistryHelper.registerBlock(block, force)) {
            registeredBlocks.add(block);
            MigrationManager.INSTANCE.addBlockMigration(block, TagManager.getMultiTag(block.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            if (itemBlockConstructor != null) {
                ItemBlockBC_Neptune item = itemBlockConstructor.apply(block);
                if (item != null) {
                    ItemManager.register(item, true);
                }
            }
            return block;
        }
        return null;
    }
}
