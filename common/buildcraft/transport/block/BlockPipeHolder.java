/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.*;
import buildcraft.lib.raytrace.RayTraceResultBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.PipeModelCacheBase;
import buildcraft.transport.client.model.PipeModelCachePluggable;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BlockPipeHolder extends BlockBCTile_Neptune<TilePipeHolder> implements ICustomPaintHandler, IBlockWithTickableTE<TilePipeHolder> {
    // public static final IUnlistedProperty<WeakReference<TilePipeHolder>> PROP_TILE = new UnlistedNonNullProperty<>("tile");
    public static final ModelProperty<TilePipeHolder> PROP_TILE = new ModelProperty<>();

    private static final VoxelShape BOX_CENTER = VoxelShapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final VoxelShape BOX_DOWN = VoxelShapes.box(0.25, 0, 0.25, 0.75, 0.25, 0.75);
    private static final VoxelShape BOX_UP = VoxelShapes.box(0.25, 0.75, 0.25, 0.75, 1, 0.75);
    private static final VoxelShape BOX_NORTH = VoxelShapes.box(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    private static final VoxelShape BOX_SOUTH = VoxelShapes.box(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    private static final VoxelShape BOX_WEST = VoxelShapes.box(0, 0.25, 0.25, 0.25, 0.75, 0.75);
    private static final VoxelShape BOX_EAST = VoxelShapes.box(0.75, 0.25, 0.25, 1, 0.75, 0.75);
    private static final VoxelShape[] BOX_FACES = { BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST };

    private static final ResourceLocation ADVANCEMENT_LOGIC_TRANSPORTATION = new ResourceLocation(
            "buildcrafttransport:logic_transportation"
    );

    public BlockPipeHolder(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);

//        setHardness(0.25f);
//        setResistance(3.0f);
//        setLightOpacity(0);
    }

    // basics

    // 1.18.2: use ModelProperty
//    @Override
//    protected BlockStateContainer createBlockState() {
//        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { PROP_TILE });
//    }

    @Override
//    public TileBC_Neptune createTileEntity(BlockPos pos, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return BCTransportBlocks.pipeHolderTile.get().create();
    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

//    @Override
//    public boolean isFullBlock(IBlockState state) {
//        return false;
//    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    // Collisions

    @Override
//    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isPistonMoving)
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return VoxelShapes.block();
        }
        List<VoxelShape> collidingBoxes = new LinkedList<>();
        boolean added = false;
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
//            addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_CENTER);
            collidingBoxes.add(BOX_CENTER);

            added = true;
            for (Direction face : Direction.values()) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    VoxelShape aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vector3d center = VecUtil.offset(new Vector3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vector3d radius = new Vector3d(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vector3d min = center.subtract(radius);
                        Vector3d max = center.add(radius);
                        aabb = BoundingBoxUtil.makeVoxelShapeFrom(min, max);
                    }
//                    addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
                    collidingBoxes.add(aabb);
                }
            }
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                VoxelShape bb = pluggable.getBoundingBox();
//                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
                collidingBoxes.add(bb);
                added = true;
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
//            addCollisionBoxToList(pos, entityBox, collidingBoxes, part.boundingBox);
            collidingBoxes.add(part.boundingBox);
            added = true;
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
//            addCollisionBoxToList(pos, entityBox, collidingBoxes, between.boundingBox);
            collidingBoxes.add(between.boundingBox);
            added = true;
        }
        if (!added) {
//            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
            collidingBoxes.add(VoxelShapes.block());
        }
        return VoxelShapes.or(VoxelShapes.empty(), collidingBoxes.toArray(new VoxelShape[0]));
    }

    @Nullable
//    public RayTraceResult rayTrace(World world, BlockPos pos, EntityPlayer player)
    public RayTraceResultBC rayTrace(IBlockReader world, BlockPos pos, PlayerEntity player) {
//        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vector3d start = player.position().add(0, player.getEyeHeight(), 0);
        double reachDistance = 5;
        if (player instanceof ServerPlayerEntity) {
//            reachDistance = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
            reachDistance = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        }
//        Vec3d end = start.add(player.getLookVec().normalize().scale(reachDistance));
        Vector3d end = start.add(player.getLookAngle().normalize().scale(reachDistance));
        return rayTrace(world, pos, start, end);
    }

    // Calen: 1.18.2 no this method, seems should trace by ourselves
