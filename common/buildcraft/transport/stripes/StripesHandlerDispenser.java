/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public enum StripesHandlerDispenser implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Class<? extends Item>> ITEM_CLASSES = new ArrayList<>();

    public static class Source implements BlockSource {
        private final ServerLevel world;
        private final BlockPos pos;
        private final Direction side;

        public Source(ServerLevel world, BlockPos pos, Direction side) {
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
//        public <T extends BlockEntity> T getBlockTileEntity()
        public <T extends BlockEntity> T getEntity() {
            return (T) world.getBlockEntity(pos);
        }

        @Override
//        public Level getWorld()
        public ServerLevel getLevel() {
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
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
//        if (!DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.containsKey(stack.getItem()))
        if (DispenserBlock.DISPENSER_REGISTRY.containsKey(stack.getItem())) {
            return false;
        }
//        IBehaviorDispenseItem behaviour = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem());
        DispenseItemBehavior behaviour = DispenserBlock.DISPENSER_REGISTRY.get(stack.getItem());
        // Temp: for testing
        // if (!shouldHandle(stack)) {
        // return false;
        // }

        BlockSource source = new Source((ServerLevel) world, pos, direction);
        ItemStack output = behaviour.dispense(source, stack.copy());
//        player.getInventory().setInventorySlotContents(player.inventory.currentItem, output);
        player.getInventory().setItem(player.getInventory().selected, output);
        return true;
    }
}
