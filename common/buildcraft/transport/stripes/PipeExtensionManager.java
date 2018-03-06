/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeExtensionManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.SoundUtil;

import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.wire.WireManager;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum PipeExtensionManager implements IPipeExtensionManager {
    INSTANCE;

    private final Int2ObjectOpenHashMap<List<PipeExtensionRequest>> requests = new Int2ObjectOpenHashMap<>();
    private final Set<PipeDefinition> retractionPipeDefs = new HashSet<>();

    @Override
    public boolean requestPipeExtension(World world, BlockPos pos, EnumFacing dir, IStripesActivator stripes, ItemStack stack) {
        if (world.isRemote || stack.isEmpty() || !(stack.getItem() instanceof IItemPipe)) {
            return false;
        }

        int id = world.provider.getDimension();
        List<PipeExtensionRequest> rList = requests.get(id);
        if (rList == null) {
            requests.put(id, rList = new ArrayList<>());
        }
        return rList.add(new PipeExtensionRequest(pos, dir, stripes, ((IItemPipe) stack.getItem()).getDefinition(), stack.copy()));
    }

    @Override
    public void registerRetractionPipe(PipeDefinition pipeDefinition) {
        if (pipeDefinition != null) {
            retractionPipeDefs.add(pipeDefinition);
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.phase != Phase.END || event.side != Side.SERVER) {
            return;
        }
        List<PipeExtensionRequest> rList = requests.get(event.world.provider.getDimension());
        if (rList == null) {
            return;
        }
        for (PipeExtensionRequest r : rList) {
            if (retractionPipeDefs.contains(r.pipeDef)) {
                retract(event.world, r);
            } else {
                extend(event.world, r);
            }
        }
        rList.clear();
    }

    private void retract(World w, PipeExtensionRequest r) {
        EnumFacing retractDir = r.dir.getOpposite();
        if (!isValidRetractionPath(w, r, retractDir)) {

            // check other directions
            List<EnumFacing> possible = new ArrayList<>();
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing.getAxis() != r.dir.getAxis()) {
                    if (isValidRetractionPath(w, r, facing)) {
                        possible.add(facing);
                    }
                }
            }

            if (possible.isEmpty()) {
                r.stripes.sendItem(r.stack.copy(), r.dir);
                return;
            }
            retractDir = possible.get(MathHelper.getInt(w.rand, 0, possible.size() - 1));
        }
        BlockPos p = r.pos.offset(retractDir);

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();
        // Always send back catalyst pipe
        stacksToSendBack.add(r.stack);

        // Step 1: Copy over existing stripes pipe
        BlockSnapshot blockSnapshot1 = BlockSnapshot.getBlockSnapshot(w, r.pos);
        IBlockState stripesStateOld = w.getBlockState(r.pos);
        TileEntity stripesTileOld = w.getTileEntity(r.pos);
        final GameProfile owner;
        // Fetch owner
        {
            IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null);
            if (stripesTileOld == null || holder == null) {
                BCLog.logger
                    .warn("Found an invalid request at " + r.pos + " as " + stripesTileOld + " was not a pipe tile!");
                return;
            }
            owner = holder.getOwner();
            PipeBehaviour behaviour = holder.getPipe().getBehaviour();
            if (behaviour instanceof PipeBehaviourStripes) {
                ((PipeBehaviourStripes) behaviour).direction = retractDir.getOpposite();
            }
        }

        NBTTagCompound stripesNBTOld = new NBTTagCompound();
        stripesTileOld.writeToNBT(stripesNBTOld);

        // Step 2: Remove previous pipe
        BlockSnapshot blockSnapshot2 = BlockSnapshot.getBlockSnapshot(w, p);
        NonNullList<ItemStack> list = NonNullList.create();
        boolean canceled = !BlockUtil.breakBlock((WorldServer) w, p, list, r.pos, owner);
        if (canceled) {
            blockSnapshot2.restore(true);
            TileEntity tile = w.getTileEntity(p);
            if (tile != null) {
                tile.onLoad();
            }
        }

        // Step 3: Place stripes pipe back and remove old one
        if (!canceled) {
            // - Correct NBT coordinates
            stripesNBTOld.setInteger("x", p.getX());
            stripesNBTOld.setInteger("y", p.getY());
            stripesNBTOld.setInteger("z", p.getZ());

            // - Create block and tile
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) w, owner, p);
            player.inventory.clear();
            w.setBlockState(p, stripesStateOld, 3);
            BlockEvent.PlaceEvent placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot2, r.dir, EnumHand.MAIN_HAND);
            if (canceled = placeEvent.isCanceled()) {
                blockSnapshot2.restore(true);
                TileEntity tile = w.getTileEntity(r.pos);
                if (tile != null) {
                    tile.onLoad();
                }
            } else {
                SoundUtil.playBlockBreak(w, p, blockSnapshot2.getReplacedBlock());

                canceled = !BlockUtil.breakBlock((WorldServer) w, r.pos, NonNullList.create(), r.pos, owner);
                if (canceled) {
                    blockSnapshot1.restore(true);
                    TileEntity tile1 = w.getTileEntity(r.pos);
                    if (tile1 != null) {
                        tile1.onLoad();
                    }

                    blockSnapshot2.restore(true);
                    TileEntity tile2 = w.getTileEntity(p);
                    if (tile2 != null) {
                        tile2.onLoad();
                    }
                } else {
                    stacksToSendBack.addAll(list);
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        ItemStack stack = player.inventory.removeStackFromSlot(i);
                        if (!stack.isEmpty()) {
                            stacksToSendBack.add(stack);
                        }
                    }
                }
            }
        }

        // Step 4: Hope for the best, clean up.
        cleanup(w, r, p, stacksToSendBack, canceled, stripesNBTOld);
    }

    private void extend(World w, PipeExtensionRequest r) {
        BlockPos p = r.pos.offset(r.dir);
        if (!w.isAirBlock(p) && !w.getBlockState(p).getBlock().isReplaceable(w, p)) {
            r.stripes.sendItem(r.stack.copy(), r.dir);
            return;
        }

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();

        // Step 1: Copy over and remove existing stripes pipe
        IBlockState stripesStateOld = w.getBlockState(r.pos);
        NBTTagCompound stripesNBTOld = new NBTTagCompound();
        TileEntity stripesTileOld = w.getTileEntity(r.pos);
        final GameProfile owner;
        // Fetch owner
        {
            IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null);
            if (stripesTileOld == null || holder == null) {
                BCLog.logger.warn("Found an invalid request at " + r.pos + " as " + stripesTileOld + " was not a pipe tile!");
                return;
            }
            owner = holder.getOwner();
        }

        stripesTileOld.writeToNBT(stripesNBTOld);
        BlockSnapshot blockSnapshot1 = BlockSnapshot.getBlockSnapshot(w, r.pos);
        boolean canceled = !BlockUtil.breakBlock((WorldServer) w, r.pos, NonNullList.create(), r.pos, owner);
        if (canceled) {
            stacksToSendBack.add(r.stack);

            blockSnapshot1.restore(true);
            TileEntity tile = w.getTileEntity(r.pos);
            if (tile != null) {
                tile.onLoad();
            }
        }

        NonNullList<ItemStack> list = NonNullList.create();

        // Step 2: Add new pipe
        if (!canceled) {
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) w, owner, r.pos);
            player.inventory.clear();
            player.inventory.setInventorySlotContents(player.inventory.currentItem, r.stack);
            EnumActionResult result = ForgeHooks.onPlaceItemIntoWorld(r.stack, player, w, r.pos, r.dir.getOpposite(), 0.5F, 0.5F, 0.5F, EnumHand.MAIN_HAND);
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.removeStackFromSlot(i);
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
            if (canceled = result != EnumActionResult.SUCCESS) {
                blockSnapshot1.restore(true);
                TileEntity tile = w.getTileEntity(r.pos);
                if (tile != null) {
                    tile.onLoad();
                }
            }
        }

        // Step 3: Place stripes pipe back
        if (!canceled) {
            // - Correct NBT coordinates
            stripesNBTOld.setInteger("x", p.getX());
            stripesNBTOld.setInteger("y", p.getY());
            stripesNBTOld.setInteger("z", p.getZ());

            // - Create block and tile
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) w, owner, p);
            player.inventory.clear();
            BlockSnapshot blockSnapshot2 = BlockSnapshot.getBlockSnapshot(w, p);
            w.setBlockState(p, stripesStateOld, 3);
            BlockEvent.PlaceEvent placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot2, r.dir.getOpposite(), EnumHand.MAIN_HAND);
            if (canceled = placeEvent.isCanceled()) {
                stacksToSendBack.add(r.stack);

                blockSnapshot1.restore(true);
                TileEntity tile = w.getTileEntity(r.pos);
                if (tile != null) {
                    tile.onLoad();
                }

                blockSnapshot2.restore(true);
            } else {
                stacksToSendBack.addAll(list);
            }
        } else {
            stacksToSendBack.addAll(list);
        }

        // Step 4: Hope for the best, clean up.
        cleanup(w, r, p, stacksToSendBack, canceled, stripesNBTOld);
    }

    private void cleanup(World w, PipeExtensionRequest r, BlockPos p, NonNullList<ItemStack> stacksToSendBack, boolean canceled, NBTTagCompound stripesNBTOld) {
        TileEntity stripesTileNew = w.getTileEntity(canceled ? r.pos : p);
        if (stripesTileNew == null) {
            // Odd.
            // Maybe it would be better to crash?
            InventoryUtil.dropAll(w, p, stacksToSendBack);
            return;
        }
        if (!canceled) {
            stripesTileNew.readFromNBT(stripesNBTOld);
            stripesTileNew.onLoad();
        }

        IPipeHolder stripesPipeHolderNew = CapUtil.getCapability(stripesTileNew, PipeApi.CAP_PIPE_HOLDER, null);
        if (stripesPipeHolderNew != null) {
            if (!canceled) {
                IWireManager wireManager = stripesPipeHolderNew.getWireManager();
                if (wireManager instanceof WireManager) {
                    ((WireManager) wireManager).getWireSystems().rebuildWireSystemsAround(stripesPipeHolderNew);
                }
            }

            PipeBehaviour behaviour = stripesPipeHolderNew.getPipe().getBehaviour();
            if (behaviour instanceof IStripesActivator) {
                IStripesActivator stripesNew = (IStripesActivator) behaviour;
                for (ItemStack s : stacksToSendBack) {
                    s = s.copy();
                    if (!stripesNew.sendItem(s, r.dir)) {
                        stripesNew.dropItem(s, r.dir);
                    }
                }
            } else {
                InventoryUtil.dropAll(w, p, stacksToSendBack);
            }
        } else {
            InventoryUtil.dropAll(w, p, stacksToSendBack);
        }
    }

    private boolean isValidRetractionPath(World w, PipeExtensionRequest r, EnumFacing retractDir) {
        TileEntity tile = w.getTileEntity(r.pos.offset(retractDir));
        IPipe pipe = CapUtil.getCapability(tile, PipeApi.CAP_PIPE, null);
        if (pipe != null) {
            boolean connected = false;
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (pipe.getConnectedType(facing) == IPipe.ConnectedType.TILE) {
                    return false;
                }
                if (facing == retractDir.getOpposite() && pipe.getConnectedType(facing) != IPipe.ConnectedType.PIPE) {
                    return false;
                }
                if (facing != retractDir.getOpposite() && connected && pipe.getConnectedType(facing) != null) {
                    return false;
                }
                if (facing != retractDir.getOpposite() && !connected && pipe.getConnectedType(facing) != null) {
                    connected = true;
                }

            }
            return true;
        }
        return false;
    }

    private class PipeExtensionRequest {
        public final BlockPos pos;
        public final EnumFacing dir;
        public final IStripesActivator stripes;
        public final PipeDefinition pipeDef;
        public final ItemStack stack;

        private PipeExtensionRequest(BlockPos pos, EnumFacing dir, IStripesActivator stripes, PipeDefinition pipeDef, ItemStack stack) {
            this.pos = pos;
            this.dir = dir;
            this.stripes = stripes;
            this.pipeDef = pipeDef;
            this.stack = stack;
        }
    }
}
