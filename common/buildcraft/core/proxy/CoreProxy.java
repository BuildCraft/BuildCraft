/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.proxy;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.core.ICoreProxy;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.CompatHooks;
import buildcraft.core.lib.items.ItemBlockBuildCraft;
import buildcraft.core.lib.utils.Utils;

public class CoreProxy implements ICoreProxy {

    @SidedProxy(clientSide = "buildcraft.core.proxy.CoreProxyClient", serverSide = "buildcraft.core.proxy.CoreProxy")
    public static CoreProxy proxy;

    /* BUILDCRAFT PLAYER */
    protected static WeakReference<EntityPlayer> buildCraftPlayer = new WeakReference<EntityPlayer>(null);

    public String getMinecraftVersion() {
        return Loader.instance().getMinecraftModContainer().getVersion();
    }

    /* INSTANCES */
    public Object getClient() {
        return null;
    }

    public World getClientWorld() {
        return null;
    }

    /* ENTITY HANDLING */
    public void removeEntity(Entity entity) {
        entity.worldObj.removeEntity(entity);
    }

    /* WRAPPER */
    @SuppressWarnings("rawtypes")
    public void feedSubBlocks(Block block, CreativeTabs tab, List itemList) {}

    public String getItemDisplayName(ItemStack newStack) {
        return "";
    }

    /* GFX */
    public void initializeRendering() {}

    public void initializeEntityRendering() {}

    /* REGISTRATION */
    public void registerBlock(Block block) {
        registerBlock(block, ItemBlockBuildCraft.class);
    }

    public void registerBlock(Block block, Class<? extends ItemBlock> item) {
        GameRegistry.registerBlock(block, item, block.getUnlocalizedName().replace("tile.", ""));
    }

    public void registerItem(Item item) {
        registerItem(item, item.getUnlocalizedName().replace("item.", ""));
    }

    public void registerItem(Item item, String overridingName) {
        GameRegistry.registerItem(item, overridingName);
    }

    public void registerTileEntity(Class<? extends TileEntity> clas, String ident) {
        GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(clas), ident);
    }

    public void registerTileEntity(Class<? extends TileEntity> clas, String id, String... alternatives) {
        GameRegistry.registerTileEntityWithAlternatives(CompatHooks.INSTANCE.getTile(clas), id, alternatives);
    }

    public void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
        stack.onCrafting(world, player, stack.stackSize);
    }

    @SuppressWarnings("unchecked")
    public void addCraftingRecipe(ItemStack result, Object... recipe) {
        String name = Utils.getNameForItem(result.getItem());
        if (name != null) {
            CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(result, recipe));
        }
    }

    @SuppressWarnings("unchecked")
    public void addShapelessRecipe(ItemStack result, Object... recipe) {
        String name = Utils.getNameForItem(result.getItem());
        if (name != null) {
            CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(result, recipe));
        }
    }

    public String playerName() {
        return "";
    }

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world) {
        EntityPlayer player = FakePlayerFactory.get(world, BuildCraftCore.gameProfile);

        return new WeakReference<EntityPlayer>(player);
    }

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world, BlockPos pos) {
        EntityPlayer player = FakePlayerFactory.get(world, BuildCraftCore.gameProfile);
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return new WeakReference<EntityPlayer>(player);
    }

    @Override
    public final WeakReference<EntityPlayer> getBuildCraftPlayer(WorldServer world) {
        if (CoreProxy.buildCraftPlayer.get() == null) {
            CoreProxy.buildCraftPlayer = createNewPlayer(world);
        } else {
            CoreProxy.buildCraftPlayer.get().worldObj = world;
        }

        return CoreProxy.buildCraftPlayer;
    }

    public final WeakReference<EntityPlayer> getBuildCraftPlayer(WorldServer world, BlockPos pos) {
        if (CoreProxy.buildCraftPlayer.get() == null) {
            CoreProxy.buildCraftPlayer = createNewPlayer(world, pos);
        } else {
            CoreProxy.buildCraftPlayer.get().worldObj = world;
            CoreProxy.buildCraftPlayer.get().posX = pos.getX();
            CoreProxy.buildCraftPlayer.get().posY = pos.getY();
            CoreProxy.buildCraftPlayer.get().posZ = pos.getZ();
        }

        return CoreProxy.buildCraftPlayer;
    }

    /** This function returns either the player from the handler if it's on the server, or directly from the minecraft
     * instance if it's the client. */
    public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
        if (handler instanceof NetHandlerPlayServer) {
            return ((NetHandlerPlayServer) handler).playerEntity;
        } else {
            return Minecraft.getMinecraft().thePlayer;
        }
    }
}
