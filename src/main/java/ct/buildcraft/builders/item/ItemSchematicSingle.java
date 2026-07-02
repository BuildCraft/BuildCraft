/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.builders.snapshot.SchematicBlockManager;
import ct.buildcraft.lib.inventory.InventoryWrapper;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ItemSchematicSingle extends Item {
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(Item.Properties prop) {
        super(prop);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return isUsed(stack) ? super.getMaxStackSize(stack) : 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.asNonNull(player.getItemInHand(hand));
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (player.isCrouching()) {
            clear(stack);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isCrouching()) {
            clear(stack);
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (!isUsed(stack)) {
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                world,
                pos,
                pos,
                state,
                state.getBlock()
            ));
            if (schematicBlock.isAir()) {
                return InteractionResult.FAIL;
            }
            return recordSingleSchematic(stack, player, schematicBlock);
        }

        Direction side = context.getClickedFace();
        BlockPos placePos = pos;
        boolean replaceable = state.canBeReplaced(new BlockPlaceContext(context));
        if (!replaceable) {
            placePos = placePos.relative(side);
        }
        if (!world.getWorldBorder().isWithinBounds(placePos)) {
            return InteractionResult.FAIL;
        }
        if (replaceable && !world.isEmptyBlock(placePos) && !world.setBlockAndUpdate(placePos, Blocks.AIR.defaultBlockState())) {
            return InteractionResult.FAIL;
        }

        try {
            ISchematicBlock schematicBlock = getSchematic(stack);
            if (schematicBlock != null) {
                if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                    List<ItemStack> requiredItems = schematicBlock.computeRequiredItems(world);
                    List<FluidStack> requiredFluids = new ArrayList<>();
                    requiredItems.stream().map(FluidUtil::getFluidHandler)
                        .map(opt -> opt.lazyMap(fluidHandler -> fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE)))
                        .filter(LazyOptional::isPresent).map(opt -> opt.orElse(FluidStack.EMPTY)).forEach(requiredFluids::add);
                    requiredFluids.addAll(schematicBlock.computeRequiredFluids(world));

                    if (requiredFluids.isEmpty()) {
                        List<ItemStack> mergedItems = StackUtil.mergeSameItems(requiredItems);
                        InventoryWrapper itemTransactor = new InventoryWrapper(player.getInventory());
                        boolean hasItems = player.isCreative() || mergedItems.stream().noneMatch(s ->
                            itemTransactor.extract(
                                extracted -> StackUtil.canMerge(s, extracted),
                                s.getCount(),
                                s.getCount(),
                                true
                            ).isEmpty()
                        );
                        if (hasItems) {
                            if (schematicBlock.build(world, placePos)) {
                                if (!player.isCreative()) {
                                    mergedItems.forEach(s ->
                                        itemTransactor.extract(
                                            extracted -> StackUtil.canMerge(s, extracted),
                                            s.getCount(),
                                            s.getCount(),
                                            false
                                        )
                                    );
                                }
                                SoundUtil.playBlockPlace(world, placePos);
                                player.swing(context.getHand());
                                return InteractionResult.SUCCESS;
                            }
                        } else {
                            player.displayClientMessage(
                                Component.translatable(
                                    "item.buildcraftbuilders.schematic_single.not_enough_items",
                                    formatItemList(mergedItems)
                                ),
                                true
                            );
                        }
                    } else {
                        player.displayClientMessage(
                            Component.translatable("item.buildcraftbuilders.schematic_single.requires_fluids"),
                            true
                        );
                    }
                }
            }
        } catch (InvalidInputDataException e) {
            player.displayClientMessage(
                Component.translatable("item.buildcraftbuilders.schematic_single.invalid_with_reason", e.getMessage()),
                true
            );
            BCLog.logger.warn("Invalid single block schematic", e);
        }
        return InteractionResult.FAIL;
    }

    private static InteractionResult recordSingleSchematic(ItemStack stack, Player player, ISchematicBlock schematicBlock) {
        if (stack.getCount() <= 1) {
            writeSchematic(stack, schematicBlock);
            return InteractionResult.SUCCESS;
        }

        ItemStack recorded = stack.copy();
        recorded.setCount(1);
        writeSchematic(recorded, schematicBlock);
        stack.shrink(1);

        if (!player.getInventory().add(recorded)) {
            player.drop(recorded, false);
        }
        return InteractionResult.SUCCESS;
    }

    private static void writeSchematic(ItemStack stack, ISchematicBlock schematicBlock) {
        NBTUtilBC.getItemData(stack).put(NBT_KEY, SchematicBlockManager.writeToNBT(schematicBlock));
        stack.setDamageValue(DAMAGE_USED);
        stack.setCount(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (!isUsed(stack)) {
            tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.blank").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.blank_hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        ISchematicBlock schematic = getSchematicSafe(stack);
        if (schematic == null) {
            tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.invalid").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.used").withStyle(ChatFormatting.GRAY));
        if (world != null) {
            try {
                List<ItemStack> items = StackUtil.mergeSameItems(schematic.computeRequiredItems(world));
                if (!items.isEmpty()) {
                    tooltip.add(Component.translatable(
                        "item.buildcraftbuilders.schematic_single.contains",
                        formatItemList(items)
                    ).withStyle(ChatFormatting.DARK_GRAY));
                }
            } catch (RuntimeException ignored) {
                tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.invalid").withStyle(ChatFormatting.RED));
            }
        }
        tooltip.add(Component.translatable("item.buildcraftbuilders.schematic_single.clear_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean isUsed(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemSchematicSingle && stack.getDamageValue() == DAMAGE_USED;
    }

    public static boolean isValidUsed(@Nonnull ItemStack stack) {
        return isUsed(stack) && getSchematicSilently(stack) != null;
    }

    public static void clear(@Nonnull ItemStack stack) {
        CompoundTag itemData = stack.getTag();
        if (itemData != null) {
            itemData.remove(NBT_KEY);
            if (itemData.isEmpty()) {
                stack.setTag(null);
            }
        }
        stack.setDamageValue(DAMAGE_CLEAN);
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
                return null;
            }
            return SchematicBlockManager.readFromNBT(tag.getCompound(NBT_KEY));
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        return getSchematicSilently(stack);
    }

    private static ISchematicBlock getSchematicSilently(@Nonnull ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            return null;
        }
    }

    private static String formatItemList(List<ItemStack> items) {
        return items.stream()
            .filter(item -> !item.isEmpty())
            .map(item -> item.getHoverName().getString() + " x " + item.getCount())
            .collect(Collectors.joining(", "));
    }
}
