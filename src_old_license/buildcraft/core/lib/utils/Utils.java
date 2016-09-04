/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.power.IEngine;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.*;
import buildcraft.core.internal.IDropControlInventory;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.lib.XorShift128Random;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterable;

/** Use buildcraft.lib.misc.*Util instead of this */
@Deprecated
public final class Utils {
    // Commonly used vectors
    public static final BlockPos POS_ZERO = BlockPos.ORIGIN;
    public static final BlockPos POS_ONE = vec3i(1);

    public static final Vec3d VEC_ZERO = vec3(0);
    public static final Vec3d VEC_HALF = vec3(0.5);
    public static final Vec3d VEC_ONE = vec3(1);

    public static final boolean CAULDRON_DETECTED;
    public static final XorShift128Random RANDOM = new XorShift128Random();
    public static final Random ACTUAL_RANDOM = new Random();

    private static final List<EnumFacing> directions = new ArrayList<>(Arrays.asList(EnumFacing.VALUES));
    private static final Map<Axis, Map<Axis, Axis>> axisOtherMap;

    static {
        boolean cauldron = false;
        try {
            cauldron = Utils.class.getClassLoader().loadClass("org.spigotmc.SpigotConfig") != null;
        } catch (ClassNotFoundException e) {

        }
        CAULDRON_DETECTED = cauldron;

        axisOtherMap = Maps.newEnumMap(Axis.class);
        for (Axis a : Axis.values()) {
            Map<Axis, Axis> tempMap = Maps.newEnumMap(Axis.class);
            axisOtherMap.put(a, tempMap);
            for (Axis b : Axis.values()) {
                EnumSet<Axis> axisSet = EnumSet.<Axis> of(a, b);
                axisSet = EnumSet.complementOf(axisSet);
                tempMap.put(b, axisSet.iterator().next());
            }
        }
    }

    public static boolean isRegistered(Block block) {
        return block != null && Block.getIdFromBlock(block) >= 0;
    }

    public static boolean isRegistered(Item item) {
        return item != null && Item.getIdFromItem(item) >= 0;
    }

    public static boolean isRegistered(ItemStack stack) {
        return stack != null && isRegistered(stack.getItem());
    }

    /** Tries to add the passed stack to any valid inventories around the given coordinates.
     *
     * @param stack
     * @param world
     * @param x
     * @param y
     * @param z
     * @return amount used */
    public static int addToRandomInventoryAround(World world, BlockPos pos, ItemStack stack) {
        Collections.shuffle(directions);
        for (EnumFacing orientation : directions) {
            BlockPos newpos = pos.offset(orientation);

            TileEntity tile = world.getTileEntity(newpos);
            ITransactor transactor = Transactor.getTransactorFor(tile, orientation.getOpposite());
            if (transactor != null && !(tile instanceof IEngine) && transactor.insert(stack, false).stackSize > 0) {// FIXME
                                                                                                                    // WRONG
                                                                                                                    // OMG
                                                                                                                    // 1.9.4
                                                                                                                    // port
                                                                                                                    // messed
                                                                                                                    // this
                                                                                                                    // ITransactor
                                                                                                                    // UP!
                return transactor.insert(stack, true).stackSize;
            }
        }
        return 0;

    }

    /** Returns the cardinal direction of the entity depending on its rotationYaw */
    @Deprecated
    public static EnumFacing get2dOrientation(EntityLivingBase entityliving) {
        return entityliving.getHorizontalFacing();
        // EnumFacing[] orientationTable = { EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST };
        // int orientationIndex = MathHelper.floor_double((entityliving.rotationYaw + 45.0) / 90.0) & 3;
        // return orientationTable[orientationIndex];
    }

    /** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
     * around. Will make sure that the location from which the items are coming from (identified by the from parameter)
     * isn't used again so that entities doesn't go backwards. Returns true if successful, false otherwise. */
    public static int addToRandomInjectableAround(World world, BlockPos pos, EnumFacing from, ItemStack stack) {
        List<IInjectable> possiblePipes = new ArrayList<>();
        List<EnumFacing> pipeDirections = new ArrayList<>();

        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getOpposite() == from) {
                continue;
            }

