/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.proxy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.core.EntityLaser;
import buildcraft.core.client.BuildCraftStateMapper;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.engines.RenderEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.ICustomStateMapper;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderLaser;

public class CoreProxyClient extends CoreProxy {

    /* INSTANCES */
    @Override
    public Object getClient() {
        return FMLClientHandler.instance().getClient();
    }

    @Override
    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().theWorld;
    }

    /* ENTITY HANDLING */
    @Override
    public void removeEntity(Entity entity) {
        super.removeEntity(entity);

        if (entity.worldObj.isRemote) {
            ((WorldClient) entity.worldObj).removeEntityFromWorld(entity.getEntityId());
        }
    }

    /* WRAPPER */
    @Override
    public String getItemDisplayName(ItemStack stack) {
        if (stack.getItem() == null) {
            return "";
        }

        return stack.getDisplayName();
    }

    @Override
    public void initializeRendering() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEngineBase.class, new RenderEngine());
    }

    @Override
    public void initializeEntityRendering() {
        RenderingRegistry.registerEntityRenderingHandler(EntityResizableCuboid.class, RenderResizableCuboid.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, new RenderLaser());

        for (Block block : blocksToRegisterRenderersFor) {
            if (block instanceof IModelRegister) {
                ((IModelRegister) block).registerModels();
                continue;
            }

            IBlockState defaultState = block.getDefaultState();
            Multimap<Integer, IBlockState> metaStateMap = ArrayListMultimap.create();
            Map<IBlockState, String> stateTypeMap = Maps.newHashMap();

            for (IBlockState state : (List<IBlockState>) block.getBlockState().getValidStates()) {
                String type = BuildCraftStateMapper.getPropertyString(state);
                stateTypeMap.put(state, type);
                metaStateMap.put(block.damageDropped(state), state);
            }
            for (Entry<Integer, Collection<IBlockState>> entry : metaStateMap.asMap().entrySet()) {
                Collection<IBlockState> blockStates = entry.getValue();
                if (blockStates.isEmpty()) continue;
                if (blockStates.contains(defaultState)) {
                    registerBlockItemModel(defaultState, entry.getKey(), stateTypeMap.get(defaultState));
                } else {
                    IBlockState state = blockStates.iterator().next();
                    registerBlockItemModel(state, entry.getKey(), stateTypeMap.get(state));
                }
            }
        }
        for (Item item : itemsToRegisterRenderersFor) {
            if (item instanceof IModelRegister) {
                ((IModelRegister) item).registerModels();
            }
        }
    }

    private void registerBlockItemModel(IBlockState state, int meta, String type) {
        Block block = state.getBlock();
        ModelResourceLocation location = new ModelResourceLocation(Utils.getNameForBlock(block).replace("|", ""), type.toLowerCase());
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), meta, location);
    }

    /* BUILDCRAFT PLAYER */
    @Override
    public String playerName() {
        return FMLClientHandler.instance().getClient().thePlayer.getDisplayNameString();
    }

    /** This function returns either the player from the handler if it's on the server, or directly from the minecraft
     * instance if it's the client. */
    @Override
    public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
        if (handler instanceof NetHandlerPlayServer) {
            return super.getPlayerFromNetHandler(handler);
        } else {
            return Minecraft.getMinecraft().thePlayer;
        }
    }

    private LinkedList<Block> blocksToRegisterRenderersFor = new LinkedList<Block>();
    private LinkedList<Item> itemsToRegisterRenderersFor = new LinkedList<Item>();

    @Override
    public void postRegisterBlock(Block block) {
        blocksToRegisterRenderersFor.add(block);
        if (block instanceof ICustomStateMapper) {
            ((ICustomStateMapper) block).setCusomStateMappers();
        } else {
            ModelLoader.setCustomStateMapper(block, BuildCraftStateMapper.INSTANCE);
        }
    }

    @Override
    public void postRegisterItem(Item item) {
        itemsToRegisterRenderersFor.add(item);
    }

    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public <T extends TileEntity> T getServerTile(T source) {
        if (BuildCraftCore.useServerDataOnClient && Minecraft.getMinecraft().isSingleplayer() && source.getWorld().isRemote) {
            WorldServer w = DimensionManager.getWorld(source.getWorld().provider.getDimensionId());
            if (w != null && w.getChunkProvider() != null) {
                Chunk c = w.getChunkFromBlockCoords(source.getPos());
                if (c != null) {
                    TileEntity t = c.getTileEntity(source.getPos(), EnumCreateEntityType.CHECK);
                    if (t != null && t.getClass().equals(source.getClass())) {
                        return (T) t;
                    }
                }
            }
        }
        return source;
    }
}
