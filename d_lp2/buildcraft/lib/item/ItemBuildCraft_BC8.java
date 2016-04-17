package buildcraft.lib.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.core.BCRegistry;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.MigrationManager;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;

public class ItemBuildCraft_BC8 extends Item {
    private static List<ItemBuildCraft_BC8> registeredItems = new ArrayList<>();

    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBuildCraft_BC8(String id) {
        this.id = id;
        setUnlocalizedName(TagManager.getTag(id, EnumTagType.UNLOCALIZED_NAME));
        setRegistryName(TagManager.getTag(id, EnumTagType.REGISTRY_NAME));
        setCreativeTab(CreativeTabManager.getTab(TagManager.getTag(id, EnumTagType.CREATIVE_TAB)));
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item) {
        return register(item, false);
    }

    public static <I extends ItemBuildCraft_BC8> I register(I item, boolean force) {
        if (BCRegistry.INSTANCE.registerItem(item, force)) {
            registeredItems.add(item);
            MigrationManager.INSTANCE.addItemMigration(item, TagManager.getMultiTag(item.id, EnumTagTypeMulti.OLD_REGISTRY_NAME));
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

    @SideOnly(Side.CLIENT)
    protected void registerModel() {
        registerModel(this, 0, TagManager.getTag(id, EnumTagType.MODEL_LOCATION));
    }

    public static void registerModel(Item item, int meta, String type) {
        ModelResourceLocation mrl = new ModelResourceLocation(type, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, mrl);
        ModelBakery.registerItemVariants(item, mrl);
    }

    @SideOnly(Side.CLIENT)
    public static void fmlInitClient() {
        for (ItemBuildCraft_BC8 item : registeredItems) {
            item.registerModel();
        }
    }
}
