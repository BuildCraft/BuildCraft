/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.api.core.InvalidInputDataException;

import gnu.trove.map.hash.TIntObjectHashMap;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSchematicSingle extends ItemBC_Neptune {
    private static final int DAMAGE_CLEAN = 0;
    private static final int DAMAGE_USED = 1;

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
            itemData.removeTag("schematic");
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
            itemData.removeTag("schematic");
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return EnumActionResult.SUCCESS;
        }
        int damage = stack.getItemDamage();
        if (damage != DAMAGE_USED) {
            ISchematicBlock<?> schematicBlock = SchematicBlockManager.getSchematicBlock(
                    world,
                    pos,
                    pos,
                    world.getBlockState(pos),
                    world.getBlockState(pos).getBlock()
            );
            if (schematicBlock.isAir()) {
                return EnumActionResult.FAIL;
            }
            NBTUtilBC.getItemData(stack).setTag("schematic", SchematicBlockManager.writeToNBT(schematicBlock));
            stack.setItemDamage(DAMAGE_USED);
            return EnumActionResult.SUCCESS;
        } else {
            BlockPos placePos = pos.offset(side);
            if (!world.isAirBlock(placePos)) {
                player.sendMessage(new TextComponentString("Not an air block @" + placePos));
                return EnumActionResult.FAIL;
            }
            ISchematicBlock<?> schematicBlock;
            try {
                schematicBlock = SchematicBlockManager.readFromNBT(
                        NBTUtilBC.getItemData(stack).getCompoundTag("schematic")
                );

                // TODO: extract required items and fluids from player's inventory
                if (!schematicBlock.isBuilt(world, placePos) &&
                        schematicBlock.canBuild(world, placePos) &&
                        schematicBlock.build(world, placePos)) {
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                } 
            } catch (InvalidInputDataException e) {
                player.sendMessage(new TextComponentString("Invalid schematic: " + e.getMessage()));
                e.printStackTrace();
                return EnumActionResult.FAIL;
            }
        }
    }
}
