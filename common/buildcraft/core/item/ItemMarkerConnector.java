/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.Line;
import buildcraft.lib.misc.PositionUtil.LineSkewResult;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;

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
        if (world.isRemote) {
            // only run this on the client
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);

        VolumeBox currentEditing = volumeBoxes.getCurrentEditing(player);

        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d end = start.add(player.getLookVec().scale(4));

        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, volumeBoxes);
        VolumeBox addonBox = selectingBoxAndSlot.getLeft();
        EnumAddonSlot addonSlot = selectingBoxAndSlot.getRight();
        if (addonBox != null && addonSlot != null) {
            if (addonBox.addons.containsKey(addonSlot)) {
                if (player.isSneaking()) {
                    addonBox.addons.get(addonSlot).onRemoved();
                    addonBox.addons.remove(addonSlot);
                    volumeBoxes.markDirty();
                } else {
                    addonBox.addons.get(addonSlot).onPlayerRightClick(player);
                    volumeBoxes.markDirty();
                }
            }
        } else if (player.isSneaking()) {
            if (currentEditing == null) {
                for (Iterator<VolumeBox> iterator = volumeBoxes.boxes.iterator(); iterator.hasNext(); ) {
                    VolumeBox box = iterator.next();
                    if (box.box.getBoundingBox().calculateIntercept(start, end) != null) {
                        box.addons.values().forEach(Addon::onRemoved);
                        iterator.remove();
                        volumeBoxes.markDirty();
                        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                    }
                }
            } else {
                currentEditing.cancelEditing();
                volumeBoxes.markDirty();
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        } else {
            if (currentEditing == null) {
                VolumeBox bestBox = null;
                double bestDist = 10000;
                BlockPos editing = null;

                for (VolumeBox box : volumeBoxes.boxes) {
                    for (BlockPos p : PositionUtil.getCorners(box.box.min(), box.box.max())) {
                        RayTraceResult ray = new AxisAlignedBB(p).calculateIntercept(start, end);
                        if (ray != null) {
                            double dist = ray.hitVec.distanceTo(start);
                            if (bestDist > dist) {
                                bestDist = dist;
                                bestBox = box;
                                editing = p;
                            }
                        }
                    }
                }

                if (bestBox != null) {
                    bestBox.setPlayer(player);

                    BlockPos min = bestBox.box.min();
                    BlockPos max = bestBox.box.max();

                    BlockPos held = min;
                    if (editing.getX() == min.getX()) {
                        held = VecUtil.replaceValue(held, EnumFacing.Axis.X, max.getX());
                    }
                    if (editing.getY() == min.getY()) {
                        held = VecUtil.replaceValue(held, EnumFacing.Axis.Y, max.getY());
                    }
                    if (editing.getZ() == min.getZ()) {
                        held = VecUtil.replaceValue(held, EnumFacing.Axis.Z, max.getZ());
                    }
                    bestBox.setHeldDistOldMinOldMax(held, Math.max(1.5, bestDist + 0.5), bestBox.box.min(), bestBox.box.max());
                    volumeBoxes.markDirty();
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            } else {
                currentEditing.confirmEditing();
                volumeBoxes.markDirty();
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
}
