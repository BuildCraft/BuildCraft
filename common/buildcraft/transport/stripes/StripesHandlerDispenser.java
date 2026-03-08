/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public enum StripesHandlerDispenser implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Class<? extends Item>> ITEM_CLASSES = new ArrayList<>();

    public static class Source implements IBlockSource {
        private final ServerWorld world;
        private final BlockPos pos;
        private final Direction side;

        public Source(ServerWorld world, BlockPos pos, Direction side) {
            this.world = world;
            this.pos = pos;
            this.side = side;
        }

        @Override
//        public double getX()
        public double x() {
            return pos.getX() + 0.5D;
        }

        @Override
//        public double getY()
        public double y() {
            return pos.getY() + 0.5D;
        }

        @Override
//        public double getZ()
        public double z() {
            return pos.getZ() + 0.5D;
        }

        @Override
//        public BlockPos getBlockPos()
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public BlockState getBlockState() {
            return Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, side);
        }

        @SuppressWarnings("unchecked")
        @Override
//        public <T extends TileEntity> T getBlockTileEntity()
        public <T extends TileEntity> T getEntity() {
            return (T) world.getBlockEntity(pos);
        }

        @Override
//        public World getWorld()
        public ServerWorld getLevel() {
            return world;
        }
    }

    private static boolean shouldHandle(ItemStack stack) {
        if (ITEMS.contains(stack.getItem())) {
            return true;
        }

        Class<?> c = stack.getItem().getClass();
        while (c != Item.class) {
            if (ITEMS.contains(c)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {
//        if (!BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.containsKey(stack.getItem()))
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(stack.getItem())) {
            return false;
        }
//        IBehaviorDispenseItem behaviour = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem());
        IDispenseItemBehavior behaviour = DispenserBlock.DISPENSER_REGISTRY.get(stack.getItem());
        // Temp: for testing
        // if (!shouldHandle(stack)) {
        // return false;
        // }

        IBlockSource source = new Source((ServerWorld) world, pos, direction);
        ItemStack output = behaviour.dispense(source, stack.copy());
//        player.getInventory().setInventorySlotContents(player.inventory.currentItem, output);
        player.inventory.setItem(player.inventory.selected, output);
        return true;
    }
}