//    @Override
//    @Nullable
//    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
//        return rayTrace(world, pos, start, end);
//    }

    @Nullable
//    public RayTraceResult rayTrace(World world, BlockPos pos, Vec3d start, Vec3d end)
    public RayTraceResultBC rayTrace(IBlockReader world, BlockPos pos, Vector3d start, Vector3d end) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
//            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
            return computeTrace(null, pos, start, end, VoxelShapes.block(), 400);
        }
        RayTraceResultBC best = null;
        Pipe pipe = tile.getPipe();
        boolean computed = false;
        if (pipe != null) {
            computed = true;
            best = computeTrace(best, pos, start, end, BOX_CENTER, 0);
            for (Direction face : Direction.values()) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    VoxelShape aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vector3d center = VecUtil.offset(new Vector3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vector3d radius = new Vector3d(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vector3d min = center.subtract(radius);
                        Vector3d max = center.add(radius);
                        aabb = BoundingBoxUtil.makeVoxelShapeFrom(min, max);
                    }
                    best = computeTrace(best, pos, start, end, aabb, face.ordinal() + 1);
                }
            }
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                VoxelShape bb = pluggable.getBoundingBox();
                best = computeTrace(best, pos, start, end, bb, face.ordinal() + 1 + 6);
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
//            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
            return computeTrace(null, pos, start, end, VoxelShapes.block(), 400);
        }
        return best;
    }

    @Nullable
    public static EnumWirePart rayTraceWire(BlockPos pos, Vector3d start, Vector3d end) {
        Vector3d realStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vector3d realEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        EnumWirePart best = null;
        double dist = 1000;
        for (EnumWirePart part : EnumWirePart.VALUES) {
//            RayTraceResult trace = part.boundingBoxPossible.calculateIntercept(realStart, realEnd);
            RayTraceResult trace = part.boundingBoxPossible.clip(realStart, realEnd, pos);
            if (trace != null) {
                if (best == null) {
                    best = part;
//                    dist = trace.hitVec.squareDistanceTo(realStart);
                    dist = trace.getLocation().distanceToSqr(realStart);
                } else {
//                    double nextDist = trace.hitVec.squareDistanceTo(realStart);
                    double nextDist = trace.getLocation().distanceToSqr(realStart);
                    if (dist > nextDist) {
                        best = part;
                        dist = nextDist;
                    }
                }
            }
        }
        return best;
    }

    private RayTraceResultBC computeTrace(RayTraceResultBC lastBest, BlockPos pos, Vector3d start, Vector3d end, VoxelShape aabb, int part) {
//        RayTraceResult next = super.rayTrace(pos, start, end, aabb);
        RayTraceResultBC next = RayTraceResultBC.fromMcHitResult(AxisAlignedBB.clip(aabb.toAabbs(), start, end, pos));
        if (next == null) {
            return lastBest;
        }
        next.subHit = part;
        if (lastBest == null) {
            return next;
        }
        double distLast = lastBest.getLocation().distanceToSqr(start);
        double distNext = next.getLocation().distanceToSqr(start);
        return distLast > distNext ? next : lastBest;
    }

    @Nullable
    public static Direction getPartSideHit(RayTraceResultBC trace) {
        if (trace.subHit <= 0) {
            return trace.sideHit;
        }
        if (trace.subHit <= 6) {
            return Direction.values()[trace.subHit - 1];
        }
        if (trace.subHit <= 6 + 6) {
            return Direction.values()[trace.subHit - 1 - 6];
        }
        return null;
    }

    @Nullable
    public static EnumWirePart getWirePartHit(RayTraceResultBC trace) {
        if (trace.subHit <= 6 + 6) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8) {
            return EnumWirePart.VALUES[trace.subHit - 1 - 6 - 6];
        } else {
            return null;
        }
    }

    @Nullable
    public static EnumWireBetween getWireBetweenHit(RayTraceResultBC trace) {
        if (trace.subHit <= 6 + 6 + 8) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8 + EnumWireBetween.VALUES.length) {
            return EnumWireBetween.VALUES[trace.subHit - 1 - 6 - 6 - 8];
        } else {
            return null;
        }
    }

    // Calen: this shape is the selected part of block
    @Override
