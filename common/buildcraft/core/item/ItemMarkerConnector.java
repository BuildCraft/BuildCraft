/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.item;

import java.util.Iterator;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

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

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.Line;
import buildcraft.lib.misc.PositionUtil.LineSkewResult;
import buildcraft.lib.misc.VecUtil;

import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ItemMarkerConnector extends ItemBC_Neptune {
    public ItemMarkerConnector(String id) {
        super(id);
    }

    @SuppressWarnings("NullableProblems")
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
        return new ActionResult<>(onItemRightClickVolumeBoxes(world, player), player.getHeldItem(hand));
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
        return best != null &&
            (cache.tryConnect(best.marker1, best.marker2) || cache.tryConnect(best.marker2, best.marker1));
    }

    public static boolean doesInteract(BlockPos a, BlockPos b, EntityPlayer player) {
        return new MarkerLineInteraction(
            a,
            b,
            player.getPositionVector().addVector(0, player.getEyeHeight(), 0),
            player.getLookVec()
        ).didInteract();
    }

    private EnumActionResult onItemRightClickVolumeBoxes(World world, EntityPlayer player) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);

        VolumeBox currentEditing = volumeBoxes.getCurrentEditing(player);

        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d end = start.add(player.getLookVec().scale(4));

        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot = EnumAddonSlot.getSelectingVolumeBoxAndSlot(
            player,
            volumeBoxes.volumeBoxes
        );
        VolumeBox addonVolumeBox = selectingVolumeBoxAndSlot.getLeft();
        EnumAddonSlot addonSlot = selectingVolumeBoxAndSlot.getRight();
        if (addonVolumeBox != null && addonSlot != null) {
            if (addonVolumeBox.addons.containsKey(addonSlot) &&
                addonVolumeBox.getLockTargetsStream().noneMatch(target ->
                    target instanceof Lock.Target.TargetAddon && ((Lock.Target.TargetAddon) target).slot == addonSlot
                )) {
                if (player.isSneaking()) {
                    addonVolumeBox.addons.get(addonSlot).onRemoved();
                    addonVolumeBox.addons.remove(addonSlot);
                    volumeBoxes.markDirty();
                } else {
                    addonVolumeBox.addons.get(addonSlot).onPlayerRightClick(player);
                    volumeBoxes.markDirty();
                }
            }
        } else if (player.isSneaking()) {
            if (currentEditing == null) {
                for (Iterator<VolumeBox> iterator = volumeBoxes.volumeBoxes.iterator(); iterator.hasNext();) {
                    VolumeBox volumeBox = iterator.next();
                    if (volumeBox.box.getBoundingBox().calculateIntercept(start, end) != null) {
                        if (volumeBox.getLockTargetsStream().noneMatch(Lock.Target.TargetResize.class::isInstance)) {
                            volumeBox.addons.values().forEach(Addon::onRemoved);
                            iterator.remove();
                            volumeBoxes.markDirty();
                            return EnumActionResult.SUCCESS;
                        } else {
                            return EnumActionResult.FAIL;
                        }
                    }
                }
            } else {
                currentEditing.cancelEditing();
                volumeBoxes.markDirty();
                return EnumActionResult.SUCCESS;
            }
        } else {
            if (currentEditing == null) {
                VolumeBox bestVolumeBox = null;
                double bestDist = Double.MAX_VALUE;
                BlockPos editing = null;

                for (VolumeBox volumeBox :
                    volumeBoxes.volumeBoxes.stream()
                        .filter(box ->
                            box.getLockTargetsStream()
                                .noneMatch(Lock.Target.TargetResize.class::isInstance)
                        )
                        .collect(Collectors.toList())
                    ) {
                    for (BlockPos p : PositionUtil.getCorners(volumeBox.box.min(), volumeBox.box.max())) {
                        RayTraceResult ray = new AxisAlignedBB(p).calculateIntercept(start, end);
                        if (ray != null) {
                            double dist = ray.hitVec.distanceTo(start);
                            if (bestDist > dist) {
                                bestDist = dist;
                                bestVolumeBox = volumeBox;
                                editing = p;
                            }
                        }
                    }
                }

                if (bestVolumeBox != null) {
                    bestVolumeBox.setPlayer(player);

                    BlockPos min = bestVolumeBox.box.min();
                    BlockPos max = bestVolumeBox.box.max();

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
                    bestVolumeBox.setHeldDistOldMinOldMax(
                        held,
                        Math.max(1.5, bestDist + 0.5),
                        bestVolumeBox.box.min(),
                        bestVolumeBox.box.max()
                    );
                    volumeBoxes.markDirty();
                    return EnumActionResult.SUCCESS;
                }
            } else {
                currentEditing.confirmEditing();
                volumeBoxes.markDirty();
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    @SuppressWarnings("WeakerAccess")
    private static class MarkerLineInteraction {
        public final BlockPos marker1, marker2;
        public final double distToPoint, distToLine;

        public MarkerLineInteraction(BlockPos marker1, BlockPos marker2, Vec3d playerPos, Vec3d playerEndPos) {
            this.marker1 = marker1;
            this.marker2 = marker2;
            LineSkewResult interactionPoint = PositionUtil.findLineSkewPoint(
                new Line(
                    VecUtil.convertCenter(marker1),
                    VecUtil.convertCenter(marker2)
                ),
                playerPos,
                playerEndPos
            );
            distToPoint = interactionPoint.closestPos.distanceTo(playerPos);
            distToLine = interactionPoint.distFromLine;
        }

        public boolean didInteract() {
            return distToPoint <= 3 && distToLine < 0.3;
        }

        public MarkerLineInteraction getBetter(MarkerLineInteraction other) {
            if (other == null) {
                return this;
            }
            if (other.marker1 == marker2 && other.marker2 == marker1) {
                return other;
            }
            if (other.distToLine < distToLine) {
                return other;
            }
            if (other.distToLine > distToLine) {
                return this;
            }
            if (other.distToPoint < distToPoint) {
                return other;
            }
            return this;
        }
    }
}
