/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.lib.inventory.InventoryWrapper;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.snapshot.SchematicBlockManager;

public class ItemSchematicSingle extends ItemBC_Neptune {
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return stack.getItemDamage() == DAMAGE_CLEAN ? 16 : super.getItemStackLimit(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, DAMAGE_CLEAN, "clean");
        addVariant(variants, DAMAGE_USED, "used");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = StackUtil.asNonNull(player.getHeldItem(hand));
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            NBTTagCompound itemData = NBTUtilBC.getItemData(stack);
            itemData.removeTag(NBT_KEY);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            NBTTagCompound itemData = NBTUtilBC.getItemData(StackUtil.asNonNull(stack));
            itemData.removeTag(NBT_KEY);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return EnumActionResult.SUCCESS;
        }
        int damage = stack.getItemDamage();
        if (damage != DAMAGE_USED) {
            IBlockState state = world.getBlockState(pos);
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(world, pos, pos, state, state.getBlock());
            if (schematicBlock.isAir()) {
                return EnumActionResult.FAIL;
            }
            NBTUtilBC.getItemData(stack).setTag(NBT_KEY, SchematicBlockManager.writeToNBT(schematicBlock));
            stack.setItemDamage(DAMAGE_USED);
            return EnumActionResult.SUCCESS;
        } else {
            BlockPos placePos = pos;
            boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
            if (!replaceable) {
                placePos = placePos.offset(side);
            }
            if (!world.mayPlace(world.getBlockState(pos).getBlock(), placePos, false, side, null)) {
                return EnumActionResult.FAIL;
            }
            if (replaceable && !world.isAirBlock(placePos)) {
                world.setBlockToAir(placePos);
            }
            try {
                ISchematicBlock schematicBlock = getSchematic(stack);
                if (schematicBlock != null) {
                    if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                        List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids(null);
                        List<ItemStack> requiredItems = schematicBlock.computeRequiredItems(null);
                        if (requiredFluids.isEmpty()) {
                            InventoryWrapper itemTransactor = new InventoryWrapper(player.inventory);
                            if (StackUtil.mergeSameItems(requiredItems).stream().noneMatch(s ->
                                itemTransactor.extract(
                                    extracted -> StackUtil.canMerge(s, extracted),
                                    s.getCount(),
                                    s.getCount(),
                                    true
                                ).isEmpty()
                            )) {
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
                                    player.swingArm(hand);
                                    return EnumActionResult.SUCCESS;
                                }
                            } else {
                                player.sendStatusMessage(
                                    new TextComponentString(
                                        "Not enough items. Total needed: " +
                                            StackUtil.mergeSameItems(requiredItems).stream()
                                                .map(s -> s.getTextComponent().getFormattedText() + " x " + s.getCount())
                                                .collect(Collectors.joining(", "))
                                    ),
                                    true
                                );
                            }
                        } else {
                            player.sendStatusMessage(
                                new TextComponentString("Schematic requires fluids"),
                                true
                            );
                        }
                    }
                }
            } catch (InvalidInputDataException e) {
                player.sendStatusMessage(
                    new TextComponentString("Invalid schematic: " + e.getMessage()),
                    true
                );
                e.printStackTrace();
            }
            return EnumActionResult.FAIL;
        }
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            return SchematicBlockManager.readFromNBT(NBTUtilBC.getItemData(stack).getCompoundTag(NBT_KEY));
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            BCLog.logger.warn("Invalid schematic " + e.getMessage());
            return null;
        }
    }
}
