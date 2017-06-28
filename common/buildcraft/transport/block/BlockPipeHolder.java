/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.prop.UnlistedNonNullProperty;

import buildcraft.transport.BCTransportItems;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;

public class BlockPipeHolder extends BlockBCTile_Neptune implements ICustomPaintHandler {
    public static final IUnlistedProperty<WeakReference<TilePipeHolder>> PROP_TILE = new UnlistedNonNullProperty<>(
        "tile");

    private static final AxisAlignedBB BOX_CENTER = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final AxisAlignedBB BOX_DOWN = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.25, 0.75);
    private static final AxisAlignedBB BOX_UP = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75, 1, 0.75);
    private static final AxisAlignedBB BOX_NORTH = new AxisAlignedBB(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    private static final AxisAlignedBB BOX_SOUTH = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    private static final AxisAlignedBB BOX_WEST = new AxisAlignedBB(0, 0.25, 0.25, 0.25, 0.75, 0.75);
    private static final AxisAlignedBB BOX_EAST = new AxisAlignedBB(0.75, 0.25, 0.25, 1, 0.75, 0.75);
    private static final AxisAlignedBB[] BOX_FACES = { BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST };

    public BlockPipeHolder(Material material, String id) {
        super(material, id);

        setHardness(0.25f);
        setResistance(3.0f);
        setLightOpacity(0);
    }

    // basics

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { PROP_TILE });
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePipeHolder();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    // Collisions

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<
        AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isPistonMoving) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
            return;
        }
        boolean added = false;
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_CENTER);
            added = true;
            for (EnumFacing face : EnumFacing.VALUES) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    AxisAlignedBB aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vec3d radius = new Vec3d(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vec3d min = center.subtract(radius);
                        Vec3d max = center.add(radius);
                        aabb = BoundingBoxUtil.makeFrom(min, max);
                    }
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
                }
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AxisAlignedBB bb = pluggable.getBoundingBox();
                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
                added = true;
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, part.boundingBox);
            added = true;
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, between.boundingBox);
            added = true;
        }
        if (!added) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
        }
    }

    @Nullable
    public RayTraceResult rayTrace(World world, BlockPos pos, EntityPlayer player) {
        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        double reachDistance = 5;
        if (player instanceof EntityPlayerMP) {
            reachDistance = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
        }
        Vec3d end = start.add(player.getLookVec().normalize().scale(reachDistance));
        return rayTrace(world, pos, start, end);
    }

    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return rayTrace(world, pos, start, end);
    }

    @Nullable
    public RayTraceResult rayTrace(World world, BlockPos pos, Vec3d start, Vec3d end) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
        }
        RayTraceResult best = null;
        Pipe pipe = tile.getPipe();
        boolean computed = false;
        if (pipe != null) {
            computed = true;
            best = computeTrace(best, pos, start, end, BOX_CENTER, 0);
            for (EnumFacing face : EnumFacing.VALUES) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    AxisAlignedBB aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vec3d radius = new Vec3d(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vec3d min = center.subtract(radius);
                        Vec3d max = center.add(radius);
                        aabb = BoundingBoxUtil.makeFrom(min, max);
                    }
                    best = computeTrace(best, pos, start, end, aabb, face.ordinal() + 1);
                }
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AxisAlignedBB bb = pluggable.getBoundingBox();
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
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
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
            RayTraceResult trace = part.boundingBoxPossible.calculateIntercept(realStart, realEnd);
            if (trace != null) {
                if (best == null) {
                    best = part;
                    dist = trace.hitVec.squareDistanceTo(realStart);
                } else {
                    double nextDist = trace.hitVec.squareDistanceTo(realStart);
                    if (dist > nextDist) {
                        best = part;
                        dist = nextDist;
                    }
                }
            }
        }
        return best;
    }

    private RayTraceResult computeTrace(RayTraceResult lastBest, BlockPos pos, Vec3d start, Vec3d end,
        AxisAlignedBB aabb, int part) {
        RayTraceResult next = super.rayTrace(pos, start, end, aabb);
        if (next == null) {
            return lastBest;
        }
        next.subHit = part;
        if (lastBest == null) {
            return next;
        }
        double distLast = lastBest.hitVec.squareDistanceTo(start);
        double distNext = next.hitVec.squareDistanceTo(start);
        return distLast > distNext ? next : lastBest;
    }

    @Nullable
    public static EnumFacing getPartSideHit(RayTraceResult trace) {
        if (trace.subHit <= 0) {
            return trace.sideHit;
        }
        if (trace.subHit <= 6) {
            return EnumFacing.VALUES[trace.subHit - 1];
        }
        if (trace.subHit <= 6 + 6) {
            return EnumFacing.VALUES[trace.subHit - 1 - 6];
        }
        return null;
    }

    @Nullable
    public static EnumWirePart getWirePartHit(RayTraceResult trace) {
        if (trace.subHit <= 6 + 6) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8) {
            return EnumWirePart.VALUES[trace.subHit - 1 - 6 - 6];
        } else {
            return null;
        }
    }

    @Nullable
    public static EnumWireBetween getWireBetweenHit(RayTraceResult trace) {
        if (trace.subHit <= 6 + 6 + 8) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8 + EnumWireBetween.VALUES.length) {
            return EnumWireBetween.VALUES[trace.subHit - 1 - 6 - 6 - 8];
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return FULL_BLOCK_AABB;
        }
        RayTraceResult trace = Minecraft.getMinecraft().objectMouseOver;
        if (trace == null || trace.subHit < 0 || !pos.equals(trace.getBlockPos())) {
            // Perhaps we aren't the object the mouse is over
            return FULL_BLOCK_AABB;
        }
        int part = trace.subHit;
        AxisAlignedBB aabb = FULL_BLOCK_AABB;
        if (part == 0) {
            aabb = BOX_CENTER;
        } else if (part < 1 + 6) {
            aabb = BOX_FACES[part - 1];
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                EnumFacing face = EnumFacing.VALUES[part - 1];
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0 && conSize != 0.25f) {
                    Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                    Vec3d radius = new Vec3d(0.25, 0.25, 0.25);
                    radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                    Vec3d min = center.subtract(radius);
                    Vec3d max = center.add(radius);
                    aabb = BoundingBoxUtil.makeFrom(min, max);
                }
            }
        } else if (part < 1 + 6 + 6) {
            EnumFacing side = EnumFacing.VALUES[part - 1 - 6];
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
            return aabb.offset(pos);
        } else {
            return (aabb == FULL_BLOCK_AABB ? aabb : aabb.grow(1 / 32.0)).offset(pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TilePipeHolder pipe = getPipe(world, pos, true);
        if (pipe != null) {
            pipe.refreshNeighbours();
            if (pipe.getPipe() != null) {
                pipe.getPipe().markForUpdate();
            }
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
        EntityPlayer player) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null || target == null) {
            return ItemStack.EMPTY;
        }
        if (target.subHit <= 6) {
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                PipeDefinition def = pipe.getDefinition();
                Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
                if (item != null) {
                    int meta = pipe.getColour() == null ? 0 : pipe.getColour().getMetadata() + 1;
                    return new ItemStack(item, 1, meta);
                }
            }
        } else if (target.subHit <= 12) {
            int pluggableHit = target.subHit - 7;
            EnumFacing face = EnumFacing.VALUES[pluggableHit];
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
                return new ItemStack(BCTransportItems.WIRE, 1, tile.wireManager.getColorOfPart(part).getMetadata());
            } else if (between != null && tile.wireManager.getColorOfPart(between.parts[0]) != null) {
                return new ItemStack(BCTransportItems.WIRE, 1, tile.wireManager.getColorOfPart(between.parts[0])
                    .getMetadata());
            }
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
        EnumFacing side, float hitX, float hitY, float hitZ) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        RayTraceResult trace = rayTrace(world, pos, player);
        if (trace == null) {
            return false;
        }
        EnumFacing realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = side;
        }
        if (trace.subHit > 6 && trace.subHit <= 12) {
            PipePluggable existing = tile.getPluggable(realSide);
            if (existing != null) {
                return existing.onPluggableActivate(player, trace, hitX, hitY, hitZ);
            }
        }

        EnumPipePart part = trace.subHit == 0 ? EnumPipePart.CENTER : EnumPipePart.fromFacing(realSide);

        ItemStack held = player.getHeldItem(hand);
        Item item = held.isEmpty() ? null : held.getItem();
        PipePluggable existing = tile.getPluggable(realSide);
        if (item instanceof IItemPluggable && existing == null) {
            IItemPluggable itemPlug = (IItemPluggable) item;
            PipePluggable plug = itemPlug.onPlace(held, tile, realSide, player, hand);
            if (plug == null) {
                return false;
            } else {
                tile.replacePluggable(realSide, plug);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                return true;
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
                wirePart = EnumWirePart.get((trace.hitVec.x % 1 + 1) % 1 > 0.5, (trace.hitVec.y % 1 + 1)
                    % 1 > 0.5, (trace.hitVec.z % 1 + 1) % 1 > 0.5);
            }
            if (wirePart != null && attachTile != null) {
                attachTile.getWireManager().addPart(wirePart, EnumDyeColor.byMetadata(held.getMetadata()));
                attachTile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
        }
        if (tile.getPipe().behaviour.onPipeActivate(player, trace, hitX, hitY, hitZ, part)) {
            return true;
        }
        if (tile.getPipe().flow.onFlowActivate(player, trace, hitX, hitY, hitZ, part)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
        boolean willHarvest) {
        if (world.isRemote) {
            return false;
        }

        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }

        NonNullList<ItemStack> toDrop = NonNullList.create();
        RayTraceResult trace = rayTrace(world, pos, player);
        EnumFacing side = null;
        EnumWirePart part = null;
        EnumWireBetween between = null;

        if (trace != null && trace.subHit > 6) {
            side = getPartSideHit(trace);
            part = getWirePartHit(trace);
            between = getWireBetweenHit(trace);
        }

        if (side != null) {
            removePluggable(side, tile, toDrop);
            if (!player.capabilities.isCreativeMode) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            return false;
        } else if (part != null) {
            toDrop.add(new ItemStack(BCTransportItems.WIRE, 1, tile.wireManager.getColorOfPart(part).getMetadata()));
            tile.wireManager.removePart(part);
            if (!player.capabilities.isCreativeMode) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else if (between != null) {
            toDrop.add(new ItemStack(BCTransportItems.WIRE, between.to == null ? 2 : 1, tile.wireManager.getColorOfPart(
                between.parts[0]).getMetadata()));
            if (between.to == null) {
                tile.wireManager.removeParts(Arrays.asList(between.parts));
            } else {
                tile.wireManager.removePart(between.parts[0]);
            }
            if (!player.capabilities.isCreativeMode) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else {
            toDrop.addAll(getDrops(world, pos, state, 0));
            for (EnumFacing face : EnumFacing.VALUES) {
                removePluggable(face, tile, NonNullList.create());
            }
        }
        if (!player.capabilities.isCreativeMode) {
            InventoryUtil.dropAll(world, pos, toDrop);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        TilePipeHolder tile = getPipe(world, pos, false);
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                pluggable.getDrops(toDrop);
            }
        }
        for (EnumDyeColor color : tile.wireManager.parts.values()) {
            toDrop.add(new ItemStack(BCTransportItems.WIRE, 1, color.getMetadata()));
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.getDrops(toDrop);
        }
        return toDrop;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        if (exploder != null) {
            Vec3d subtract = exploder.getPositionVector().subtract(new Vec3d(pos).add(VecUtil.VEC_HALF)).normalize();
            EnumFacing side = Arrays.stream(EnumFacing.VALUES).min(Comparator.comparing(facing -> new Vec3d(facing
                .getDirectionVec()).distanceTo(subtract))).orElseThrow(IllegalArgumentException::new);
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
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
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
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te,
        ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        PipePluggable pluggable = tile.getPluggable(facing);
        return pluggable != null && pluggable.canBeConnected();
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        PipePluggable pluggable = tile.getPluggable(side);
        return pluggable != null && pluggable.isSideSolid();
    }

    private static void removePluggable(EnumFacing side, TilePipeHolder tile, NonNullList<ItemStack> toDrop) {
        PipePluggable removed = tile.replacePluggable(side, null);
        if (removed != null) {
            removed.onRemove();
            removed.getDrops(toDrop);
        }
    }

    public static TilePipeHolder getPipe(IBlockAccess access, BlockPos pos, boolean requireServer) {
        if (access instanceof World) {
            return getPipe((World) access, pos, requireServer);
        }
        if (requireServer) {
            return null;
        }
        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    public static TilePipeHolder getPipe(World world, BlockPos pos, boolean requireServer) {
        if (requireServer && world.isRemote) {
            return null;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    // Block overrides

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition,
        IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return super.addHitEffects(state, worldObj, target, manager);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return super.addDestroyEffects(world, pos, manager);
    }

    // paint

    @Override
    public EnumActionResult attemptPaint(World world, BlockPos pos, IBlockState state, Vec3d hitPos, EnumFacing hitSide,
        EnumDyeColor paintColour) {
        TilePipeHolder tile = getPipe(world, pos, true);
        if (tile == null) {
            return EnumActionResult.PASS;
        }

        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return EnumActionResult.FAIL;
        }
        if (pipe.getColour() == paintColour) {
            return EnumActionResult.FAIL;
        } else {
            pipe.setColour(paintColour);
            return EnumActionResult.SUCCESS;
        }
    }

    // rendering

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extended = (IExtendedBlockState) state;
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile != null) {
            extended = extended.withProperty(PROP_TILE, new WeakReference<>(tile));
        }
        return extended;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
    }
}
