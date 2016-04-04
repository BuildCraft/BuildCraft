package a.buildcraft.lib.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.Item;

import net.minecraftforge.oredict.OreDictionary;

import a.buildcraft.lib.CreativeTabManager;
import a.buildcraft.lib.MigrationManager;
import a.buildcraft.lib.TagManager;
import a.buildcraft.lib.TagManager.EnumTagType;
import a.buildcraft.lib.TagManager.EnumTagTypeMulti;

import buildcraft.core.BCRegistry;

public class ItemBuildCraft_BC8 extends Item {
    private static List<ItemBuildCraft_BC8> registeredItems = new ArrayList<>();

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBuildCraft_BC8(String tag) {
        this.id = tag;
        setUnlocalizedName(TagManager.getTag(tag, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(tag, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(tag, EnumTagType.CREATIVE_TAB)));
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item) {
        return register(item, false, null);
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item, boolean force) {
        return register(item, force, null);
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item, Consumer<I> postRegister) {
        return register(item, false, postRegister);
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item, boolean force, Consumer<I> postRegister) {
        if (BCRegistry.INSTANCE.registerItem(item, force)) {
            registeredItems.add(item);
            MigrationManager.INSTANCE.addItemMigration(item, TagManager.getMultiTag(item.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
            if (postRegister != null) postRegister.accept(item);
            return item;
        }
        return null;
    }

    public static void fmlInit() {
        for (ItemBuildCraft_BC8 item : registeredItems) {
            if (TagManager.hasTag(item.id, EnumTagType.OREDICT_NAME)) {
                OreDictionary.registerOre(TagManager.getTag(item.id, EnumTagType.OREDICT_NAME), item);
            }
        }
    }
}
