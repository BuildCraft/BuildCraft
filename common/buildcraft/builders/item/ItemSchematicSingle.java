/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.lib.inventory.InventoryWrapper;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.WorldUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemSchematicSingle extends ItemBC_Neptune {
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setHasSubtypes(true);
//        setMaxStackSize(1); // Calen: moved to properties
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
//        return stack.getItemDamage() == DAMAGE_CLEAN ? 16 : super.getItemStackLimit(stack);
        return stack.getDamageValue() == DAMAGE_CLEAN ? 16 : super.getItemStackLimit(stack);
    }

    // Calen: not still useful in 1.18.2
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants)
//    {
//        addVariant(variants, DAMAGE_CLEAN, "clean");
//        addVariant(variants, DAMAGE_USED, "used");
//    }

    @Override
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = StackUtil.asNonNull(player.getItemInHand(hand));
        if (world.isClientSide) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
//        if (player.isSneaking())
        if (player.isShiftKeyDown()) {
            CompoundNBT itemData = NBTUtilBC.getItemData(stack);
            itemData.remove(NBT_KEY);
            if (itemData.isEmpty()) {
                stack.setTag(null);
            }
//            stack.setItemDamage(DAMAGE_CLEAN);
            stack.setDamageValue(DAMAGE_CLEAN);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
//    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand)
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Hand hand = ctx.getHand();
        Direction side = ctx.getClickedFace();
        Vector3d vec3Pos = ctx.getClickLocation();
        if (world.isClientSide) {
            return ActionResultType.PASS;
        }
//        ItemStack stack = player.getHeldItem(hand);
//        if (player.isSneaking())
        if (player.isShiftKeyDown()) {
            CompoundNBT itemData = NBTUtilBC.getItemData(StackUtil.asNonNull(stack));
            itemData.remove(NBT_KEY);
            if (itemData.isEmpty()) {
                stack.setTag(null);
            }
            stack.setDamageValue(DAMAGE_CLEAN);
            return ActionResultType.SUCCESS;
        }
        int damage = stack.getDamageValue();
        if (damage != DAMAGE_USED) {
            BlockState state = world.getBlockState(pos);
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                    world,
                    pos,
                    pos,
                    state,
                    state.getBlock()
            ));
            if (schematicBlock.isAir()) {
                return ActionResultType.FAIL;
            }
            NBTUtilBC.getItemData(stack).put(NBT_KEY, SchematicBlockManager.writeToNBT(schematicBlock));
            stack.setDamageValue(DAMAGE_USED);
            return ActionResultType.SUCCESS;
        } else {
            BlockPos placePos = pos;
//            boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
            boolean replaceable = world.getBlockState(pos).getMaterial().isReplaceable();
//            boolean replaceable = world.getBlockState(pos).canBeReplaced(new BlockItemUseContext(world,player,hand,stack, BlockRayTraceResult.miss()));
            if (!replaceable) {
                placePos = placePos.relative(side);
            }
//            if (!world.mayPlace(world.getBlockState(pos).getBlock(), placePos, false, side, null))
            if (!WorldUtil.mayPlace(world, world.getBlockState(pos).getBlock(), placePos, false, side, null)) {
                return ActionResultType.FAIL;
            }
            if (replaceable && !world.isEmptyBlock(placePos)) {
//                world.setBlockToAir(placePos);
                world.setBlock(placePos, Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL);
            }
            try {
                ISchematicBlock schematicBlock = getSchematic(stack);
                if (schematicBlock != null) {
                    if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                        List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids();
                        List<ItemStack> requiredItems = schematicBlock.computeRequiredItems();
                        if (requiredFluids.isEmpty()) {
//                            InventoryWrapper itemTransactor = new InventoryWrapper(player.inventory);
                            InventoryWrapper itemTransactor = new InventoryWrapper(player.inventory);
                            if (StackUtil.mergeSameItems(requiredItems).stream().noneMatch(s ->
                                    itemTransactor.extract(
                                            extracted -> StackUtil.canMerge(s, extracted),
                                            s.getCount(),
                                            s.getCount(),
                                            true
                                    ).isEmpty()
                            ))
                            {
                                if (schematicBlock.build(world, placePos)) {
                                    StackUtil.mergeSameItems(requiredItems).forEach(s ->
                                            itemTransactor.extract(
                                                    extracted -> StackUtil.canMerge(s, extracted),
                                                    s.getCount(),
                                                    s.getCount(),
                                                    false
                                            )
                                    );
                                    SoundUtil.playBlockPlace(world, placePos);
//                                    player.swingArm(hand);
                                    player.swing(hand);
                                    return ActionResultType.SUCCESS;
                                }
                            } else {
//                                player.sendStatusMessage(
//                                        new TextComponentString(
//                                                "Not enough items. Total needed: " +
//                                                        StackUtil.mergeSameItems(requiredItems).stream()
//                                                                .map(s -> s.getTextComponent().getFormattedText() + " x " + s.getCount())
//                                                                .collect(Collectors.joining(", "))
//                                        ),
//                                        true
//                                );
                                IFormattableTextComponent message = new TranslationTextComponent("chat.buildcraft.schematic_single.not_enough_item").append("\n");
                                List<IFormattableTextComponent> requiredItemNames = Lists.newArrayList(
                                        StackUtil.mergeSameItems(requiredItems).stream()
                                                .map(s -> new StringTextComponent("    ")
                                                        .append(s.getDisplayName())
                                                        .append(" x " + s.getCount())
                                                )
                                                .toArray(IFormattableTextComponent[]::new)
                                );
                                for (int index = 0; index < requiredItemNames.size(); index++) {
                                    message.append(requiredItemNames.get(index));
                                    if (index != requiredItemNames.size() - 1) {
                                        message.append("\n");
                                    }
                                }
                                player.sendMessage(message, Util.NIL_UUID);
                            }
                        } else {
//                            player.sendStatusMessage(
//                                    new TextComponentString("Schematic requires fluids"),
//                                    true
//                            );
                            player.sendMessage(
                                    new TranslationTextComponent("chat.buildcraft.schematic_single.require_fluid"),
                                    Util.NIL_UUID
                            );
                        }
                    }
                }
            } catch (InvalidInputDataException e) {
//                player.sendStatusMessage(
//                        new TextComponentString("Invalid schematic: " + e.getMessage()),
//                        true
//                );
                player.sendMessage(
                        new TranslationTextComponent("chat.buildcraft.schematic_single.invalid").append(e.getMessage()),
                        Util.NIL_UUID
                );
                e.printStackTrace();
                BCLog.logger.warn("[builders.schematic_single] Invalid schematic ", e);
            }
            return ActionResultType.FAIL;
        }
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            return SchematicBlockManager.readFromNBT(NBTUtilBC.getItemData(stack).getCompound(NBT_KEY));
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        // Calen FIX: when mouse hovers on unused schematic, #getSchematic will cause InvalidInputDataException
        if ((!stack.hasTag()) || !(stack.getTag().contains("name"))) {
            return null;
        }
        // BC 1.12.2
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
//            BCLog.logger.warn("Invalid schematic " + e.getMessage());
            BCLog.logger.warn("[builders.schematic_single] Invalid schematic " + e.getMessage());
            return null;
        }
    }
}
