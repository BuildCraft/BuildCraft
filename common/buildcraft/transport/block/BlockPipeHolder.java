/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;

/** The pipe block. Migrated from the 1,115-LOC Forge original (Phase 4F finale of the transport block layer).
 *
 * <h2>IExtendedBlockState replacement</h2>
 * Forge attached a {@code WeakReference<TilePipeHolder>} to the block state through an unlisted property
 * ({@code PROP_TILE}) + {@code getExtendedState()}, and the baked model pulled the {@link
 * buildcraft.transport.client.model.key.PipeModelKey PipeModelKey} from it. 1.20.1 has no IExtendedBlockState, so
 * the per-tile render data is routed through Fabric's {@code RenderAttachmentBlockEntity} on {@link TilePipeHolder}
 * (see {@link TilePipeHolder#getRenderAttachmentData()}). Consequently the block carries <b>no</b> block-state
 * properties at all (the Forge {@code ExtendedBlockState} held only the unlisted tile reference), so there is no
 * {@code createBlockState}/{@code appendProperties} override and no placement metadata (pipe type + colour live on
 * the item / block entity, not the block state — there is no meta in 1.20.1).
 *
 * <h2>Raytracing / sub-parts</h2>
 * Yarn's {@link BlockHitResult} has no {@code subHit} field, so the Forge per-sub-part hit index is preserved via
 * the {@link PipeRayTraceResult} subclass. Collision/outline shapes are assembled as {@link VoxelShape}s via
 * {@link VoxelShapes#union} from the pipe centre, connection boxes, pluggable boxes and wire boxes. */
