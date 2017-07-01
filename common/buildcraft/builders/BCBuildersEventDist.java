/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.builders.client.ClientArchitectTables;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.builders.tile.TileQuarry;

public enum BCBuildersEventDist implements IWorldEventListener {
    INSTANCE;

    private static final UUID UUID_SINGLE_SCHEMATIC = new UUID(0xfd3b8c59b0a8b191L, 0x772ec006c1b0ffaaL);
    private final Map<World, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();

    public void validateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(quarry.getWorld());
        if (quarries == null) {
            quarries = new LinkedList<>();
            allQuarries.put(quarry.getWorld(), quarries);
            quarry.getWorld().addEventListener(this);
        }
        quarries.add(new WeakReference<>(quarry));
        BCLog.logger.info("Added quarry to checking list");
    }

    public void invalidateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(quarry.getWorld());
        if (quarries == null) {
            // Odd.
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
                iter.remove();
                BCLog.logger.info("Removed quarry from checking list");
            }
        }
    }

    @SubscribeEvent
    public void onGetCollisionBoxesForQuarry(GetCollisionBoxesEvent event) {
        AxisAlignedBB target = event.getAabb();
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(event.getWorld());
        if (quarries == null) {
            // No quarries in the target world
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry quarry = ref.get();
            if (quarry == null) {
                iter.remove();
                continue;
            }
            for (AxisAlignedBB aabb : quarry.getCollisionBoxes()) {
                if (target.intersectsWith(aabb)) {
                    event.getCollisionBoxesList().add(aabb);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderTooltipPostText(RenderTooltipEvent.PostText event) {
        Snapshot snapshot = null;
        ItemStack stack = event.getStack();
        Header header = BCBuildersItems.snapshot.getHeader(stack);
        if (header != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        } else {
            ISchematicBlock<?> schematic = ItemSchematicSingle.getSchematicSafe(stack);
            if (schematic != null) {
                Blueprint blueprint = new Blueprint();
                blueprint.size = new BlockPos(1, 1, 1);
                blueprint.offset = BlockPos.ORIGIN;
                blueprint.data = new int[][][] { { { 0 } } };
                blueprint.palette.add(schematic);
                blueprint.computeKey();
                snapshot = blueprint;
            }
        }

        if (snapshot != null) {
            int pX = event.getX();
            int pY = event.getY() + event.getHeight() + 10;
            int sX = 100;
            int sY = 100;

            // Copy from GuiUtils#drawHoveringText
            int zLevel = 300;
            int backgroundColor = 0xF0100010;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 4, pX + sX + 3, pY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 3, pX + sX + 3, pY + sY + 4, backgroundColor,
                backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY + sY + 3, backgroundColor,
                backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 4, pY - 3, pX - 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 3, pY - 3, pX + sX + 4, pY + sY + 3, backgroundColor,
                backgroundColor);
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3 + 1, pX - 3 + 1, pY + sY + 3 - 1, borderColorStart,
                borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 2, pY - 3 + 1, pX + sX + 3, pY + sY + 3 - 1, borderColorStart,
                borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY - 3 + 1, borderColorStart,
                borderColorStart);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 2, pX + sX + 3, pY + sY + 3, borderColorEnd,
                borderColorEnd);

            ClientSnapshots.INSTANCE.renderSnapshot(snapshot, pX, pY, sX, sY);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused()) {
            ClientArchitectTables.tick();
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {

    }


    @Override
    public void notifyBlockUpdate(World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(world);
        if (quarries == null)
            return;
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry quarry = ref.get();
            if (quarry == null) {
                iter.remove();
                continue;
            }
            quarry.onLocationChange(pos);
        }
    }

    @Override
    public void notifyLightSet(BlockPos pos) {}

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {}

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {}

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

    @Override
    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

    @Override
    public void onEntityAdded(Entity entityIn) {}

    @Override
    public void onEntityRemoved(Entity entityIn) {}

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {}

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
}