//    @OnlyIn(Dist.CLIENT)
//    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
//            return FULL_BLOCK_AABB;
            return VoxelShapes.block();
        }
        if (!(context instanceof EntitySelectionContext && context.getEntity() instanceof PlayerEntity)) {
            return getInteractionShape(state, world, pos);
        }
//        RayTraceResult trace = Minecraft.getMinecraft().objectMouseOver;
        RayTraceResultBC trace = rayTrace(world, pos, ((PlayerEntity) context.getEntity()));
        if (trace == null || trace.subHit < 0 || !pos.equals(trace.getBlockPos())) {
            // Perhaps we aren't the object the mouse is over
//            return FULL_BLOCK_AABB;
            return getInteractionShape(state, world, pos); // Calen: Should not be full block so that we can collide a next block
        }
        int part = trace.subHit;
//        AxisAlignedBB aabb = FULL_BLOCK_AABB;
        VoxelShape aabb = VoxelShapes.block();
        if (part == 0) {
            aabb = BOX_CENTER;
        } else if (part < 1 + 6) {
            aabb = BOX_FACES[part - 1];
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                Direction face = Direction.values()[part - 1];
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0 && conSize != 0.25f) {
                    Vector3d center = VecUtil.offset(new Vector3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                    Vector3d radius = new Vector3d(0.25, 0.25, 0.25);
                    radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                    Vector3d min = center.subtract(radius);
                    Vector3d max = center.add(radius);
                    aabb = BoundingBoxUtil.makeVoxelShapeFrom(min, max);
                }
            }
        } else if (part < 1 + 6 + 6) {
            Direction side = Direction.values()[part - 1 - 6];
            PipePluggable pluggable = tile.getPluggable(side);
            if (pluggable != null) {
                aabb = pluggable.getBoundingBox();
            }
        } else if (part < 1 + 6 + 6 + 8) {
            EnumWirePart wirePart = EnumWirePart.VALUES[part - 1 - 6 - 6];
            aabb = wirePart.boundingBox;
        } else if (part < 1 + 6 + 6 + 6 + 8 + 36) {
            EnumWireBetween wireBetween = EnumWireBetween.VALUES[part - 1 - 6 - 6 - 8];
            aabb = wireBetween.boundingBox;
        }
        if (part >= 1 + 6 + 6) {
//            return aabb.offset(pos);
            return aabb;
        } else {
//            return (aabb == FULL_BLOCK_AABB ? aabb : aabb.grow(1 / 32.0)).offset(pos);
            return (aabb == VoxelShapes.block() ? aabb : VoxelShapes.create(aabb.bounds().inflate(1 / 32.0)));
        }
    }

    @Override
