/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by api.blocks.CustomRotationHelper, api.tools.IToolWrench, lib.misc.SoundUtil (not yet migrated)
package buildcraft.core.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;

public class ItemWrench_Neptune extends ItemBC_Neptune implements IToolWrench {
    private static final Identifier ADVANCEMENT = new Identifier("buildcraftcore:wrenched");

    public ItemWrench_Neptune(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    public boolean canWrench(PlayerEntity player, Hand hand, ItemStack wrench, HitResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(PlayerEntity player, Hand hand, ItemStack wrench, HitResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        player.swingArm(hand);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean doesSneakBypassUse(ItemStack stack, BlockView world, BlockPos pos, PlayerEntity player) {
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public ActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        // FIXME: Disabled world check as it doesn't allow us to swing the player's arm!
        // if (world.isClient) {
        // return ActionResult.PASS;
        // }
        BlockState state = world.getBlockState(pos);
        state = state.getActualState(world, pos);
        ActionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
        if (result == ActionResult.SUCCESS) {
            wrenchUsed(player, hand, player.getStackInHand(hand), new HitResult(new Vec3d(hitX, hitY, hitZ), side, pos));
        }
        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }
}
