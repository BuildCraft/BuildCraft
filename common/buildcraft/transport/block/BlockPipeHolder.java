package buildcraft.transport.block;

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
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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
import buildcraft.lib.prop.UnlistedNonNullProperty;
import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;

public class BlockPipeHolder extends BlockBCTile_Neptune implements ICustomPaintHandler {
    public static final IUnlistedProperty<PipeModelKey> PROP_MODEL = new UnlistedNonNullProperty<>("model");

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

        setHardness(1.0f);
        setResistance(3.0f);
        setLightOpacity(0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { PROP_MODEL });
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

    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
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
            EnumFacing side = EnumFacing.VALUES[part - 1 - 6 - 6];
            PipePluggable pluggable = tile.getPluggable(side);
            if (pluggable != null) {
                aabb = pluggable.getBoundingBox();
            }
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
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TilePipeHolder) {
            TilePipeHolder holder = (TilePipeHolder) tile;
            if (holder.getPipe() != null) {
                // TODO: this needs to be a full model key, not just the pipe shape
                PipeModelKey key = ((TilePipeHolder) tile).getPipe().getModel();
                extended = extended.withProperty(PROP_MODEL, key);
            }
        }
        return extended;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
    }
}