//    public ItemStack getPickBlock(BlockState state, HitResultBC target, World world, BlockPos pos, PlayerEntity player)
    public ItemStack getPickBlock(BlockState state, RayTraceResult targetIn, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TilePipeHolder tile = getPipe(world, pos, false);
//        if (tile == null || target == null)
        if (tile == null) {
            return ItemStack.EMPTY;
        }

        // Calen: in 1.18.2 we can't create custom RayTraceResult before #getCloneItemStack called (in 1.12.2 that's allowed with #collisionRayTrace)
        RayTraceResultBC target = rayTrace(world, pos, player);

        // Calen: target.getType() may be RayTraceResult.Type.MISS
        if (target == null || target.getType() != RayTraceResult.Type.BLOCK) {
            return StackUtil.EMPTY;
        }

        if (target.subHit <= 6) {
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                PipeDefinition def = pipe.getDefinition();
//                Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
                Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(def, pipe.getColour());
                if (item != null) {
                    // Calen: different item object for each colour
                    // pipe.getColour() instead of meta
//                    int meta = pipe.getColour() == null ? 0 : pipe.getColour().getMetadata() + 1;
//                    return new ItemStack(item, 1, meta);
                    return new ItemStack(item, 1);
                }
            }
        } else if (target.subHit <= 12) {
            int pluggableHit = target.subHit - 7;
            Direction face = Direction.values()[pluggableHit];
            PipePluggable plug = tile.getPluggable(face);
            if (plug != null) {
                return plug.getPickStack();
            }
        } else {
            EnumWirePart part = null;
            EnumWireBetween between = null;

            if (target.subHit > 6) {
                part = getWirePartHit(target);
                between = getWireBetweenHit(target);
            }

            if (part != null && tile.wireManager.getColorOfPart(part) != null) {
                ItemStack stack = new ItemStack(BCTransportItems.wire.get(), 1);
                ColourUtil.addColourTagToStack(stack, tile.wireManager.getColorOfPart(part).getId());
                return stack;
            } else if (between != null && tile.wireManager.getColorOfPart(between.parts[0]) != null) {
                ItemStack stack = new ItemStack(BCTransportItems.wire.get(), 1);
                ColourUtil.addColourTagToStack(stack, tile.wireManager.getColorOfPart(between.parts[0]).getId());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        float hitX = hitResult.getBlockPos().getX();
        float hitY = hitResult.getBlockPos().getY();
        float hitZ = hitResult.getBlockPos().getZ();
        Direction side = hitResult.getDirection();
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
//            return false;
            return ActionResultType.PASS;
        }
        RayTraceResultBC trace = rayTrace(world, pos, player);
        if (trace == null) {
//            return false;
            return ActionResultType.PASS;
        }
        Direction realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = side;
        }
        if (trace.subHit > 6 && trace.subHit <= 12) {
            PipePluggable existing = tile.getPluggable(realSide);
            if (existing != null) {
//                return existing.onPluggableActivate(player, trace, hitX, hitY, hitZ);
                return existing.onPluggableActivate(player, trace, hitX, hitY, hitZ) ?
                        ActionResultType.SUCCESS : ActionResultType.FAIL;
            }
        }

        EnumPipePart part = trace.subHit == 0 ? EnumPipePart.CENTER : EnumPipePart.fromFacing(realSide);

        ItemStack held = player.getItemInHand(hand);
        Item item = held.isEmpty() ? null : held.getItem();
        PipePluggable existing = tile.getPluggable(realSide);
        if (item instanceof IItemPluggable && existing == null) {
            IItemPluggable itemPlug = (IItemPluggable) item;
            PipePluggable plug = itemPlug.onPlace(held, tile, realSide, player, hand);
            if (plug == null) {
//                return false;
                return ActionResultType.PASS;
            } else {
                tile.replacePluggable(realSide, plug);
                plug.onPlacedBy(player);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
//                return true;
                return ActionResultType.SUCCESS;
            }
        }
        if (item instanceof ItemWire) {
            EnumWirePart wirePartHit = getWirePartHit(trace);
            EnumWirePart wirePart;
            TilePipeHolder attachTile = tile;
            if (wirePartHit != null) {
                WireNode node = new WireNode(pos, wirePartHit);
                node = node.offset(trace.sideHit);
                wirePart = node.part;
                if (!node.pos.equals(pos)) {
                    attachTile = getPipe(world, node.pos, false);
                }
            } else {
                wirePart = EnumWirePart.get((trace.getLocation().x % 1 + 1) % 1 > 0.5, (trace.getLocation().y % 1 + 1) % 1 > 0.5,
                        (trace.getLocation().z % 1 + 1) % 1 > 0.5);
            }
            if (wirePart != null && attachTile != null) {
//                EnumDyeColor colour = EnumDyeColor.byMetadata(held.getMetadata());
                DyeColor colour = ColourUtil.getStackColourFromTag(held);
                boolean attached =
                        attachTile.getWireManager().addPart(wirePart, colour);
                attachTile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
                if (attached) {
                    WireNode from = new WireNode(attachTile.getPipePos(), wirePart);

                    boolean isNowConnected = false;
                    for (Direction dir : Direction.values()) {
                        WireNode to = from.offset(dir);
                        if (to.pos == attachTile.getPipePos()) {
                            if (attachTile.getWireManager().getColorOfPart(to.part) == colour) {
                                isNowConnected = true;
                                break;
                            }
                        } else {
                            TileEntity localTile = attachTile.getLocalTile(to.pos);
                            if (localTile instanceof TilePipeHolder) {
                                if (((TilePipeHolder) localTile).getWireManager().getColorOfPart(to.part) == colour) {
                                    isNowConnected = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isNowConnected) {
                        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_LOGIC_TRANSPORTATION);
                    }

                    if (!player.isCreative()) {
                        held.shrink(1);
                    }
                }
                if (attached) {
//                    return true;
                    return ActionResultType.SUCCESS;
                }
            }
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
//            return false;
            return ActionResultType.PASS;
        }
        if (pipe.behaviour.onPipeActivate(player, trace, hitX, hitY, hitZ, part)) {
//            return true;
            return ActionResultType.SUCCESS;
        }
        if (pipe.flow.onFlowActivate(player, trace, hitX, hitY, hitZ, part)) {
//            return true;
            return ActionResultType.SUCCESS;
        }
//        return false;
        return ActionResultType.PASS;
    }

    @Override
//    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest)
    public boolean removedByPlayer(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (worldIn.isClientSide) {
//            return false;
            // Calen: to call #addDestroyEffects in Client Thread to spawn particles
            // in 1.18.2, without #playerWillDestroy, the particle will not spawn, different to 1.12.2
            playerWillDestroy(worldIn, pos, state, player);
            return false;
        }

        ServerWorld world = (ServerWorld) worldIn;

        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
//            return super.removedByPlayer(state, level, pos, player, willHarvest);
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }

        NonNullList<ItemStack> toDrop = NonNullList.create();
        RayTraceResultBC trace = rayTrace(world, pos, player);
        Direction side = null;
        EnumWirePart part = null;
        EnumWireBetween between = null;

        if (trace != null && trace.subHit > 6) {
            side = getPartSideHit(trace);
            part = getWirePartHit(trace);
            between = getWireBetweenHit(trace);
        }

        if (side != null) {
            removePluggable(side, tile, toDrop);
            if (!player.isCreative()) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            return false;
        } else if (part != null) {
            ItemStack stack = new ItemStack(BCTransportItems.wire.get(), 1);
            ColourUtil.addColourTagToStack(stack, tile.wireManager.getColorOfPart(part).getId());
            toDrop.add(stack);
            tile.wireManager.removePart(part);
            if (!player.isCreative()) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else if (between != null) {
            ItemStack stack = new ItemStack(BCTransportItems.wire.get(), between.to == null ? 2 : 1);
            ColourUtil.addColourTagToStack(stack, tile.wireManager.getColorOfPart(between.parts[0]).getId());
            toDrop.add(stack);
            if (between.to == null) {
                tile.wireManager.removeParts(Arrays.asList(between.parts));
            } else {
                tile.wireManager.removePart(between.parts[0]);
            }
            if (!player.isCreative()) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else {
//            toDrop.addAll(getDrops(world, pos, state, 0));
            toDrop.addAll(getDrops(state, world, pos));
            for (Direction face : Direction.values()) {
                removePluggable(face, tile, NonNullList.create());
            }
        }
        if (!player.isCreative()) {
            InventoryUtil.dropAll(world, pos, toDrop);
        }
//        return super.removedByPlayer(state, world, pos, player, willHarvest);
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    // Calen: if overrides getDrops(BlockState state, LootContext.IBuilder builder), world.getBlockEntity(pos) will be null when MC calls this method
//    @Override
//    public void getDrops(NonNullList<ItemStack> toDrop, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    private List<ItemStack> getDrops(BlockState state, World world, BlockPos pos) {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            BCLog.logger.warn("[transport.pipe.holder] Tried to remove TileEntity of block [" + state + "] at [" + pos + "] but fount no TileEntity!");
            return toDrop;
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
//                pluggable.addDrops(toDrop, fortune);
                pluggable.addDrops(toDrop, 0);
            }
        }
        for (DyeColor color : tile.wireManager.parts.values()) {
            ItemStack stack = new ItemStack(BCTransportItems.wire.get(), 1);
            ColourUtil.addColourTagToStack(stack, color.getId());
            toDrop.add(stack);
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
//            pipe.addDrops(toDrop, fortune);
            pipe.addDrops(toDrop, 0);
        }
        return toDrop;
    }

    @Override
