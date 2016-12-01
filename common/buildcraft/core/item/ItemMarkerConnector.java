/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.core.marker.volume.VolumeMarkerCache;
import buildcraft.core.marker.volume.VolumeMarkerCache.VolumeBox;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.Line;
import buildcraft.lib.misc.PositionUtil.LineSkewResult;
import buildcraft.lib.misc.VecUtil;

public class ItemMarkerConnector extends ItemBC_Neptune {
    public ItemMarkerConnector(String id) {
        super(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            for (MarkerCache<?> cache : MarkerCache.CACHES) {
                if (interactCache(cache.getSubCache(world), player)) {
                    player.swingArm(hand);
                    break;
                }
            }
        }
        return newVolumeCacheStuff_onItemRightClick(player.getHeldItem(hand), world, player, hand);
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
            Line line = new Line(VecUtil.convertCenter(marker1), VecUtil.convertCenter(marker2));
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

    // ##################################
    //
    // NEW volume cache stuff
    //
    // ##################################

    private ActionResult<ItemStack> newVolumeCacheStuff_onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            // only run this on the client
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        VolumeMarkerCache mk = VolumeMarkerCache.SERVER_INSTANCE;

        if (player.isSneaking()) {
            mk.currentlyEditing = null;

            Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
            Vec3d end = start.add(player.getLookVec().scale(4));

            Iterator<VolumeBox> iter = mk.boxes.iterator();
            while (iter.hasNext()) {
                VolumeBox vbox = iter.next();
                AxisAlignedBB aabb = new AxisAlignedBB(vbox.box.min(), vbox.box.max().add(VecUtil.POS_ONE));

                RayTraceResult ray = aabb.calculateIntercept(start, end);
                if (ray != null) {
                    iter.remove();
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (mk.currentlyEditing == null) {
            VolumeBox vBest = null;
            double bestDist = 10000;
            BlockPos editing = null;

            Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
            Vec3d end = start.add(player.getLookVec().scale(4));

            for (VolumeBox vbox : mk.boxes) {

                for (BlockPos p : PositionUtil.getCorners(vbox.box.min(), vbox.box.max())) {
                    AxisAlignedBB aabb = new AxisAlignedBB(p);

                    RayTraceResult ray = aabb.calculateIntercept(start, end);

                    if (ray != null) {
                        double dist = ray.hitVec.distanceTo(start);
                        if (bestDist > dist) {
                            bestDist = dist;
                            vBest = vbox;
                            editing = p;
                        }
                    }
                }
            }

            if (vBest != null && editing != null) {
                mk.currentlyEditing = vBest;

                BlockPos min = vBest.box.min();
                BlockPos max = vBest.box.max();

                BlockPos held = min;
                if (editing.getX() == min.getX()) {
                    held = VecUtil.replaceValue(held, Axis.X, max.getX());
                }
                if (editing.getY() == min.getY()) {
                    held = VecUtil.replaceValue(held, Axis.Y, max.getY());
                }
                if (editing.getZ() == min.getZ()) {
                    held = VecUtil.replaceValue(held, Axis.Z, max.getZ());
                }
                mk.held = held;
                mk.dist = Math.max(1.5, bestDist + 0.5);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        } else {
            mk.currentlyEditing.box.reset();
            mk.currentlyEditing.box.extendToEncompass(mk.held);
            BlockPos lookingAt = new BlockPos(player.getPositionVector().addVector(0, player.getEyeHeight(), 0).add(player.getLookVec().scale(mk.dist)));
            mk.currentlyEditing.box.extendToEncompass(lookingAt);
            mk.currentlyEditing = null;
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
}
