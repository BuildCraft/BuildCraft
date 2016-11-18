/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.CoreConstants;
import buildcraft.core.DefaultAreaProvider;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.internal.IDropControlInventory;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.utils.*;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.misc.data.Box.Kind;

import io.netty.buffer.ByteBuf;

public class TileQuarry extends TileAbstractBuilder implements IHasWork, ISidedInventory, IDropControlInventory, IPipeConnection, IControllable, IDebuggable {

    private static enum Stage {
        BUILDING,
        DIGGING,
        MOVING,
        IDLE,
        DONE
    }

    public EntityMechanicalArm arm;
    public EntityPlayer placedBy;

    /** The box for the setup */
    protected Box box = new Box();
    /** The box of what will be mined. Goes from y=0 to the very top layer of the mining box */
    private Box miningBox = new Box();
    private BlockPos target = BlockPos.ORIGIN;
    private Vec3d headPos = Utils.VEC_ZERO;
    private double speed = 0.03;
    private Stage stage = Stage.BUILDING;
    private boolean movingHorizontally;
    private boolean movingVertically;
    private float headTrajectory;

    private SafeTimeTracker updateTracker = new SafeTimeTracker(BuildCraftCore.updateFactor);

    private BptBuilderBase builder;

    private final LinkedList<int[]> visitList = Lists.newLinkedList();

    private boolean loadDefaultBoundaries = false;
    private Ticket chunkTicket;

    private boolean frameProducer = true;

    private NBTTagCompound initNBT = null;

    private BlockMiner miner;
    private int ledState;

    // TMP
    private int buildCallsS = 0, buildCallsF = 0;

    public TileQuarry() {
        box.kind = Kind.STRIPES;
        this.setBattery(new RFBattery((int) (2 * 64 * BuilderAPI.BREAK_ENERGY * BuildCraftCore.miningMultiplier), (int) (1000 * BuildCraftCore.miningMultiplier), 0));
    }

    public void createUtilsIfNeeded() {
        if (!worldObj.isRemote) {
            if (builder == null) {
                if (!box.isInitialized()) {
                    setBoundaries(loadDefaultBoundaries);
                }

                initializeBlueprintBuilder();
            }
        }

        if (getStage() != Stage.BUILDING) {
            box.isVisible = false;

            if (arm == null) {
                createArm();
            }

            if (miningBox == null || !miningBox.isInitialized()) {
                miningBox = new Box(box.min(), box.max());
                miningBox.contract(1);
                miningBox.setMin(VecUtil.replaceValue(miningBox.min(), Axis.Y, 0));
                miningBox.setMax(miningBox.max().add(0, 1, 0));
            }

            if (findTarget(false)) {
                AxisAlignedBB union = miningBox.getBoundingBox().union(box.getBoundingBox());
                if (!union.isVecInside(headPos)) {
                    headPos = VecUtil.replaceValue(headPos, Axis.Y, (double) (miningBox.max().getY() - 2));
                    BlockPos nearestPos = miningBox.closestInsideTo(Utils.convertFloor(headPos));
                    headPos = Utils.convert(nearestPos);
                }
            }
        } else {
            box.isVisible = true;
        }
    }