//    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        Entity exploder = explosion.getExploder();
        if (exploder != null) {
            Vector3d subtract = exploder.position().subtract(Vector3d.atLowerCornerOf(pos).add(VecUtil.VEC_HALF)).normalize();
            Direction side = Arrays.stream(Direction.values())
                    .min(Comparator.comparing(facing -> Vector3d.atLowerCornerOf(facing.getNormal()).distanceTo(subtract)))
                    .orElseThrow(IllegalArgumentException::new);
            TilePipeHolder tile = getPipe(world, pos, true);
            if (tile != null) {
                PipePluggable pluggable = tile.getPluggable(side);
                if (pluggable != null) {
                    float explosionResistance = pluggable.getExplosionResistance(exploder, explosion);
                    if (explosionResistance > 0) {
                        return explosionResistance;
                    }
                }
            }
        }
//        return super.getExplosionResistance(world, pos, exploder, explosion);
        return super.getExplosionResistance(state, world, pos, explosion);
    }

    @Override
//    public void onEntityCollidedWithBlock(World world, BlockPos pos, BlockState state, Entity entity)
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return;
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.getBehaviour().onEntityCollide(entity);
        }
    }

    @Override
//    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack)
    public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
//        player.addStat(StatList.getBlockStats(this));
        player.awardStat(Stats.BLOCK_MINED.get(this));
