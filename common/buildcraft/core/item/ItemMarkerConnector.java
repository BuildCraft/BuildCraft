/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.Line;
import buildcraft.lib.misc.PositionUtil.LineSkewResult;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMarkerConnector extends ItemBC_Neptune {
    public ItemMarkerConnector(String id) {
        super(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        EnumActionResult result = EnumActionResult.PASS;
        if (!world.isRemote) {
            for (MarkerCache<?> cache : MarkerCache.CACHES) {
                if (interactCache(cache.getSubCache(world), player)) {
                    result = EnumActionResult.SUCCESS;
                    player.swingArm(hand);
                    break;
                }
            }
        }
        return ActionResult.newResult(result, stack);
    }

    private static <S extends MarkerSubCache<?>> boolean interactCache(S cache, EntityPlayer player) {
        MarkerLineInteraction best = null;
        Vec3d playerPos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d playerLook = player.getLookVec();
        for (BlockPos marker : cache.getAllMarkers()) {
            ImmutableList<BlockPos> possibles = cache.getValidConnections(marker);
            for (BlockPos possible : possibles) {
                MarkerLineInteraction interaction = new MarkerLineInteraction(marker, possible, playerPos, playerLook);
                if (interaction.didInteract()) {
                    best = interaction.getBetter(best);
                }
            }
        }
        if (best != null) {
            if (cache.tryConnect(best.marker1, best.marker2)) {
                return true;
            } else if (cache.tryConnect(best.marker2, best.marker1)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean doesInteract(BlockPos a, BlockPos b, EntityPlayer player) {
        Vec3d playerPos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d playerLook = player.getLookVec();
        MarkerLineInteraction interaction = new MarkerLineInteraction(a, b, playerPos, playerLook);
        return interaction.didInteract();
    }

    private static class MarkerLineInteraction {
        public final BlockPos marker1, marker2;
        public final double distToPoint, distToLine;

        public MarkerLineInteraction(BlockPos marker1, BlockPos marker2, Vec3d playerPos, Vec3d playerEndPos) {
            this.marker1 = marker1;
            this.marker2 = marker2;
            Line line = new Line(new Vec3d(marker1).add(Utils.VEC_HALF), new Vec3d(marker2).add(Utils.VEC_HALF));
            LineSkewResult interactionPoint = PositionUtil.findLineSkewPoint(line, playerPos, playerEndPos);
            distToPoint = interactionPoint.closestPos.distanceTo(playerPos);
            distToLine = interactionPoint.distFromLine;
        }

        public boolean didInteract() {
            return distToPoint <= 3 && distToLine < 0.3;
        }

        public MarkerLineInteraction getBetter(MarkerLineInteraction other) {
            if (other == null) return this;
            if (other.marker1 == marker2 && other.marker2 == marker1) {
                return other;
            }
            if (other.distToLine < distToLine) return other;
            if (other.distToLine > distToLine) return this;
            if (other.distToPoint < distToPoint) return other;
            return this;
        }
    }
}
