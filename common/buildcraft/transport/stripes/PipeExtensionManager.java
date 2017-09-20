/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.SoundUtil;

import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.wire.WireManager;

public enum PipeExtensionManager implements IPipeExtensionManager {
    INSTANCE;

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

    private final Map<Integer, Set<PipeExtensionRequest>> requests = new HashMap<>();

    @Override
    public boolean requestPipeExtension(World world, BlockPos pos, EnumFacing dir, IStripesActivator stripes, ItemStack stack) {
        if (world.isRemote || stack.isEmpty() || !(stack.getItem() instanceof IItemPipe)) {
            return false;
        }

        return requests.computeIfAbsent(world.provider.getDimension(), i -> new LinkedHashSet<>())
            .add(new PipeExtensionRequest(pos, dir, stripes, ((IItemPipe) stack.getItem()).getDefinition(), stack.copy()));
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && requests.containsKey(event.world.provider.getDimension())) {
            Set<PipeExtensionRequest> rSet = requests.get(event.world.provider.getDimension());
            World w = event.world;
            for (PipeExtensionRequest r : rSet) {
                BlockPos p = r.pos;

                boolean retract = r.pipeDef == BCTransportPipes.voidItem;
                NonNullList<ItemStack> stacksToSendBack = NonNullList.create();

                if (retract) {
                    p = p.offset(r.dir.getOpposite());
                    if (w.getBlockState(p).getBlock() != BCTransportBlocks.pipeHolder ||
                        w.getBlockState(p.offset(r.dir.getOpposite())).getBlock() != BCTransportBlocks.pipeHolder) {
                        r.stripes.sendItem(r.stack.copy(), r.dir);
                        continue;
                    }
                } else {
                    p = p.offset(r.dir);
                    if (!w.isAirBlock(p) && !w.getBlockState(p).getBlock().isReplaceable(w, p)) {
                        r.stripes.sendItem(r.stack.copy(), r.dir);
                        continue;
                    }
                }

                // Step 1: Copy over and remove existing stripes pipe
                IBlockState stripesStateOld = w.getBlockState(r.pos);
                NBTTagCompound stripesNBTOld = new NBTTagCompound();
                TileEntity stripesTileOld = w.getTileEntity(r.pos);
                if (!stripesTileOld.hasCapability(PipeApi.CAP_PIPE_HOLDER, null)) {
                    BCLog.logger.warn("Found an invalid request at " + r.pos + " as " + stripesTileOld + " was not a pipe tile!");
                    continue;
                }
                GameProfile owner = stripesTileOld.getCapability(PipeApi.CAP_PIPE_HOLDER, null).getOwner();
                stripesTileOld.writeToNBT(stripesNBTOld);
                w.setBlockToAir(r.pos);

                // Step 2: If retracting, remove previous pipe; if extending, add new pipe
                if (retract) {
                    IBlockState state = w.getBlockState(p);
                    List<ItemStack> list = state.getBlock().getDrops(w, p, state, 0);

                    SoundUtil.playBlockBreak(w, p, state);
                    w.setBlockToAir(p);

                    stacksToSendBack.add(r.stack);
                    if (list != null) {
                        stacksToSendBack.addAll(list);
                    }
                } else {
                    FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) w, owner, r.pos);
                    player.inventory.clear();
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, r.stack);
                    BlockUtil.useItemOnBlock(w, player, r.stack, r.pos, r.dir.getOpposite());
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        ItemStack stack = player.inventory.removeStackFromSlot(i);
                        if (!stack.isEmpty()) {
                            stacksToSendBack.add(stack);
                        }
                    }
                }

                // Step 3: Place stripes pipe back
                // - Correct NBT coordinates
                stripesNBTOld.setInteger("x", p.getX());
                stripesNBTOld.setInteger("y", p.getY());
                stripesNBTOld.setInteger("z", p.getZ());
                // - Create block and tile
                w.setBlockState(p, stripesStateOld, 3);

                TileEntity stripesTileNew = w.getTileEntity(p);
                stripesTileNew.readFromNBT(stripesNBTOld);
                stripesTileNew.onLoad();

                if (stripesTileNew.hasCapability(PipeApi.CAP_PIPE_HOLDER, null)) {
                    IPipeHolder stripesPipeHolderNew = stripesTileNew.getCapability(PipeApi.CAP_PIPE_HOLDER, null);
                    IWireManager wireManager = stripesPipeHolderNew.getWireManager();
                    if (wireManager instanceof WireManager) {
                        ((WireManager) wireManager).getWireSystems().rebuildWireSystemsAround(stripesPipeHolderNew);
                    }

                    // Step 4: Hope for the best, clean up.
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
            rSet.clear();
        }
    }
}