//        player.addExhaustion(0.005F);
        player.causeFoodExhaustion(0.005F);
        dropResources(state, world, pos, te, player, stack);
    }

    // Calen: it seems no this method in 1.18.2
//    @Override
//    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
//        TilePipeHolder tile = getPipe(world, pos, false);
//        if (tile == null) {
//            return false;
//        }
//        PipePluggable pluggable = tile.getPluggable(facing);
//        return pluggable != null && pluggable.canBeConnected();
//    }

//    @Override
//    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
//        TilePipeHolder tile = getPipe(world, pos, false);
//        if (tile == null) {
//            return false;
//        }
//        PipePluggable pluggable = tile.getPluggable(side);
//        return pluggable != null && pluggable.isSideSolid();
//    }

//    @Override
//    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
//        TilePipeHolder tile = getPipe(world, pos, false);
//        if (tile == null) {
//            return BlockFaceShape.UNDEFINED;
//        }
//        PipePluggable pluggable = tile.getPluggable(face);
//        return pluggable != null ? pluggable.getBlockFaceShape() : BlockFaceShape.UNDEFINED;
//    }

    private static void removePluggable(Direction side, TilePipeHolder tile, NonNullList<ItemStack> toDrop) {
        PipePluggable removed = tile.replacePluggable(side, null);
        if (removed != null) {
            removed.onRemove();
            removed.addDrops(toDrop, 0);
        }
    }

    // public static TilePipeHolder getPipe(IBlockAccess access, BlockPos pos, boolean requireServer)
    public static TilePipeHolder getPipe(IBlockReader access, BlockPos pos, boolean requireServer) {
        if (access instanceof World) {
            return getPipe((World) access, pos, requireServer);
        }
        if (requireServer) {
            return null;
        }
        TileEntity tile = access.getBlockEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    public static TilePipeHolder getPipe(World world, BlockPos pos, boolean requireServer) {
        if (requireServer && world.isClientSide) {
            return null;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    // Block overrides

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld worldObj, BlockPos blockPosition, BlockState iblockstate, LivingEntity entity, int numberOfParticles) {
        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
    }

    @OnlyIn(Dist.CLIENT)
    private static HitSpriteInfo getHitSpriteInfo(RayTraceResultBC target, TilePipeHolder pipeHolder) {
        int p = target.subHit;
        VoxelShape aabb = null;
        TextureAtlasSprite sprite = SpriteUtil.missingSprite();
        if (0 <= p && p <= 6) {
            aabb = p == 0 ? BOX_CENTER : BOX_FACES[p - 1];
            PipeDefinition def = pipeHolder.getPipe().definition;
            TextureAtlasSprite[] sprites = PipeModelCacheBase.generator.getItemSprites(def);
            sprite = sprites.length == 0 ? SpriteUtil.missingSprite() : sprites[0];
        } else if (6 + 1 <= p && p < 6 + 6 + 1) {
            PipePluggable plug = pipeHolder.getPluggable(Direction.values()[p - 6 - 1]);
            if (plug == null) {
                return null;
            }
            aabb = plug.getBoundingBox();
            if (aabb == null) {
                return null;
            }
            PluggableModelKey keyC = plug.getModelRenderKey(RenderType.cutout());
            PluggableModelKey keyT = plug.getModelRenderKey(RenderType.translucent());
            if (keyC == null && keyT == null) {
                return null;
            }
            List<BakedQuad> quads = null;
            if (keyC != null) quads = PipeModelCachePluggable.cacheCutoutSingle.bake(keyC);
            if (quads == null || quads.isEmpty()) {
                if (keyT == null) {
                    return null;
                }
                quads = PipeModelCachePluggable.cacheTranslucentSingle.bake(keyT);
                if (quads == null || quads.isEmpty()) {
                    return null;
                }
            }
            sprite = quads.get(0).getSprite();
        } else if (6 + 6 + 1 <= p && p < 1 + 6 + 6 + 8) {
            EnumWirePart wirePart = EnumWirePart.values()[p - 6 - 6 - 1];
            aabb = wirePart.boundingBox;
            DyeColor colour = pipeHolder.getWireManager().getColorOfPart(wirePart);
            if (colour == null) {
                return null;
            }
            sprite = PipeWireRenderer.getWireSprite(colour).getSprite();
        } else if (6 + 6 + 1 + 8 < p && p <= 6 + 6 + 1 + 8 + 36) {
            EnumWireBetween wireBetween = EnumWireBetween.values()[p - 6 - 6 - 1 - 8];
            aabb = wireBetween.boundingBox;
            DyeColor colour = pipeHolder.getWireManager().betweens.get(wireBetween);
            if (colour == null) {
                return null;
            }
            sprite = PipeWireRenderer.getWireSprite(colour).getSprite();
        } else {
            return null;
        }
        if (aabb == null) {
            throw new IllegalStateException("Null aabb for index " + p + " (and sprite " + sprite + ")");
        }
        return new HitSpriteInfo(aabb.bounds(), sprite);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
//            public boolean addHitEffects(BlockState state, World world, HitResultBC target, ParticleManager manager)
    public boolean addHitEffects(BlockState state, World worldIn, RayTraceResult targetIn, ParticleManager manager) {
        ClientWorld world = (ClientWorld) worldIn;
        RayTraceResultBC target = rayTrace(world, ((BlockRayTraceResult) targetIn).getBlockPos(), Minecraft.getInstance().player);
        TileEntity te = world.getBlockEntity(target.getBlockPos());
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = ((TilePipeHolder) te);
            HitSpriteInfo info = getHitSpriteInfo(target, pipeHolder);

            if (info == null) {
                return false;
            }

            double x = Math.random() * (info.aabb.maxX - info.aabb.minX) + info.aabb.minX;
            double y = Math.random() * (info.aabb.maxY - info.aabb.minY) + info.aabb.minY;
            double z = Math.random() * (info.aabb.maxZ - info.aabb.minZ) + info.aabb.minZ;

            switch (target.sideHit) {
                case DOWN:
                    y = info.aabb.minY - 0.1;
                    break;
                case UP:
                    y = info.aabb.maxY + 0.1;
                    break;
                case NORTH:
                    z = info.aabb.minZ - 0.1;
                    break;
                case SOUTH:
                    z = info.aabb.maxZ + 0.1;
                    break;
                case WEST:
                    x = info.aabb.minX - 0.1;
                    break;
                default:
                    x = info.aabb.maxX + 0.1;
                    break;
            }

            x += target.getBlockPos().getX();
            y += target.getBlockPos().getY();
            z += target.getBlockPos().getZ();

//                    ParticleDigging particle = new ParticleDigging(world, x, y, z, 0, 0, 0, state);
            BreakingParticle particle = new BreakingParticle(world, x, y, z, 0, 0, 0, StackUtil.EMPTY);
//                    particle.setBlockPos(target.getBlockPos());
            particle.setPos(x, y, z);
//                    particle.setParticleTexture(info.sprite);
            particle.setSprite(info.sprite);
//                    particle.multiplyVelocity(0.2F);
            particle.setPower(0.2F);
//                    particle.multipleParticleScaleBy(0.6F);
            particle.scale(0.6F);
//                    manager.addEffect(particle);
            manager.add(particle);

            return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World worldIn, BlockPos pos, ParticleManager manager) {
        ClientWorld world = (ClientWorld) worldIn;
        RayTraceResultBC hitResult = rayTrace(world, pos, Minecraft.getInstance().player);
        if (hitResult == null || !pos.equals(hitResult.getBlockPos())) {
            return false;
        }
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = (TilePipeHolder) te;
            HitSpriteInfo info = getHitSpriteInfo(hitResult, pipeHolder);
            if (info == null) {
                return false;
            }

            double sizeX = info.aabb.maxX - info.aabb.minX;
            double sizeY = info.aabb.maxY - info.aabb.minY;
            double sizeZ = info.aabb.maxZ - info.aabb.minZ;

            int countX = (int) Math.max(2, 4 * sizeX);
            int countY = (int) Math.max(2, 4 * sizeY);
            int countZ = (int) Math.max(2, 4 * sizeZ);

//                    BlockState state = world.getBlockState(pos);
            for (int x = 0; x < countX; x++) {
                for (int y = 0; y < countY; y++) {
                    for (int z = 0; z < countZ; z++) {

                        double _x = pos.getX() + info.aabb.minX + (x + 0.5) * sizeX / countX;
                        double _y = pos.getY() + info.aabb.minY + (y + 0.5) * sizeY / countY;
                        double _z = pos.getZ() + info.aabb.minZ + (z + 0.5) * sizeZ / countZ;

//                                ParticleDigging particle = new ParticleDigging(world, _x, _y, _z, 0, 0, 0, state);
                        BreakingParticle particle = new BreakingParticle(world, _x, _y, _z, 0, 0, 0, StackUtil.EMPTY);
                        // 1.18.2: if we use pos, the particle will spawn at the corner of the block
//                                particle.setBlockPos(pos);
                        particle.setPos(_x, _y, _z);
//                                particle.setParticleTexture(info.sprite);
                        particle.setSprite(info.sprite);
//                                manager.addEffect(particle);
                        manager.add(particle);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private static final class HitSpriteInfo {
        final AxisAlignedBB aabb;
        final TextureAtlasSprite sprite;

        HitSpriteInfo(AxisAlignedBB aabb, TextureAtlasSprite sprite) {
            this.aabb = aabb;
            this.sprite = sprite;
        }
    }

    // paint

    @Override
    public ActionResultType attemptPaint(World world, BlockPos pos, BlockState state, Vector3d hitPos, Direction hitSide, DyeColor paintColour) {
        TilePipeHolder tile = getPipe(world, pos, true);
        if (tile == null) {
            return ActionResultType.PASS;
        }

        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return ActionResultType.FAIL;
        }
        if (pipe.getColour() == paintColour || !pipe.definition.canBeColoured) {
            return ActionResultType.FAIL;
        } else {
            pipe.setColour(paintColour);
            return ActionResultType.SUCCESS;
        }
    }

    // rendering

    // 1.18.2: moved to TilePipeHolder#getModelData
//    @Override
//    @SideOnly(Side.CLIENT)
//    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
//        IExtendedBlockState extended = (IExtendedBlockState) state;
//        TilePipeHolder tile = getPipe(world, pos, false);
//        if (tile != null) {
//            extended = extended.withProperty(PROP_TILE, new WeakReference<>(tile));
//        }
//        return extended;
//    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
//        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
//    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if (side == null) return false;
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile != null) {
            PipePluggable pluggable = tile.getPluggable(side.getOpposite());
            return pluggable != null && pluggable.canConnectToRedstone(side);
        }
        return false;
    }

    @Override
//    public boolean canProvidePower(BlockState state)
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
//    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    public int getDirectSignal(@Nonnull BlockState blockState, @Nonnull IBlockReader blockAccess, @Nonnull BlockPos pos, @Nonnull Direction side) {
        if (side == null) {
            return 0;
        }
        TilePipeHolder tile = getPipe(blockAccess, pos, false);
        if (tile != null) {
            return tile.getRedstoneOutput(side.getOpposite());
        }
        return 0;
    }

//    @Override
//    public boolean isBlockNormalCube(IBlockState state) {
//        return false;
//    }

    @Override
//    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    public int getSignal(@Nonnull BlockState blockState, @Nonnull IBlockReader blockAccess, @Nonnull BlockPos pos, @Nonnull Direction side) {
//        return getStrongPower(blockState, blockAccess, pos, side);
        return getDirectSignal(blockState, blockAccess, pos, side);
    }
}