            BlockPos newpos = pos.offset(side);

            TileEntity tile = world.getTileEntity(newpos);

            if (tile instanceof IInjectable) {
                if (!((IInjectable) tile).canInjectItems(side.getOpposite())) {
                    continue;
                }

                possiblePipes.add((IInjectable) tile);
                pipeDirections.add(side.getOpposite());
            } else {
                IInjectable wrapper = CompatHooks.INSTANCE.getInjectableWrapper(tile, side);
                if (wrapper != null) {
                    possiblePipes.add(wrapper);
                    pipeDirections.add(side.getOpposite());
                }
            }
        }

        if (possiblePipes.size() > 0) {
            int choice = RANDOM.nextInt(possiblePipes.size());

            IInjectable pipeEntry = possiblePipes.get(choice);

            return pipeEntry.injectItem(stack, true, pipeDirections.get(choice), null);
        }
        return 0;
    }

    public static void dropTryIntoPlayerInventory(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        if (player != null && player.inventory.addItemStackToInventory(stack)) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
        InvUtils.dropItems(world, stack, pos);
    }

    public static IAreaProvider getNearbyAreaProvider(World world, BlockPos pos) {
        for (Object t : world.loadedTileEntityList) {
            if (t instanceof ITileAreaProvider && ((ITileAreaProvider) t).isValidFromLocation(pos)) {
                return (IAreaProvider) t;
            }
        }

        return null;
    }

    public static EntityLaser createLaser(World world, Vec3d p1, Vec3d p2, LaserKind kind) {
        if (p1.equals(p2)) {
            return null;
        }
        EntityLaser block = new EntityLaser(world, p1, p2, kind);
        return block;
    }

    public static EntityLaser[] createLaserBox(World world, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, LaserKind kind) {
        EntityLaser[] lasers = new EntityLaser[12];
        Vec3d[] p = new Vec3d[8];

        p[0] = new Vec3d(xMin, yMin, zMin);
        p[1] = new Vec3d(xMax, yMin, zMin);
        p[2] = new Vec3d(xMin, yMax, zMin);
        p[3] = new Vec3d(xMax, yMax, zMin);
        p[4] = new Vec3d(xMin, yMin, zMax);
        p[5] = new Vec3d(xMax, yMin, zMax);
        p[6] = new Vec3d(xMin, yMax, zMax);
        p[7] = new Vec3d(xMax, yMax, zMax);

        lasers[0] = Utils.createLaser(world, p[0], p[1], kind);
        lasers[1] = Utils.createLaser(world, p[0], p[2], kind);
        lasers[2] = Utils.createLaser(world, p[2], p[3], kind);
        lasers[3] = Utils.createLaser(world, p[1], p[3], kind);
        lasers[4] = Utils.createLaser(world, p[4], p[5], kind);
        lasers[5] = Utils.createLaser(world, p[4], p[6], kind);
        lasers[6] = Utils.createLaser(world, p[5], p[7], kind);
        lasers[7] = Utils.createLaser(world, p[6], p[7], kind);
        lasers[8] = Utils.createLaser(world, p[0], p[4], kind);
        lasers[9] = Utils.createLaser(world, p[1], p[5], kind);
        lasers[10] = Utils.createLaser(world, p[2], p[6], kind);
        lasers[11] = Utils.createLaser(world, p[3], p[7], kind);

        return lasers;
    }

    public static LaserData[] createLaserDataBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        return createLaserDataBox(new Vec3d(xMin, yMin, zMin), new Vec3d(xMax, yMax, zMax));
    }

    public static LaserData[] createLaserDataBox(Vec3d min, Vec3d max) {
        LaserData[] lasers = new LaserData[12];
        Vec3d[] p = new Vec3d[8];

        p[0] = min;// ___
        p[1] = new Vec3d(max.xCoord, min.yCoord, min.zCoord);
        p[2] = new Vec3d(min.xCoord, max.yCoord, min.zCoord);
        p[3] = new Vec3d(max.xCoord, max.yCoord, min.zCoord);
        p[4] = new Vec3d(min.xCoord, min.yCoord, max.zCoord);
        p[5] = new Vec3d(max.xCoord, min.yCoord, max.zCoord);
        p[6] = new Vec3d(min.xCoord, max.yCoord, max.zCoord);
        p[7] = max;

        lasers[0] = new LaserData(p[0], p[1]);
        lasers[1] = new LaserData(p[0], p[2]);
        lasers[2] = new LaserData(p[2], p[3]);
        lasers[3] = new LaserData(p[1], p[3]);
        lasers[4] = new LaserData(p[4], p[5]);
        lasers[5] = new LaserData(p[4], p[6]);
        lasers[6] = new LaserData(p[5], p[7]);
        lasers[7] = new LaserData(p[6], p[7]);
        lasers[8] = new LaserData(p[0], p[4]);
        lasers[9] = new LaserData(p[1], p[5]);
        lasers[10] = new LaserData(p[2], p[6]);
        lasers[11] = new LaserData(p[3], p[7]);

        return lasers;
    }

    public static void preDestroyBlock(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IInventory && !world.isRemote) {
            if (!(tile instanceof IDropControlInventory) || ((IDropControlInventory) tile).doDrop()) {
                InvUtils.dropItems(world, (IInventory) tile, pos);
                InvUtils.wipeInventory((IInventory) tile);
            }
        }

        if (tile instanceof TileBuildCraft) {
            ((TileBuildCraft) tile).destroy();
        }
    }

    public static boolean isFakePlayer(EntityPlayer player) {
        if (player instanceof FakePlayer) {
            return true;
        }

        // Tip donated by skyboy - addedToChunk must be set to false by a fake player
        // or it becomes a chunk-loading entity.
        if (!player.addedToChunk) {
            return true;
        }

        return false;
    }

    public static boolean checkPipesConnections(TileEntity tile1, TileEntity tile2) {
        if (tile1 == null || tile2 == null) {
            return false;
        }

        if (!(tile1 instanceof IPipeTile) && !(tile2 instanceof IPipeTile)) {
            return false;
        }

        EnumFacing o = null;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (tile1.getPos().offset(facing).equals(tile2.getPos())) {
                o = facing;
                break;
            }
        }

        if (o == null) {
            return false;
        }

        if (tile1 instanceof IPipeTile && !((IPipeTile) tile1).isPipeConnected(o)) {
            return false;
        }

        if (tile2 instanceof IPipeTile && !((IPipeTile) tile2).isPipeConnected(o.getOpposite())) {
            return false;
        }

        return true;
    }

    public static boolean isPipeConnected(IBlockAccess access, BlockPos pos, EnumFacing dir, IPipeTile.PipeType type) {
        TileEntity tile = access.getTileEntity(pos.offset(dir));
        return tile instanceof IPipeTile && ((IPipeTile) tile).getPipeType() == type && ((IPipeTile) tile).isPipeConnected(dir.getOpposite());
    }

    public static int[] createSlotArray(int first, int count) {
        int[] slots = new int[count];
        for (int k = first; k < first + count; k++) {
            slots[k - first] = k;
        }
        return slots;
    }

    public static String getNameForItem(Item item) {
        Object obj = Item.REGISTRY.getNameForObject(item);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static String getNameForBlock(Block block) {
        Object obj = Block.REGISTRY.getNameForObject(block);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static String getModSpecificNameForBlock(Block block) {
        Object obj = Block.REGISTRY.getNameForObject(block);
        if (obj == null) {
            return null;
        }
        return ((ResourceLocation) obj).getResourcePath();
    }

    public static String getModSpecificNameForItem(Item item) {
        Object obj = Item.REGISTRY.getNameForObject(item);
        if (obj == null) {
            return null;
        }
        return ((ResourceLocation) obj).getResourcePath();
    }

    /** Checks between a min and max all the chunks inbetween actually exist. Args: world, minX, minY, minZ, maxX, maxY,
     * maxZ */
    public static boolean checkChunksExist(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return world.isAreaLoaded(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public static boolean checkChunksExist(World world, BlockPos min, BlockPos max) {
        return checkChunksExist(world, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    // Vector utils

    /** Factory that returns a new Vec3d with the same argument for x, y and z. */
    public static Vec3d vec3(double value) {
        return new Vec3d(value, value, value);
    }

    /** Factory that returns a new BlockPos with the same argument for x, y and z. */
    public static BlockPos vec3i(int value) {
        return new BlockPos(value, value, value);
    }

    /** Factory that returns a new Vector3f with the same argument for x, y and z. */
    public static Vector3f vec3f(float value) {
        return new Vector3f(value, value, value);
    }

    /** Factory that converts an integer vector to a double vector. */
    public static Vec3d convert(Vec3i vec3i) {
        return new Vec3d(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    /** Convert an integer vector to an equal floating point vector, 0.5 added to all coordinates (so the middle of a
     * block if this vector represents a block) */
    public static Vec3d convertMiddle(Vec3i vec3i) {
        return convert(vec3i).add(Utils.VEC_HALF);
    }

    public static Vec3d convert(EnumFacing face) {
        if (face == null) {
            return Utils.VEC_ZERO;
        }
        return new Vec3d(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());

    }

    public static Vec3d convert(EnumFacing face, double size) {
        return multiply(convert(face), size);
    }

    public static Vec3d convertExcept(EnumFacing face, double size) {
        int direction = face.getAxisDirection().getOffset();
        return vec3(direction * size).subtract(convert(face, size));
    }

    public static EnumFacing convertPositive(EnumFacing face) {
        if (face == null) {
            return null;
        }
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            return face.getOpposite();
        }
        return face;
    }

    public static Axis other(Axis a, Axis b) {
        return axisOtherMap.get(a).get(b);
    }

    // We always return BlockPos instead of Vec3di as it will be usable in all situations that Vec3di is, and all the
    // ones
    // that require BlockPos
    public static BlockPos convertFloor(Vec3d vec) {
        return new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static BlockPos convertCeiling(Vec3d vec) {
        return new BlockPos(Math.ceil(vec.xCoord), Math.ceil(vec.yCoord), Math.ceil(vec.zCoord));
    }

    public static BlockPos convertFloor(EnumFacing face) {
        return convertFloor(convert(face));
    }

    public static BlockPos convertFloor(EnumFacing face, int multiple) {
        return convertFloor(convert(face, multiple));
    }

    public static BlockPos min(BlockPos one, BlockPos two) {
        if (one == null) return two;
        if (two == null) return one;
        int x = Math.min(one.getX(), two.getX());
        int y = Math.min(one.getY(), two.getY());
        int z = Math.min(one.getZ(), two.getZ());
        return new BlockPos(x, y, z);
    }

    public static BlockPos max(BlockPos one, BlockPos two) {
        if (one == null) return two;
        if (two == null) return one;
        int x = Math.max(one.getX(), two.getX());
        int y = Math.max(one.getY(), two.getY());
        int z = Math.max(one.getZ(), two.getZ());
        return new BlockPos(x, y, z);
    }

    public static Vec3d convert(Vector3f vec) {
        return new Vec3d(vec.x, vec.y, vec.z);
    }

    public static Vector3f convertFloat(Vec3d vec) {
        return new Vector3f((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord);
    }

    public static Vec3d multiply(Vec3d vec, double multiple) {
        return new Vec3d(vec.xCoord * multiple, vec.yCoord * multiple, vec.zCoord * multiple);
    }

    public static Vec3d divide(Vec3d vec, double divisor) {
        return multiply(vec, 1 / divisor);
    }

    public static Vec3d clamp(Vec3d in, Vec3d lower, Vec3d upper) {
        double x = MathUtils.clamp(in.xCoord, lower.xCoord, upper.xCoord);
        double y = MathUtils.clamp(in.yCoord, lower.yCoord, upper.yCoord);
        double z = MathUtils.clamp(in.zCoord, lower.zCoord, upper.zCoord);
        return new Vec3d(x, y, z);
    }

    public static Vec3d min(Vec3d one, Vec3d two) {
        double x = Math.min(one.xCoord, two.xCoord);
        double y = Math.min(one.yCoord, two.yCoord);
        double z = Math.min(one.zCoord, two.zCoord);
        return new Vec3d(x, y, z);
    }

    public static Vec3d max(Vec3d one, Vec3d two) {
        double x = Math.max(one.xCoord, two.xCoord);
        double y = Math.max(one.yCoord, two.yCoord);
        double z = Math.max(one.zCoord, two.zCoord);
        return new Vec3d(x, y, z);
    }

    public static Matrix3d toMatrix(Vec3d vec) {
        Matrix3d matrix = new Matrix3d();
        matrix.m00 = vec.xCoord;
        matrix.m11 = vec.yCoord;
        matrix.m22 = vec.zCoord;
        return matrix;
    }

    public static Vec3d getVec(Entity entity) {
        return new Vec3d(entity.posX, entity.posY, entity.posZ);
    }

    public static BlockPos getPos(Entity entity) {
        return convertFloor(getVec(entity));
    }

    public static Vec3d min(AxisAlignedBB bb) {
        return new Vec3d(bb.minX, bb.minY, bb.minZ);
    }

    public static Vec3d max(AxisAlignedBB bb) {
        return new Vec3d(bb.maxX, bb.maxY, bb.maxZ);
    }

    @SideOnly(Side.CLIENT)
    public static Vec3d getInterpolatedVec(Entity entity, float partialTicks) {
        return entity.getPositionEyes(partialTicks).addVector(0, -entity.getEyeHeight(), 0);
    }

    /** Returns all of the chunks that all the block positions returned by
     * {@link #allInBoxIncludingCorners(BlockPos, BlockPos)} occupy */
    public static Iterable<ChunkPos> allChunksFor(BlockPos pos1, BlockPos pos2) {
        BlockPos min = min(pos1, pos2);
        BlockPos max = max(pos1, pos2);
        int minX = min.getX() >> 4;
        int maxX = max.getX() >> 4;
        int minZ = min.getZ() >> 4;
        int maxZ = max.getZ() >> 4;
        List<ChunkPos> list = Lists.newArrayList();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                list.add(new ChunkPos(x, z));
            }
        }
        return list;
    }

    public static Iterable<BlockPos> allInChunk(ChunkPos ccip) {
        return BlockPos.getAllInBox(ccip.getBlock(0, 0, 0), ccip.getBlock(15, 255, 15));
    }

    public static Vec3d getMinForFace(EnumFacing face, Vec3d min, Vec3d max) {
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            return min;
        }
        if (face == EnumFacing.EAST) {
            return new Vec3d(max.xCoord, min.yCoord, min.zCoord);
        } else if (face == EnumFacing.UP) {
            return new Vec3d(min.xCoord, max.yCoord, min.zCoord);
        } else {// MUST be SOUTH
            return new Vec3d(min.xCoord, min.yCoord, max.zCoord);
        }
    }

    public static Vec3d getMaxForFace(EnumFacing face, Vec3d min, Vec3d max) {
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            return max;
        }
        if (face == EnumFacing.WEST) {
            return new Vec3d(min.xCoord, max.yCoord, max.zCoord);
        } else if (face == EnumFacing.DOWN) {
            return new Vec3d(max.xCoord, min.yCoord, max.zCoord);
        } else {// MUST be NORTH
            return new Vec3d(max.xCoord, max.yCoord, min.zCoord);
        }
    }

    public static BlockPos getMinForFace(EnumFacing face, BlockPos min, BlockPos max) {
        return convertFloor(getMinForFace(face, convert(min), convert(max)));
    }

    public static BlockPos getMaxForFace(EnumFacing face, BlockPos min, BlockPos max) {
        return convertFloor(getMaxForFace(face, convert(min), convert(max)));
    }

    public static boolean isInside(BlockPos toTest, BlockPos min, BlockPos max) {
        if (toTest.getX() < min.getX() || toTest.getY() < min.getY() || toTest.getZ() < min.getZ()) {
            return false;
        }
        return toTest.getX() <= max.getX() && toTest.getY() <= max.getY() && toTest.getZ() <= max.getZ();
    }

    public static BlockPos getClosestInside(BlockPos from, BlockPos min, BlockPos max) {
        BlockPos maxMin = max(from, min);
        BlockPos minMax = min(maxMin, max);
        return minMax;
    }

    public static BlockPos getClosestInside(Box box, BlockPos from) {
        return min(max(from, box.min()), box.max());
    }

    public static AxisAlignedBB boundingBox(Vec3d pointA, Vec3d pointB) {
        Vec3d min = min(pointA, pointB);
        Vec3d max = max(pointA, pointB);
        return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
    }

    public static BlockPos[] getNeighboursIncludingSelf(BlockPos pos, EnumFacing face) {
        BlockPos[] positions = new BlockPos[5];
        positions[0] = pos;
        int ordinal = 0;
        for (EnumFacing f : getNeighbours(face)) {
            positions[++ordinal] = pos.offset(f);
        }
        return positions;
    }

    public static EnumFacing[] getNeighbours(EnumFacing face) {
        EnumFacing[] faces = new EnumFacing[4];
        int ordinal = 0;
        for (EnumFacing next : EnumFacing.values()) {
            if (next.getAxis() != face.getAxis()) {
                faces[ordinal] = next;
                ordinal++;
            }
        }
        return faces;
    }

    /** Like {@link Random#nextInt(int)} the size is taken as exclusive */
    public static BlockPos randomBlockPos(Random rand, BlockPos size) {
        return new BlockPos(rand.nextInt(size.getX()), rand.nextInt(size.getY()), rand.nextInt(size.getZ()));
    }

    public static BlockPos invert(BlockPos pos) {
        return new BlockPos(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    /** Finds the closest block position in a set to the given position. Will return a random block position if server
     * are found within a similar distance */
    public static BlockPos findClosestTo(Set<BlockPos> set, BlockPos hint) {
        return findClosestTo(set, hint, ACTUAL_RANDOM);
    }

    /** Finds the closest block position in a set to the given position. Will return a random block position if server
     * are found within a similar distance */
    public static BlockPos findClosestTo(Set<BlockPos> set, BlockPos hint, Random rand) {
        if (set.isEmpty()) return null;
        if (hint == null) return set.iterator().next();
        int lowestY = Integer.MAX_VALUE;
        double closestDist = Double.MAX_VALUE;
        List<BlockPos> closest = Lists.newArrayList();
        for (BlockPos pos : set) {
            // The lower the Y value, the better
            if (pos.getY() < lowestY) {
                closest.clear();
                closest.add(pos);
                closestDist = pos.distanceSq(hint);
                lowestY = pos.getY();
            } else {
                double dist = pos.distanceSq(hint);
                if (dist - 1 > closestDist) continue;
                if (dist + 1 < closestDist) {
                    closest.clear();
                    closest.add(pos);
                    closestDist = dist;
                } else {
                    closest.add(pos);
                }
            }
        }
        if (closest.isEmpty()) return null;
        return closest.get(rand.nextInt(closest.size()));
    }

    /** Like {@link BlockPos#getAllInBox(BlockPos, BlockPos)} but can iterate in orders other than XYZ */
    public static BoxIterable getAllInBox(BlockPos a, BlockPos b, final AxisOrder order) {
        final BlockPos min = min(a, b);
        final BlockPos max = max(a, b);
        return new BoxIterable(min, max, order);
    }
}
