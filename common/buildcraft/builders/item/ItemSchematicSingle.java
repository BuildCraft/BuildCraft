/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;

import buildcraft.builders.bpt.player.BuilderPlayer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemSchematicSingle extends ItemBC_Neptune {
    private static final String NBT_KEY_SCHEMATIC = "schematic";
    private static final int DAMAGE_CLEAN = 0;
    private static final int DAMAGE_STORED_SCHEMATIC = 1;

    public ItemSchematicSingle(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (stack.getItemDamage() == DAMAGE_CLEAN) {
            return 16;
        } else {
            return super.getItemStackLimit(stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, DAMAGE_CLEAN, "clean");
        addVariant(variants, DAMAGE_STORED_SCHEMATIC, "used");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = StackUtil.asNonNull(player.getHeldItem(hand));
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            NBTTagCompound itemData = NBTUtilBC.getItemData(stack);
            itemData.removeTag(NBT_KEY_SCHEMATIC);
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
            itemData.removeTag(NBT_KEY_SCHEMATIC);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return EnumActionResult.SUCCESS;
        }
        int damage = stack.getItemDamage();
        if (damage != DAMAGE_STORED_SCHEMATIC) {
            IBlockState state = world.getBlockState(pos);
            SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(state.getBlock());
            if (factory != null) {
                try {
                    SchematicBlock schematic = factory.createFromWorld(world, pos);
                    NBTTagCompound schematicData = schematic.serializeNBT();
                    NBTTagCompound itemData = NBTUtilBC.getItemData(stack);
                    itemData.setTag(NBT_KEY_SCHEMATIC, schematicData);
                    stack.setItemDamage(DAMAGE_STORED_SCHEMATIC);
                    return EnumActionResult.SUCCESS;
                } catch (SchematicException e) {
                    e.printStackTrace();
                }
            }
            return EnumActionResult.FAIL;
        } else {
            NBTTagCompound schematicNBT = NBTUtilBC.getItemData(stack).getCompoundTag(NBT_KEY_SCHEMATIC);
            if (schematicNBT == null) {
                player.sendMessage(new TextComponentString("No schematic data!"));
                return EnumActionResult.FAIL;
            }
            BlockPos place = pos.offset(side);
            if (!world.isAirBlock(place)) {
                player.sendMessage(new TextComponentString("Not an air block @" + place));
                return EnumActionResult.FAIL;
            } else {
                world.setBlockToAir(place);
            }
            try {
                SchematicBlock schematic = BlueprintAPI.deserializeSchematicBlock(schematicNBT);
                BuilderPlayer playerBuilder = new BuilderPlayer(player);
                if (schematic.buildImmediatly(world, playerBuilder, place)) {
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            } catch (SchematicException e) {
                e.printStackTrace();
                return EnumActionResult.FAIL;
            }
        }
    }
}