public class BlockPipeHolder extends BlockBCBase_Neptune implements BlockEntityProvider {

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final Box BOX_CENTER = new Box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final Box BOX_DOWN = new Box(0.25, 0, 0.25, 0.75, 0.25, 0.75);
    private static final Box BOX_UP = new Box(0.25, 0.75, 0.25, 0.75, 1, 0.75);
    private static final Box BOX_NORTH = new Box(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    private static final Box BOX_SOUTH = new Box(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    private static final Box BOX_WEST = new Box(0, 0.25, 0.25, 0.25, 0.75, 0.75);
    private static final Box BOX_EAST = new Box(0.75, 0.25, 0.25, 1, 0.75, 0.75);
    private static final Box[] BOX_FACES = { BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST };

    private static final Box FULL_BLOCK_BOX = new Box(0, 0, 0, 1, 1, 1);

    public BlockPipeHolder(AbstractBlock.Settings settings, String id) {
        // STUB(R.Chen): Forge setHardness(0.25f)/setResistance(3.0f)/setLightOpacity(0) move into the
        // AbstractBlock.Settings passed by the registrar (strength/.nonOpaque()); set there in Phase 4F.
        super(settings, id);
    }

    // BlockEntityProvider

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TilePipeHolder(BCTransportBlocks.pipeHolderTile, pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
        BlockEntityType<T> type) {
        if (type != BCTransportBlocks.pipeHolderTile) {
            return null;
        }
        return (BlockEntityTicker<T>) TilePipeHolder.<TilePipeHolder>ticker();
    }

    // Shapes (was getBoundingBox / addCollisionBoxToList)

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // STUB(R.Chen): Forge getSelectedBoundingBox returned only the single sub-part under the cursor (so the
        // highlight box hugged the wire/pluggable/connection being looked at). That requires the client's live
        // raytrace, which is a Phase 5 client-render concern; for now the outline is the full pipe envelope.
        return getShape(world, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShape(world, pos);
    }

    private static VoxelShape getShape(BlockView world, BlockPos pos) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile == null) {
            return VoxelShapes.fullCube();
        }
        List<Box> boxes = new ArrayList<>();
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            boxes.add(BOX_CENTER);
            for (Direction face : DIRECTIONS) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    boxes.add(connectionBox(face, conSize));
                }
            }
        }
        for (Direction face : DIRECTIONS) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                boxes.add(pluggable.getBoundingBox());
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            boxes.add(part.boundingBox);
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            boxes.add(between.boundingBox);
        }
        if (boxes.isEmpty()) {
            return VoxelShapes.fullCube();
        }
        VoxelShape shape = VoxelShapes.empty();
        for (Box box : boxes) {
            shape = VoxelShapes.union(shape, VoxelShapes.cuboid(box));
        }
        return shape;
    }

    /** The collision/raytrace box for a pipe connection of the given length (was inline in the Forge collision +
     * raytrace loops; {@code BoundingBoxUtil.makeFrom} is not yet migrated, so the {@link Box} is built directly). */
    private static Box connectionBox(Direction face, float conSize) {
        if (conSize == 0.25f) {
            return BOX_FACES[face.ordinal()];
        }
        Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
        Vec3d radius = VecUtil.replaceValue(new Vec3d(0.25, 0.25, 0.25), face.getAxis(), conSize / 2);
        return new Box(center.subtract(radius), center.add(radius));
    }

    // Raytracing (was collisionRayTrace + the custom rayTrace family). Yarn BlockHitResult has no subHit, so the
    // sub-part index is carried by PipeRayTraceResult.

    @Nullable
    public PipeRayTraceResult rayTrace(World world, BlockPos pos, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        // STUB(R.Chen): Forge read the exact reach from ServerPlayerEntity.interactionManager.getBlockReachDistance();
        // Yarn's ServerPlayerInteractionManager does not expose it, so the vanilla 5-block reach is used.
        double reachDistance = 5;
        Vec3d end = start.add(player.getRotationVec(1.0F).normalize().multiply(reachDistance));
        return rayTrace(world, pos, start, end);
    }

    @Nullable
    public PipeRayTraceResult rayTrace(World world, BlockPos pos, Vec3d start, Vec3d end) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile == null) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_BOX, 400);
        }
        PipeRayTraceResult best = null;
        Pipe pipe = tile.getPipe();
        boolean computed = false;
        if (pipe != null) {
            computed = true;
            best = computeTrace(best, pos, start, end, BOX_CENTER, 0);
            for (Direction face : DIRECTIONS) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    best = computeTrace(best, pos, start, end, connectionBox(face, conSize), face.ordinal() + 1);
                }
            }
        }
        for (Direction face : DIRECTIONS) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                best = computeTrace(best, pos, start, end, pluggable.getBoundingBox(), face.ordinal() + 1 + 6);
                computed = true;
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            best = computeTrace(best, pos, start, end, part.boundingBox, part.ordinal() + 1 + 6 + 6);
            computed = true;
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            best = computeTrace(best, pos, start, end, between.boundingBox, between.ordinal() + 1 + 6 + 6 + 8);
            computed = true;
        }
        if (!computed) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_BOX, 400);
        }
        return best;
    }

    @Nullable
    public static EnumWirePart rayTraceWire(BlockPos pos, Vec3d start, Vec3d end) {
        Vec3d realStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3d realEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        EnumWirePart best = null;
        double dist = 1000;
        for (EnumWirePart part : EnumWirePart.VALUES) {
            // Forge Box.calculateIntercept(start, end) → Yarn Box.raycast(start, end) (Optional<Vec3d>).
            Optional<Vec3d> trace = part.boundingBoxPossible.raycast(realStart, realEnd);
            if (trace.isPresent()) {
                double nextDist = trace.get().squaredDistanceTo(realStart);
                if (best == null || dist > nextDist) {
                    best = part;
                    dist = nextDist;
                }
            }
        }
        return best;
    }

    private static PipeRayTraceResult computeTrace(PipeRayTraceResult lastBest, BlockPos pos, Vec3d start, Vec3d end,
        Box box, int part) {
        Box worldBox = box.offset(pos);
        Optional<Vec3d> hit = worldBox.raycast(start, end);
        if (hit.isEmpty()) {
            return lastBest;
        }
        Vec3d hitVec = hit.get();
        PipeRayTraceResult next = new PipeRayTraceResult(hitVec, sideForHit(worldBox, hitVec), pos, false, part);
        if (lastBest == null) {
            return next;
        }
        double distLast = lastBest.getPos().squaredDistanceTo(start);
        double distNext = hitVec.squaredDistanceTo(start);
        return distLast > distNext ? next : lastBest;
    }

    /** Determines which face of {@code box} the {@code hit} point lies on. Forge got this for free from
     * {@code Block.rayTrace}; Yarn's {@link Box#raycast(Vec3d, Vec3d)} only returns the point, so it is recovered
     * by matching the hit against the box bounds. */
    private static Direction sideForHit(Box box, Vec3d hit) {
        double e = 1.0E-4;
        if (Math.abs(hit.x - box.minX) < e) return Direction.WEST;
        if (Math.abs(hit.x - box.maxX) < e) return Direction.EAST;
        if (Math.abs(hit.y - box.minY) < e) return Direction.DOWN;
        if (Math.abs(hit.y - box.maxY) < e) return Direction.UP;
        if (Math.abs(hit.z - box.minZ) < e) return Direction.NORTH;
        return Direction.SOUTH;
    }

    @Nullable
    public static Direction getPartSideHit(PipeRayTraceResult trace) {
        if (trace.subHit <= 0) {
            return trace.getSide();
        }
        if (trace.subHit <= 6) {
            return DIRECTIONS[trace.subHit - 1];
        }
        if (trace.subHit <= 6 + 6) {
            return DIRECTIONS[trace.subHit - 1 - 6];
        }
        return null;
    }

    @Nullable
    public static EnumWirePart getWirePartHit(PipeRayTraceResult trace) {
        if (trace.subHit <= 6 + 6) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8) {
            return EnumWirePart.VALUES[trace.subHit - 1 - 6 - 6];
        } else {
            return null;
        }
    }

    @Nullable
    public static EnumWireBetween getWireBetweenHit(PipeRayTraceResult trace) {
        if (trace.subHit <= 6 + 6 + 8) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8 + EnumWireBetween.VALUES.length) {
            return EnumWireBetween.VALUES[trace.subHit - 1 - 6 - 6 - 8];
        } else {
            return null;
        }
    }

    // Interactions

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
        BlockHitResult hit) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile == null) {
            return ActionResult.PASS;
        }
        PipeRayTraceResult trace = rayTrace(world, pos, player);
        if (trace == null) {
            return ActionResult.PASS;
        }
        Direction realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = hit.getSide();
        }
        float hitX = (float) (trace.getPos().x - pos.getX());
        float hitY = (float) (trace.getPos().y - pos.getY());
        float hitZ = (float) (trace.getPos().z - pos.getZ());

        PipePluggable existing = tile.getPluggable(realSide);
        if (trace.subHit > 6 && trace.subHit <= 12 && existing != null) {
            if (existing.onPluggableActivate(player, trace, hitX, hitY, hitZ)) {
                return ActionResult.SUCCESS;
            }
        }

        EnumPipePart part = trace.subHit == 0 ? EnumPipePart.CENTER : EnumPipePart.fromFacing(realSide);

        ItemStack held = player.getStackInHand(hand);
        Item item = held.isEmpty() ? null : held.getItem();
        if (item instanceof IItemPluggable && existing == null) {
            IItemPluggable itemPlug = (IItemPluggable) item;
            PipePluggable plug = itemPlug.onPlace(held, tile, realSide, player, hand);
            if (plug == null) {
                return ActionResult.FAIL;
            } else {
                tile.replacePluggable(realSide, plug);
                plug.onPlacedBy(player);
                if (!player.getAbilities().creativeMode) {
                    held.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        // STUB(R.Chen): the ItemWire placement branch is dropped here. It needs the unmigrated transport item
        // layer (ItemWire + BCTransportItems.wire) and relied on stack.getDamage()→DyeColor, which the 1.13
        // item flattening removed. Restore in Phase 4F (wire item carries its colour via the item/NBT). The Forge
        // branch added a wire part via tile.getWireManager().addPart(...), scheduled a WIRES network update, and
        // unlocked ADVANCEMENT_LOGIC_TRANSPORTATION when the new wire became connected.

        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return ActionResult.PASS;
        }
        if (pipe.behaviour.onPipeActivate(player, trace, hitX, hitY, hitZ, part)) {
            return ActionResult.SUCCESS;
        }
        if (pipe.flow.onFlowActivate(player, trace, hitX, hitY, hitZ, part)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos,
        boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        TilePipeHolder tile = getPipe(world, pos);
        if (tile != null) {
            tile.onNeighbourBlockChanged(block, fromPos);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile == null) {
            return;
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.getBehaviour().onEntityCollide(entity);
        }
    }

    // Drops (was getDrops + the partial removedByPlayer logic)

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockEntity be = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (!(be instanceof TilePipeHolder)) {
            return super.getDroppedStacks(state, builder);
        }
        TilePipeHolder tile = (TilePipeHolder) be;
        DefaultedList<ItemStack> toDrop = DefaultedList.of();
        for (Direction face : DIRECTIONS) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                pluggable.addDrops(toDrop, 0);
            }
        }
        // STUB(R.Chen): wire drops (new ItemStack(BCTransportItems.wire, ...)) dropped — wire item layer unmigrated.
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.addDrops(toDrop, 0);
        }
        return toDrop;
        // STUB(R.Chen): the Forge removedByPlayer sub-part removal (break only the wire/pluggable under the cursor
        // and cancel the full-block break) needs the player's live raytrace at break time + the wire item layer;
        // deferred to Phase 4F. The full-block break path drops everything above, which getDroppedStacks covers.
    }

    // Redstone

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile != null) {
            return tile.getRedstoneOutput(side.getOpposite());
        }
        return 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return getStrongRedstonePower(state, world, pos, side);
    }

    // STUB(R.Chen): Forge IForgeBlock hooks with no vanilla equivalent are deferred:
    //   - canConnectRedstone(state, world, pos, side) → pluggable.canConnectToRedstone (no Fabric block hook).
    //   - getExplosionResistance(world, pos, exploder, explosion) → positional pluggable blast resistance
    //     (AbstractBlock only exposes a flat getBlastResistance from Settings).
    //   - isSideSolid / getBlockFaceShape / canBeConnectedTo (BlockFaceShape removed in 1.16+).
    //   - addLandingEffects / addRunningEffects / addHitEffects / addDestroyEffects (client particle hooks).
    // All restored alongside the Phase 5 client render layer / Phase 4F capability wiring.

    // Paint
    // STUB(R.Chen): was @Override of ICustomPaintHandler.attemptPaint — that API interface is still Forge-form
    // (ActionResult/DyeColor) and unmigrated, so the implements clause is dropped and this is kept as a
    // plain method (no @Override). Re-add the interface + its registration in Phase 4F. Logic is preserved.
    public ActionResult attemptPaint(World world, BlockPos pos, BlockState state, Vec3d hitPos,
        @Nullable Direction hitSide, @Nullable DyeColor paintColour) {
        TilePipeHolder tile = getPipe(world, pos);
        if (tile == null) {
            return ActionResult.PASS;
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return ActionResult.FAIL;
        }
        if (pipe.getColour() == paintColour || !pipe.definition.canBeColoured) {
            return ActionResult.FAIL;
        } else {
            pipe.setColour(paintColour);
            return ActionResult.SUCCESS;
        }
    }

    // Helpers

    @Nullable
    public static TilePipeHolder getPipe(BlockView access, BlockPos pos) {
        if (access == null) {
            return null;
        }
        BlockEntity tile = access.getBlockEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    /** Called from {@link TilePipeHolder#readPayload} when a NET_CREATE_LANDING_PARTICLE message arrives. */
    @Environment(EnvType.CLIENT)
    public static void spawnLandingParticles(TilePipeHolder tile, double x, double y, double z, int number) {
        // STUB(R.Chen): pipe-flow landing particle spawn restored with the client render layer in Phase 5
        // (Forge used ParticleBlockDust + MinecraftClient.effectRenderer; needs the migrated pipe/pluggable sprites).
    }

    /** Carries the Forge {@code HitResult.subHit} sub-part index, which Yarn's {@link BlockHitResult} lacks.
     * 0 = pipe centre; 1..6 = pipe connection on {@code DIRECTIONS[subHit-1]}; 7..12 = pluggable on that face;
     * 13..20 = wire part; 21+ = wire-between; 400 = full-block fallback. */
    public static final class PipeRayTraceResult extends BlockHitResult {
        public final int subHit;

        public PipeRayTraceResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock, int subHit) {
            super(pos, side, blockPos, insideBlock);
            this.subHit = subHit;
        }
    }
}
