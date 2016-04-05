package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

import buildcraft.core.BCRegistry;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.MigrationManager;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;

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
        return register(block, false, ItemBlock.class, null);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, boolean force) {
        return register(block, force, ItemBlock.class, null);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, Class<? extends ItemBlock> itemClass) {
        return register(block, false, itemClass, null);
    }

    public static <B extends BlockBuildCraftBase_BC8> B register(B block, boolean force, Class<? extends ItemBlock> itemClass, Consumer<B> postRegister) {
        if (BCRegistry.INSTANCE.registerBlock(block, itemClass, force)) {
            registeredBlocks.add(block);
            MigrationManager.INSTANCE.addBlockMigration(block, TagManager.getMultiTag(block.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            if (postRegister != null) postRegister.accept(block);
            return block;
        }
        return null;
    }
}
