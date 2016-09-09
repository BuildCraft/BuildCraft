package buildcraft.core;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.core.lib.recipe.NBTAwareShapedOreRecipe;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.ItemBlockBC_Neptune;

@Deprecated
public final class BCRegistry {
    public static final BCRegistry INSTANCE = new BCRegistry();
    private Configuration regCfg;

    private BCRegistry() {

    }

    public boolean isEnabled(String category, String name) {
        return regCfg.get(category, name, true).getBoolean();
    }

    public void setRegistryConfig(File f) {
        regCfg = new Configuration(f);
    }

    public boolean registerBlock(BlockBCBase_Neptune block, boolean forced) {
        return registerBlock(block, new ItemBlockBC_Neptune(block), forced);
    }

    public boolean registerBlock(Block block, Item item, boolean forced) {
        ResourceLocation name = block.getRegistryName();
        if (name == null) throw new IllegalArgumentException("Tried to register a block without specifing its registry name!");
        if (forced || regCfg.get("blocks", name.getResourcePath(), true).getBoolean()) {
            GameRegistry.register(block);
            BCLibProxy.getProxy().postRegisterBlock(block);
            registerItem(item, true);
            return true;
        }
        return false;
    }

    public boolean registerItem(Item item, boolean forced) {
        ResourceLocation name = item.getRegistryName();
        if (name == null) throw new IllegalArgumentException("Tried to register an item without specifing its registry name!");
        if (forced || regCfg.get("items", name.getResourcePath(), true).getBoolean()) {
            GameRegistry.register(item);
            BCLibProxy.getProxy().postRegisterItem(item);
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void registerTileEntity(Class clas, String... ident) {
        GameRegistry.registerTileEntityWithAlternatives(CompatHooks.INSTANCE.getTile(clas), ident[0], ident);
    }

    private boolean isInvalidRecipeElement(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof Block && !Utils.isRegistered((Block) o)) {
            return true;
        }
        if (o instanceof Item && !Utils.isRegistered((Item) o)) {
            return true;
        }
        if (o instanceof ItemStack && !Utils.isRegistered((ItemStack) o)) {
            return true;
        }

        return false;
    }

    public void addCraftingRecipe(ItemStack result, Object... recipe) {
        if (isInvalidRecipeElement(result)) {
            return;
        }

        for (Object o : recipe) {
            if (isInvalidRecipeElement(o)) {
                return;
            }
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(result, recipe));
    }

    public void addCraftingRecipeNBTAware(ItemStack result, Object... recipe) {
        if (isInvalidRecipeElement(result)) {
            return;
        }

        for (Object o : recipe) {
            if (isInvalidRecipeElement(o)) {
                return;
            }
        }

        GameRegistry.addRecipe(new NBTAwareShapedOreRecipe(result, recipe));
    }

    public void addShapelessRecipe(ItemStack result, Object... recipe) {
        if (isInvalidRecipeElement(result)) {
            return;
        }

        for (Object o : recipe) {
            if (isInvalidRecipeElement(o)) {
                return;
            }
        }

        GameRegistry.addRecipe(new ShapelessOreRecipe(result, recipe));
    }

    public void save() {
        regCfg.save();
    }
}
