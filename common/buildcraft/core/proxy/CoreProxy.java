/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.proxy;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.SidedProxy;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.ICoreProxy;

public class CoreProxy implements ICoreProxy {

//    @SidedProxy(clientSide = "buildcraft.core.proxy.CoreProxyClient", serverSide = "buildcraft.core.proxy.CoreProxy")
    public static CoreProxy proxy;

    /* BUILDCRAFT PLAYER */
    protected static WeakReference<EntityPlayer> buildCraftPlayer = new WeakReference<>(null);

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

    public String getItemDisplayName(ItemStack newStack) {
        return "";
    }

    /* GFX */
    public void init() {}

    public void onCraftingPickup(World world, EntityPlayer player, ItemStack stack) {
        stack.onCrafting(world, player, stack.stackSize);
    }

    public String playerName() {
        return "";
    }

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world) {
        EntityPlayer player = FakePlayerFactory.get(world, BuildCraftCore.gameProfile);

        return new WeakReference<>(player);
    }

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world, BlockPos pos) {
        EntityPlayer player = FakePlayerFactory.get(world, BuildCraftCore.gameProfile);
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return new WeakReference<>(player);
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
        }
        return null;
    }

    public <T extends TileEntity> T getServerTile(T source) {
        return source;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void postRegisterBlock(Block block) {}

    public void postRegisterItem(Item item) {}

    public InputStream getStreamForResource(ResourceLocation location) {
        return null;
    }
}
