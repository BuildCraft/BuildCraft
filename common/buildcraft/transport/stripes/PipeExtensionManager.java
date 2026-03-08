/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.*;
import buildcraft.lib.misc.*;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.wire.WireManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public enum PipeExtensionManager implements IPipeExtensionManager {
    INSTANCE;

    // private final Int2ObjectOpenHashMap<List<PipeExtensionRequest>> requests = new Int2ObjectOpenHashMap<>();
    private final HashMap<String, List<PipeExtensionRequest>> requests = new HashMap<>();
    private final Set<PipeDefinition> retractionPipeDefs = new HashSet<>();

    @Override
    public boolean requestPipeExtension(World world, BlockPos pos, Direction dir, IStripesActivator stripes, ItemStack stack) {
        if (world.isClientSide || stack.isEmpty() || !(stack.getItem() instanceof IItemPipe)) {
            return false;
        }

//        int id = world.provider.getDimension();
        String id = world.dimension().location().toString();
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
//        if (event.phase != Phase.END || event.side != Side.SERVER)
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
//        List<PipeExtensionRequest> rList = requests.get(event.world.provider.getDimension());
        List<PipeExtensionRequest> rList = requests.get(event.world.dimension().location().toString());
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
        Direction retractDir = r.dir.getOpposite();
        if (!isValidRetractionPath(w, r, retractDir)) {

            // check other directions
            List<Direction> possible = new ArrayList<>();
            for (Direction facing : Direction.values()) {
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
//            retractDir = possible.get(MathHelper.getInt(w.random, 0, possible.size() - 1));
            retractDir = possible.get(MathHelper.nextInt(w.random, 0, possible.size() - 1));
        }
        BlockPos p = r.pos.relative(retractDir);

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();
        // Always send back catalyst pipe
        stacksToSendBack.add(r.stack);

        // Step 1: Copy over existing stripes pipe
//        BlockSnapshot blockSnapshot1 = BlockSnapshot.getBlockSnapshot(w, r.pos);
        BlockSnapshot blockSnapshot1 = BlockSnapshot.create(w.dimension(), w, r.pos);
        BlockState stripesStateOld = w.getBlockState(r.pos);
        TileEntity stripesTileOld = w.getBlockEntity(r.pos);
        final GameProfile owner;
        // Fetch owner
        {
            IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null).orElse(null);
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

        CompoundNBT stripesNBTOld = new CompoundNBT();
//        stripesTileOld.writeToNBT(stripesNBTOld);
        stripesTileOld.save(stripesNBTOld);

        // Step 2: Remove previous pipe
//        BlockSnapshot blockSnapshot2 = BlockSnapshot.getBlockSnapshot(w, p);
        BlockSnapshot blockSnapshot2 = BlockSnapshot.create(w.dimension(), w, p);
        NonNullList<ItemStack> list = NonNullList.create();
        boolean canceled = !BlockUtil.breakBlock((ServerWorld) w, p, list, r.pos, owner);
        if (canceled) {
            blockSnapshot2.restore(true);
            TileEntity tile = w.getBlockEntity(p);
            if (tile != null) {
                tile.onLoad();
            }
        }

        // Step 3: Place stripes pipe back and remove old one
        if (!canceled) {
            // - Correct NBT coordinates
            stripesNBTOld.putInt("x", p.getX());
            stripesNBTOld.putInt("y", p.getY());
            stripesNBTOld.putInt("z", p.getZ());

            // - Create block and tile
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerWorld) w, owner, p);
            player.inventory.clearContent();
            w.setBlock(p, stripesStateOld, BlockConstants.UPDATE_ALL);
//            BlockEvent.PlaceEvent placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot2, r.dir, Hand.MAIN_HAND);
            canceled = ForgeEventFactory.onBlockPlace(player, blockSnapshot2, r.dir.getOpposite());
//            if (canceled = placeEvent.isCanceled())
            if (canceled) {
                blockSnapshot2.restore(true);
                TileEntity tile = w.getBlockEntity(r.pos);
                if (tile != null) {
                    tile.onLoad();
                }
            } else {
                SoundUtil.playBlockBreak(w, p, blockSnapshot2.getReplacedBlock());

                canceled = !BlockUtil.breakBlock((ServerWorld) w, r.pos, NonNullList.create(), r.pos, owner);
                if (canceled) {
                    blockSnapshot1.restore(true);
                    TileEntity tile1 = w.getBlockEntity(r.pos);
                    if (tile1 != null) {
                        tile1.onLoad();
                    }

                    blockSnapshot2.restore(true);
                    TileEntity tile2 = w.getBlockEntity(p);
                    if (tile2 != null) {
                        tile2.onLoad();
                    }
                } else {
                    stacksToSendBack.addAll(list);
                    for (int i = 0; i < player.inventory.getContainerSize(); i++) {
//                        ItemStack stack = player.getInventory().removeStackFromSlot(i);
                        ItemStack stack = player.inventory.removeItem(i, player.inventory.getItem(i).getCount());
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
        BlockPos p = r.pos.relative(r.dir);
//        if (!w.isAirBlock(p) && !w.getBlockState(p).getBlock().isReplaceable(w, p))
        if (!w.isEmptyBlock(p) && !w.getBlockState(p).getBlock().canBeReplaced(
                w.getBlockState(p),
                new BlockItemUseContext(
                        w,
                        null,
                        Hand.MAIN_HAND,
                        StackUtil.EMPTY,
                        BlockRayTraceResult.miss(Vector3d.ZERO, r.dir, p)
                )
        )
        )
        {
            r.stripes.sendItem(r.stack.copy(), r.dir);
            return;
        }

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();

        // Step 1: Copy over and remove existing stripes pipe
        BlockState stripesStateOld = w.getBlockState(r.pos);
        CompoundNBT stripesNBTOld = new CompoundNBT();
        TileEntity stripesTileOld = w.getBlockEntity(r.pos);
        final GameProfile owner;
        // Fetch owner
        {
            IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null).orElse(null);
            if (stripesTileOld == null || holder == null) {
                BCLog.logger.warn("Found an invalid request at " + r.pos + " as " + stripesTileOld + " was not a pipe tile!");
                return;
            }
            owner = holder.getOwner();
        }

//        stripesTileOld.writeToNBT(stripesNBTOld);
        stripesNBTOld = stripesTileOld.save(stripesNBTOld);
//        BlockSnapshot blockSnapshot1 = BlockSnapshot.getBlockSnapshot(w, r.pos);
        BlockSnapshot blockSnapshot1 = BlockSnapshot.create(w.dimension(), w, r.pos);
        boolean canceled = !BlockUtil.breakBlock((ServerWorld) w, r.pos, NonNullList.create(), r.pos, owner);
        if (canceled) {
            stacksToSendBack.add(r.stack);

            blockSnapshot1.restore(true);
            TileEntity tile = w.getBlockEntity(r.pos);
            if (tile != null) {
                tile.onLoad();
            }
        }

        NonNullList<ItemStack> list = NonNullList.create();

        // Step 2: Add new pipe
        if (!canceled) {
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerWorld) w, owner, r.pos);
            player.inventory.clearContent();
            player.inventory.setItem(player.inventory.selected, r.stack);
//            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(r.stack, player, w, r.pos, r.dir.getOpposite(), 0.5F, 0.5F, 0.5F, Hand.MAIN_HAND);
            ActionResultType result = ForgeHooks.onPlaceItemIntoWorld(
                    new ItemUseContext(w,
                            player,
                            Hand.MAIN_HAND,
                            r.stack,
                            new BlockRayTraceResult(
                                    new Vector3d(0.5F, 0.5F, 0.5F),
                                    r.dir.getOpposite(),
                                    r.pos,
                                    false
                            )
                    )
            );
            for (int i = 0; i < player.inventory.getContainerSize(); i++) {
//                ItemStack stack = player.inventory.removeStackFromSlot(i);
                ItemStack stack = player.inventory.removeItem(i, player.inventory.getItem(i).getCount());
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
//            if (canceled = result != ActionResultType.SUCCESS)
            if (canceled = (!result.consumesAction())) {
                blockSnapshot1.restore(true);
                TileEntity tile = w.getBlockEntity(r.pos);
                if (tile != null) {
                    tile.onLoad();
                }
            }
        }

        // Step 3: Place stripes pipe back
        if (!canceled) {
            // - Correct NBT coordinates
            stripesNBTOld.putInt("x", p.getX());
            stripesNBTOld.putInt("y", p.getY());
            stripesNBTOld.putInt("z", p.getZ());

            // - Create block and tile
            FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerWorld) w, owner, p);
//            player.inventory.clear();
            player.inventory.clearContent();
//            BlockSnapshot blockSnapshot2 = BlockSnapshot.getBlockSnapshot(w, p);
            BlockSnapshot blockSnapshot2 = BlockSnapshot.create(w.dimension(), w, p);
            w.setBlock(p, stripesStateOld, BlockConstants.UPDATE_ALL);
//            BlockEvent.PlaceEvent placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot2, r.dir.getOpposite(), Hand.MAIN_HAND);
            canceled = ForgeEventFactory.onBlockPlace(player, blockSnapshot2, r.dir.getOpposite());
            if (canceled) {
                stacksToSendBack.add(r.stack);

                blockSnapshot1.restore(true);
                TileEntity tile = w.getBlockEntity(r.pos);
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

    private void cleanup(World w, PipeExtensionRequest r, BlockPos p, NonNullList<ItemStack> stacksToSendBack, boolean canceled, CompoundNBT stripesNBTOld) {
        TileEntity stripesTileNew = w.getBlockEntity(canceled ? r.pos : p);
        if (stripesTileNew == null) {
            // Odd.
            // Maybe it would be better to crash?
            InventoryUtil.dropAll(w, p, stacksToSendBack);
            return;
        }
        if (!canceled) {
            stripesTileNew.load(stripesTileNew.getBlockState(), stripesNBTOld);
            stripesTileNew.onLoad();
        }

        IPipeHolder stripesPipeHolderNew = CapUtil.getCapability(stripesTileNew, PipeApi.CAP_PIPE_HOLDER, null).orElse(null);
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

    private boolean isValidRetractionPath(World w, PipeExtensionRequest r, Direction retractDir) {
        TileEntity tile = w.getBlockEntity(r.pos.relative(retractDir));
        IPipe pipe = CapUtil.getCapability(tile, PipeApi.CAP_PIPE, null).orElse(null);
        if (pipe != null) {
            boolean connected = false;
            for (Direction facing : Direction.values()) {
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
        public final Direction dir;
        public final IStripesActivator stripes;
        public final PipeDefinition pipeDef;
        public final ItemStack stack;

        private PipeExtensionRequest(BlockPos pos, Direction dir, IStripesActivator stripes, PipeDefinition pipeDef, ItemStack stack) {
            this.pos = pos;
            this.dir = dir;
            this.stripes = stripes;
            this.pipeDef = pipeDef;
            this.stack = stack;
        }
    }
}