    private void createArm() {
        Vec3d vec = Utils.convert(box.min()).add(Utils.vec3(CoreConstants.PIPE_MAX_POS));
        vec = vec.add(Utils.convert(EnumFacing.UP, box.size().getY() - 1.5));

        double width = box.size().getX() - 2 + CoreConstants.PIPE_MIN_POS * 2;
        double height = box.size().getZ() - 2 + CoreConstants.PIPE_MIN_POS * 2;

        worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj, vec, width, height, this));
    }

    // Callback from the arm once it's created
    public void setArm(EntityMechanicalArm arm) {
        this.arm = arm;
    }

    public boolean areChunksLoaded() {
        if (BuildCraftBuilders.quarryLoadsChunks) {
            // Small optimization
            return true;
        }

        return Utils.checkChunksExist(worldObj, box.min(), box.max());
    }

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            if (getStage() != Stage.DONE) {
                moveHead(speed);
            }

            return;
        }

        if (getStage() == Stage.DONE) {
            if (mode == Mode.Loop) {
                setStage(Stage.IDLE);
            } else {
                return;
            }
        }

        if (!areChunksLoaded()) {
            return;
        }

        if (mode == Mode.Off && getStage() != Stage.MOVING) {
            return;
        }

        createUtilsIfNeeded();
        if (getStage() == Stage.BUILDING) {
            if (builder != null && !builder.isDone(this)) {
                if (builder.buildNextSlot(worldObj, this)) buildCallsS++;
                else buildCallsF++;
            } else {
                setStage(Stage.IDLE);
            }
        } else if (getStage() == Stage.DIGGING) {
            dig();
        } else if (getStage() == Stage.IDLE) {
            idling();

            // We are sending a network packet update ONLY below.
            // In this case, since idling() does it anyway, we should return.
            return;
        } else if (stage == Stage.MOVING) {
            int energyUsed = this.getBattery().useEnergy(20, (int) Math.ceil(20D + (double) getBattery().getEnergyStored() / 10), false);

            if (energyUsed >= 20) {

                speed = 0.1 + energyUsed / 2000F;

                // If it's raining or snowing above the head, slow down.
                if (worldObj.isRaining()) {
                    if (worldObj.getHeight(Utils.convertFloor(headPos)).getY() < headPos.yCoord) {
                        speed *= 0.7;
                    }
                }

                moveHead(speed);
            } else {
                speed = 0;
            }
        }

        if (updateTracker.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
        }
    }

    protected void dig() {
        if (worldObj.isRemote) {
            return;
        }

        if (miner == null) {
            // Hmm. Probably shouldn't be mining if there's no miner.
            stage = Stage.IDLE;
            return;
        }

        int rfTaken = miner.acceptEnergy(getBattery().getEnergyStored());
        getBattery().useEnergy(rfTaken, rfTaken, false);

        if (miner.hasMined()) {
            // Collect any lost items laying around.
            double[] head = getHead();
            AxisAlignedBB axis = new AxisAlignedBB(head[0] - 2, head[1] - 2, head[2] - 2, head[0] + 3, head[1] + 3, head[2] + 3);
            List<EntityItem> result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
            for (EntityItem entity : result) {
                if (entity.isDead) {
                    continue;
                }

                ItemStack mineable = entity.getEntityItem();
                if (mineable.stackSize <= 0) {
                    continue;
                }
                CoreProxy.proxy.removeEntity(entity);
                miner.mineStack(mineable);
            }
            setStage(Stage.IDLE);
            miner = null;
            return;
        }

        if (!findFrame()) {
            initializeBlueprintBuilder();
            stage = Stage.BUILDING;
        } else if (miner.hasFailed()) {
            setStage(Stage.IDLE);
            miner = null;
        }
    }

    protected boolean findFrame() {
        return worldObj.getBlockState(box.min()).getBlock() == BuildCraftBuilders.frameBlock;
    }

    protected void idling() {
        if (!findTarget(true)) {
            // I believe the issue is box going null becuase of bad chunkloader positioning
            if (arm != null && box != null) {
                setTarget(new BlockPos(box.min().getX() + 1, pos.getY() + 2, box.min().getZ() + 1));
            }

            setStage(Stage.DONE);
        } else {
            setStage(Stage.MOVING);
        }

        movingHorizontally = true;
        movingVertically = true;
        double[] head = getHead();
        int[] target = getTarget();
        headTrajectory = (float) Math.atan2(target[2] - head[2], target[0] - head[0]);
        sendNetworkUpdate();
    }

    public boolean findTarget(boolean doSet) {
        if (worldObj.isRemote) {
            return false;
        }

        boolean columnVisitListIsUpdated = false;

        if (visitList.isEmpty()) {
            createColumnVisitList();
            columnVisitListIsUpdated = true;
        }

        if (!doSet) {
            return !visitList.isEmpty();
        }

        if (visitList.isEmpty()) {
            return false;
        }

        int[] nextTarget = visitList.removeFirst();

        if (!columnVisitListIsUpdated) { // nextTarget may not be accurate, at least search the target column for
                                         // changes
            for (int y = nextTarget[1] + 1; y < getPos().getY() + 3; y++) {
                if (isQuarriableBlock(new BlockPos(nextTarget[0], y, nextTarget[2]))) {
                    createColumnVisitList();
                    columnVisitListIsUpdated = true;
                    nextTarget = null;
                    break;
                }
            }
        }

        if (columnVisitListIsUpdated && nextTarget == null && !visitList.isEmpty()) {
            nextTarget = visitList.removeFirst();
        } else if (columnVisitListIsUpdated && nextTarget == null) {
            return false;
        }

        setTarget(new BlockPos(nextTarget[0], nextTarget[1] + 1, nextTarget[2]));

        return true;
    }

    /** Make the column visit list: called once per layer */
    private void createColumnVisitList() {
        visitList.clear();
        boolean[][] blockedColumns = new boolean[builder.blueprint.size.getX() - 2][builder.blueprint.size.getZ() - 2];

        for (int searchY = pos.getY() + 3; searchY >= 1; --searchY) {
            int startX, endX, incX;

            if (searchY % 2 == 0) {
                startX = 0;
                endX = builder.blueprint.size.getX() - 2;
                incX = 1;
            } else {
                startX = builder.blueprint.size.getX() - 3;
                endX = -1;
                incX = -1;
            }

            for (int searchX = startX; searchX != endX; searchX += incX) {
                int startZ, endZ, incZ;

                if (searchX % 2 == searchY % 2) {
                    startZ = 0;
                    endZ = builder.blueprint.size.getZ() - 2;
                    incZ = 1;
                } else {
                    startZ = builder.blueprint.size.getZ() - 3;
                    endZ = -1;
                    incZ = -1;
                }

                for (int searchZ = startZ; searchZ != endZ; searchZ += incZ) {
                    if (!blockedColumns[searchX][searchZ]) {
                        int bx = box.min().getX() + searchX + 1;
                        int by = searchY;
                        int bz = box.min().getZ() + searchZ + 1;

                        BlockPos pos = new BlockPos(bx, by, bz);
                        IBlockState state = worldObj.getBlockState(pos);
                        Block block = state.getBlock();

                        if (!BlockUtil.canChangeBlock(state, worldObj, pos)) {
                            blockedColumns[searchX][searchZ] = true;
                        } else if (!BuildCraftAPI.isSoftBlock(worldObj, pos) && !(block instanceof BlockLiquid) && !(block instanceof IFluidBlock)) {
                            visitList.add(new int[] { bx, by, bz });
                        }

                        // Stop at two planes - generally any obstructions will have been found and will force a
                        // recompute prior to this
                        if (visitList.size() > builder.blueprint.size.getZ() * builder.blueprint.size.getX() * 2) {
                            return;
                        }
                    }
                }
            }
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        if (nbttagcompound.hasKey("box")) {
            box.initialize(nbttagcompound.getCompoundTag("box"));

            loadDefaultBoundaries = false;
        } else if (nbttagcompound.hasKey("xSize")) {
            // This is a legacy save, get old data

            int xMin = nbttagcompound.getInteger("xMin");
            int zMin = nbttagcompound.getInteger("zMin");

            int xSize = nbttagcompound.getInteger("xSize");
            int ySize = nbttagcompound.getInteger("ySize");
            int zSize = nbttagcompound.getInteger("zSize");

            box.reset();
            box.setMin(new BlockPos(xMin, pos.getY(), zMin));
            box.setMax(new BlockPos(xMin + xSize - 1, pos.getY() + ySize - 1, zMin + zSize - 1));

            loadDefaultBoundaries = false;
        } else {
            // This is a legacy save, compute boundaries

            loadDefaultBoundaries = true;
        }

        int targetX = nbttagcompound.getInteger("targetX");
        int targetY = nbttagcompound.getInteger("targetY");
        int targetZ = nbttagcompound.getInteger("targetZ");
        target = new BlockPos(targetX, targetY, targetZ);

        double headPosX = nbttagcompound.getDouble("headPosX");
        double headPosY = nbttagcompound.getDouble("headPosY");
        double headPosZ = nbttagcompound.getDouble("headPosZ");
        headPos = new Vec3d(headPosX, headPosY, headPosZ);

        // The rest of load has to be done upon initialize.
        initNBT = (NBTTagCompound) nbttagcompound.getCompoundTag("bpt").copy();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("targetX", target.getX());
        nbt.setInteger("targetY", target.getY());
        nbt.setInteger("targetZ", target.getZ());
        nbt.setDouble("headPosX", headPos.xCoord);
        nbt.setDouble("headPosY", headPos.yCoord);
        nbt.setDouble("headPosZ", headPos.zCoord);

        NBTTagCompound boxTag = new NBTTagCompound();
        box.writeToNBT(boxTag);
        nbt.setTag("box", boxTag);

        NBTTagCompound bptNBT = new NBTTagCompound();

        if (builder != null) {
            NBTTagCompound builderCpt = new NBTTagCompound();
            builder.saveBuildStateToNBT(builderCpt, this);
            bptNBT.setTag("builderState", builderCpt);
        }

        nbt.setTag("bpt", bptNBT);
        return nbt;
    }

    public void positionReached() {
        if (worldObj.isRemote) {
            return;
        }

        BlockPos pos = target.down();
        if (isQuarriableBlock(pos)) {
            miner = new BlockMiner(worldObj, this, pos);
            setStage(Stage.DIGGING);
        } else {
            setStage(Stage.IDLE);
        }
    }

    private boolean isQuarriableBlock(BlockPos pos) {
        IBlockState state = worldObj.getBlockState(pos);
        Block block = state.getBlock();
        return BlockUtil.canChangeBlock(state, worldObj, pos) && !BuildCraftAPI.isSoftBlock(worldObj, pos) && !(block instanceof BlockLiquid) && !(block instanceof IFluidBlock);
    }

    @Override
    public void invalidate() {
        if (chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
        }

        super.invalidate();
        destroy();
    }

    @Override
    public void onChunkUnload() {
        destroy();
    }

    @Override
    public void destroy() {
        if (arm != null) {
            arm.setDead();
        }

        arm = null;

        frameProducer = false;

        if (miner != null) {
            miner.invalidate();
        }
    }

    @Override
    public boolean hasWork() {
        return getStage() != Stage.DONE;
    }

    private Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        this.stage = stage;
        IBlockState state = worldObj.getBlockState(pos);
        if (stage == Stage.DONE) {
            worldObj.setBlockState(pos, state.withProperty(BuildCraftProperties.LED_DONE, true));
        } else if (state.getValue(BuildCraftProperties.LED_DONE) == true) {
            worldObj.setBlockState(pos, state.withProperty(BuildCraftProperties.LED_DONE, false));
        }
    }

    private void setBoundaries(boolean useDefaultI) {
        boolean useDefault = useDefaultI;

        if (BuildCraftBuilders.quarryLoadsChunks && chunkTicket == null) {
            chunkTicket = ForgeChunkManager.requestTicket(BuildCraftBuilders.instance, worldObj, Type.NORMAL);
        }

        if (chunkTicket != null) {
            chunkTicket.getModData().setInteger("quarryX", pos.getX());
            chunkTicket.getModData().setInteger("quarryY", pos.getY());
            chunkTicket.getModData().setInteger("quarryZ", pos.getZ());
            ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
        }

        IAreaProvider a = null;

        if (!useDefault) {
            a = Utils.getNearbyAreaProvider(worldObj, pos);
        }

        if (a == null) {
            a = new DefaultAreaProvider(pos, pos.add(new BlockPos(10, 4, 10)));

            useDefault = true;
        }

        int xSize = a.max().getX() - a.min().getX() + 1;
        int zSize = a.max().getZ() - a.min().getZ() + 1;

        if (xSize < 3 || zSize < 3 || (chunkTicket != null && ((xSize * zSize) >> 8) >= chunkTicket.getMaxChunkListDepth())) {
            if (placedBy != null) {
                placedBy.addChatMessage(new TextComponentTranslation("chat.buildcraft.quarry.tooSmall", xSize, zSize, chunkTicket != null ? chunkTicket.getMaxChunkListDepth() : 0));
            }

            a = new DefaultAreaProvider(pos, pos.add(new BlockPos(10, 4, 10)));
            useDefault = true;
        }

        xSize = a.max().getX() - a.min().getX() + 1;
        int ySize = a.max().getY() - a.min().getY() + 1;
        zSize = a.max().getZ() - a.min().getZ() + 1;

        box.initialize(a);

        if (ySize < 5) {
            ySize = 5;
            box.setMax(VecUtil.replaceValue(box.max(), Axis.Y, box.min().getY() + ySize - 1));
        }

        if (useDefault) {
            int xMin, zMin;

            EnumFacing face = worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();

            switch (face) {
                case EAST:
                    xMin = pos.getX() + 1;
                    zMin = pos.getZ() - 4 - 1;
                    break;
                case WEST:
                    xMin = pos.getX() - 9 - 2;
                    zMin = pos.getZ() - 4 - 1;
                    break;
                case SOUTH:
                    xMin = pos.getX() - 4 - 1;
                    zMin = pos.getZ() + 1;

                    break;
                case NORTH:
                default:
                    xMin = pos.getX() - 4 - 1;
                    zMin = pos.getZ() - 9 - 2;
                    break;
            }

            box.reset();
            box.setMin(new BlockPos(xMin, pos.getY(), zMin));
            box.setMax(new BlockPos(xMin + xSize - 1, pos.getY() + ySize - 1, zMin + zSize - 1));
        }

        a.removeFromWorld();
        if (chunkTicket != null) {
            forceChunkLoading(chunkTicket);
        }

        sendNetworkUpdate();
    }

    private void initializeBlueprintBuilder() {
        PatternQuarryFrame pqf = PatternQuarryFrame.INSTANCE;

        Blueprint bpt = pqf.getBlueprint(box, worldObj);
        builder = new BptBuilderBlueprint(bpt, worldObj, box.min());
        builder.setOrder(new AxisOrder(EnumAxisOrder.XZY, true, true, false));
        speed = 0;
        stage = Stage.BUILDING;
        sendNetworkUpdate();
    }

    @Override
    public void writeData(ByteBuf stream) {
        super.writeData(stream);
        box.writeData(stream);
        stream.writeInt(target.getX());
        stream.writeShort(target.getY());
        stream.writeInt(target.getZ());
        stream.writeDouble(headPos.xCoord);
        stream.writeDouble(headPos.yCoord);
        stream.writeDouble(headPos.zCoord);
        stream.writeFloat((float) speed);
        stream.writeFloat(headTrajectory);
        int flags = stage.ordinal();
        flags |= movingHorizontally ? 0x10 : 0;
        flags |= movingVertically ? 0x20 : 0;
        stream.writeByte(flags);
        ledState = (getBattery().getEnergyStored() * 3 / getBattery().getMaxEnergyStored());
        stream.writeByte(ledState);
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);
        box.readData(stream);
        int targetX = stream.readInt();
        int targetY = stream.readUnsignedShort();
        int targetZ = stream.readInt();
        target = new BlockPos(targetX, targetY, targetZ);

        double headPosX = stream.readDouble();
        double headPosY = stream.readDouble();
        double headPosZ = stream.readDouble();
        headPos = new Vec3d(headPosX, headPosY, headPosZ);

        speed = stream.readFloat();
        headTrajectory = stream.readFloat();
        int flags = stream.readUnsignedByte();
        setStage(Stage.values()[flags & 0x07]);
        movingHorizontally = (flags & 0x10) != 0;
        movingVertically = (flags & 0x20) != 0;
        ledState = stream.readUnsignedByte();

        createUtilsIfNeeded();

        if (arm != null) {
            arm.setHead(headPos);
            arm.updatePosition();
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        if (!this.getWorld().isRemote && !box.isInitialized()) {
            setBoundaries(false);
        }

        createUtilsIfNeeded();

        if (initNBT != null && builder != null) {
            builder.loadBuildStateToNBT(initNBT.getCompoundTag("builderState"), this);
        }

        initNBT = null;

        sendNetworkUpdate();
    }

    public void reinitalize() {
        initializeBlueprintBuilder();
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if (frameProducer) {
            return new ItemStack(BuildCraftBuilders.frameBlock);
        } else {
            return null;
        }
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (frameProducer) {
            return new ItemStack(BuildCraftBuilders.frameBlock, j);
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {}

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return null;
    }

    @Override
    public String getInventoryName() {
        return "";
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isBuildingMaterialSlot(int i) {
        return true;
    }

    public void moveHead(double instantSpeed) {
        int[] target = getTarget();
        double[] head = getHead();

        if (movingHorizontally) {
            if (Math.abs(target[0] - head[0]) < instantSpeed * 2 && Math.abs(target[2] - head[2]) < instantSpeed * 2) {
                head[0] = target[0];
                head[2] = target[2];

                movingHorizontally = false;

                if (!movingVertically) {
                    positionReached();
                    head[1] = target[1];
                }
            } else {
                head[0] += MathHelper.cos(headTrajectory) * instantSpeed;
                head[2] += MathHelper.sin(headTrajectory) * instantSpeed;
            }
            setHead(head[0], head[1], head[2]);
        }

        if (movingVertically) {
            if (Math.abs(target[1] - head[1]) < instantSpeed * 2) {
                head[1] = target[1];

                movingVertically = false;
                if (!movingHorizontally) {
                    positionReached();
                    head[0] = target[0];
                    head[2] = target[2];
                }
            } else {
                if (target[1] > head[1]) {
                    head[1] += instantSpeed;
                } else {
                    head[1] -= instantSpeed;
                }
            }
            setHead(head[0], head[1], head[2]);
        }

        updatePosition();
    }

    private void updatePosition() {
        if (arm != null && worldObj.isRemote) {
            arm.setHead(headPos);
            arm.updatePosition();
        }
    }

    private void setHead(double x, double y, double z) {
        headPos = new Vec3d(x, y, z);
    }

    private double[] getHead() {
        return new double[] { headPos.xCoord, headPos.yCoord, headPos.zCoord };
    }

    private int[] getTarget() {
        return new int[] { target.getX(), target.getY(), target.getZ() };
    }

    private void setTarget(BlockPos pos) {
        target = pos;
    }

    public void forceChunkLoading(Ticket ticket) {
        if (chunkTicket == null) {
            chunkTicket = ticket;
        }

        Set<ChunkPos> chunks = Sets.newHashSet();
        ChunkPos quarryChunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        chunks.add(quarryChunk);
        ForgeChunkManager.forceChunk(ticket, quarryChunk);

        if (box.isInitialized()) {
            for (int chunkX = box.min().getX() >> 4; chunkX <= box.max().getX() >> 4; chunkX++) {
                for (int chunkZ = box.min().getZ() >> 4; chunkZ <= box.max().getZ() >> 4; chunkZ++) {
                    ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
                    ForgeChunkManager.forceChunk(ticket, chunk);
                    chunks.add(chunk);
                }
            }
        }

        if (placedBy != null && !(placedBy instanceof FakePlayer)) {
            placedBy.addChatMessage(new TextComponentTranslation("chat.buildcraft.quarry.chunkloadInfo", getPos().getX(), getPos().getY(), getPos().getZ(), chunks.size()));
        }
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // return Utils.boundingBox(Utils.vec3(-100000d), Utils.vec3(100000d));
        if (getPos() == null) return null;
        return new Box(this).extendToEncompass(box).extendToEncompass(miningBox).getBoundingBox();
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == Mode.Off || mode == Mode.On || mode == Mode.Loop;
    }

    @Override
    public boolean doDrop() {
        return false;
    }

    @Override
    public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, EnumFacing with) {
        if (with == worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING)) {
            return ConnectOverride.DISCONNECT;
        }
        return type == IPipeTile.PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        TileQuarry server = CoreProxy.proxy.getServerTile(this);

        left.add("");
        left.add("  - IsServer = " + (server != this));
        left.add("  - Stage = " + server.getStage());
        left.add("  - Mode = " + server.mode);
        if (server.builder == null) {
            left.add("  - Builder = null");
        } else {
            left.add("  - Builder");
            left.add("    - IsDone = " + (server.builder.isDone(server)));
            left.add("    - Min = " + StringUtilBC.vec3ToDispString(server.builder.min()));
            left.add("    - Max = " + StringUtilBC.vec3ToDispString(server.builder.max()));
            left.add("    - Successes = " + server.buildCallsS);
            left.add("    - Failures = " + server.buildCallsF);
        }
        if (server.box == null || !server.box.isInitialized()) {
            left.add("  - BuildingBox = null");
        } else {
            left.add("  - BuildingBox");
            left.add("    - Min = " + StringUtilBC.vec3ToDispString(server.box.min()));
            left.add("    - Max = " + StringUtilBC.vec3ToDispString(server.box.max()));
        }
        if (server.miningBox == null || !server.miningBox.isInitialized()) {
            left.add("  - MiningBox = null");
        } else {
            left.add("  - MiningBox");
            left.add("    - Min = " + StringUtilBC.vec3ToDispString(server.miningBox.min()));
            left.add("    - Max = " + StringUtilBC.vec3ToDispString(server.miningBox.max()));
        }
        left.add("  - Head = " + StringUtilBC.vec3ToDispString(server.headPos));
        left.add("  - Target = " + StringUtilBC.vec3ToDispString(server.target));
    }
}
