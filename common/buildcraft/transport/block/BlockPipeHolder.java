package buildcraft.transport.block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.prop.UnlistedNonNullProperty;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.api_move.EnumWirePart;
import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.plug.PluggableBlocker;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;
import buildcraft.transport.wire.WireManager;

public class BlockPipeHolder extends BlockBCTile_Neptune implements ICustomPaintHandler {
    public static final IUnlistedProperty<WeakReference<TilePipeHolder>> PROP_TILE = new UnlistedNonNullProperty<>("tile");

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

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { PROP_TILE });
    }

    // common

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

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return;
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_CENTER);
            for (EnumFacing face : EnumFacing.VALUES) {
                if (pipe.isConnected(face)) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_FACES[face.ordinal()]);
                }
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AxisAlignedBB bb = pluggable.getBoundingBox();
                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
            }
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
            return null;
        }
        RayTraceResult best = null;
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            best = computeTrace(best, pos, start, end, BOX_CENTER, 0);
            for (EnumFacing face : EnumFacing.VALUES) {
                if (pipe.isConnected(face)) {
                    best = computeTrace(best, pos, start, end, BOX_FACES[face.ordinal()], face.ordinal() + 1);
                }
            }
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AxisAlignedBB bb = pluggable.getBoundingBox();
                best = computeTrace(best, pos, start, end, bb, face.ordinal() + 7);
            }
        }
        WireManager wires = tile.getWireManager();
        for (EnumWirePart part : EnumWirePart.VALUES) {
            if (wires.getWireByPart(part) != null) {
                best = computeTrace(best, pos, start, end, part.boundingBox, part.ordinal() + 13);
            }
        }
        // for (EnumWireBetween wire : EnumWireBetween.VALUES) {
        // best = computeTrace(best, pos, start, end, wire.boundingBox, wire.ordinal() + 21);
        // }
        return best;
    }

    private RayTraceResult computeTrace(RayTraceResult lastBest, BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB aabb, int part) {
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
        if (trace.subHit == 0) {
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
        } else if (trace.subHit <= 6 + 6 + 16) {
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
            return null;
        }
        int part = Minecraft.getMinecraft().objectMouseOver.subHit;
        AxisAlignedBB aabb = null;
        if (part == 0) {
            aabb = BOX_CENTER;
        } else if (part < 1 + 6) {
            aabb = BOX_FACES[part - 1];
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
            EnumWireBetween wirePart = EnumWireBetween.VALUES[part - 1 - 6 - 6 - 8];
            aabb = wirePart.boundingBox;
        }
        return aabb == null ? null : aabb.expandXyz(1 / 32.0).offset(pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
        TilePipeHolder pipe = getPipe(world, pos, true);
        if (pipe != null && pipe.getPipe() != null) {
            pipe.getPipe().markForUpdate();
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TilePipeHolder tile = getPipe(world, pos, false);
        Pipe pipe = tile == null ? null : tile.getPipe();
        if (pipe != null) {
            PipeDefinition def = pipe.getDefinition();
            Item item = (Item) PipeAPI.pipeRegistry.getItemForPipe(def);
            if (item != null) {
                int meta = pipe.getColour() == null ? 0 : pipe.getColour().getMetadata() + 1;
                return new ItemStack(item, 1, meta);
            }
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack held, EnumFacing side, float hitX, float hitY, float hitZ) {
        TilePipeHolder tile = getPipe(world, pos, true);
        if (tile == null) {
            return false;
        }
        RayTraceResult trace = rayTrace(world, pos, player);
        EnumFacing realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = side;
        }
        if (realSide == null) {
            return false;
        }
        if (tile.getPluggable(realSide) != null) {
            return false;
        }
        if (held != null && held.getItem() == BCTransportItems.plugStop) {
            // TODO: Add custom items for addition
            tile.replacePluggable(realSide, PluggableBlocker.CREATOR.createPluggable(tile, realSide));
            SoundUtil.playBlockPlace(world, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult trace = rayTrace(world, pos, player);
        EnumFacing side = null;

        if (trace != null && trace.subHit > 6) {
            side = getPartSideHit(trace);
        }

        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile != null) {
            if (side != null) {
                removePluggable(world, pos, player, side, tile);
                return false;
            } else {
                for (EnumFacing face : EnumFacing.VALUES) {
                    removePluggable(world, pos, player, face, tile);
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    private static void removePluggable(World world, BlockPos pos, EntityPlayer player, EnumFacing side, TilePipeHolder tile) {
        PipePluggable removed = tile.replacePluggable(side, null);
        if (removed != null) {
            List<ItemStack> toDrop = new ArrayList<>();
            removed.onRemove(toDrop);
            if (!world.isRemote && !player.capabilities.isCreativeMode) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
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

    // paint

    @Override
    public EnumActionResult attemptPaint(World world, BlockPos pos, IBlockState state, Vec3d hitPos, EnumFacing hitSide, EnumDyeColor paintColour) {
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
            extended = extended.withProperty(PROP_TILE, new WeakReference<>((TilePipeHolder) tile));
        }
        return extended;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
    }
}
