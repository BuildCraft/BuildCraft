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
import buildcraft.lib.item.ItemBuildCraftBlock_BC8;

public class BlockBuildCraftBase_BC8 extends Block {
    private static List<BlockBuildCraftBase_BC8> registeredBlocks = new ArrayList<>();

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public BlockBuildCraftBase_BC8(Material material, String id) {
        super(material);
        this.id = id;
        setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block) {
        return register(block, false, ItemBuildCraftBlock_BC8::new);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, boolean force) {
        return register(block, force, ItemBuildCraftBlock_BC8::new);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, Function<B, ItemBuildCraftBlock_BC8> itemBlockConstructor) {
        return register(block, false, itemBlockConstructor);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, boolean force, Function<B, ItemBuildCraftBlock_BC8> itemBlockConstructor) {
        if (RegistryHelper.registerBlock(block, force)) {
            registeredBlocks.add(block);
            MigrationManager.INSTANCE.addBlockMigration(block, TagManager.getMultiTag(block.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            ItemBuildCraftBlock_BC8 item = itemBlockConstructor.apply(block);
            RegistryHelper.registerItem(item, true);
            return block;
        }
        return null;
    }
}
