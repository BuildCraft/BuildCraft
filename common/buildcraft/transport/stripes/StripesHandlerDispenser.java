/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerDispenser implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Class<? extends Item>> ITEM_CLASSES = new ArrayList<>();

    public static class Source implements IBlockSource {
        private final World world;
        private final BlockPos pos;
        private final EnumFacing side;

        public Source(World world, BlockPos pos, EnumFacing side) {
            this.world = world;
            this.pos = pos;
            this.side = side;
        }

        @Override
        public double getX() {
            return pos.getX() + 0.5D;
        }

        @Override
        public double getY() {
            return pos.getY() + 0.5D;
        }

        @Override
        public double getZ() {
            return pos.getZ() + 0.5D;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public IBlockState getBlockState() {
            return Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, side);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TileEntity> T getBlockTileEntity() {
            return (T) world.getTileEntity(pos);
        }

        @Override
        public World getWorld() {
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
                          EnumFacing direction,
                          ItemStack stack,
                          EntityPlayer player,
                          IStripesActivator activator) {
        if (!BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.containsKey(stack.getItem())) {
            return false;
        }
        IBehaviorDispenseItem behaviour = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem());
        // Temp: for testing
        // if (!shouldHandle(stack)) {
        // return false;
        // }

        IBlockSource source = new Source(world, pos, direction);
        ItemStack output = behaviour.dispense(source, stack.copy());
        player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
        return true;
    }
}
