/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.BlockUtils;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {
    private final Set<Class<? extends Block>> shiftRotations = new HashSet<Class<? extends Block>>();
    private final Set<Class<? extends Block>> blacklistedRotations = new HashSet<Class<? extends Block>>();

    public ItemWrench() {
        super();

        setFull3D();
        setMaxStackSize(1);
        shiftRotations.add(BlockLever.class);
        shiftRotations.add(BlockButton.class);
        shiftRotations.add(BlockChest.class);
        blacklistedRotations.add(BlockBed.class);
        setHarvestLevel("wrench", 0);
    }

    private boolean isClass(Set<Class<? extends Block>> set, Class<? extends Block> cls) {
        for (Class<? extends Block> shift : set) {
            if (shift.isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == null || isClass(blacklistedRotations, block.getClass())) {
            return false;
        }

        if (player.isSneaking() != isClass(shiftRotations, block.getClass())) {
            return false;
        }

        // Double chests should NOT be rotated.
        if (block instanceof BlockChest && BlockUtils.getOtherDoubleChest(world.getTileEntity(pos)) != null) {
            return false;
        }

        if (block.rotateBlock(world, pos, side)) {
            player.swingItem();
            return !world.isRemote;
        }
        return false;
    }

    @Override
    public boolean canWrench(EntityPlayer player, BlockPos pos) {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, BlockPos pos) {
        player.swingItem();
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean canWrench(EntityPlayer player, Entity entity) {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, Entity entity) {
        player.swingItem();
    }
}
