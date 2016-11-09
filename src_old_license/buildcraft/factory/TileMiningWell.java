/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.BlockMiner;
import buildcraft.lib.misc.BlockUtil;

public class TileMiningWell extends TileBuildCraft implements IHasWork, IPipeConnection, IControllable {
    private boolean isDigging = true;
    private BlockMiner miner;
    private int ledState;
    private int ticksSinceAction = 9001;

    private SafeTimeTracker updateTracker = new SafeTimeTracker(BuildCraftCore.updateFactor);

    public TileMiningWell() {
        super();
        this.setBattery(new RFBattery(2 * 64 * BuilderAPI.BREAK_ENERGY, BuilderAPI.BREAK_ENERGY * 4 + BuilderAPI.BUILD_ENERGY, 0));
    }

    /** Dig the next available piece of land if not done. As soon as it reaches bedrock, lava or goes below 0, it's
     * considered done. */
    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

        if (updateTracker.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
        }

        ticksSinceAction++;

        if (mode == Mode.Off) {
            if (miner != null) {
                miner.invalidate();
                miner = null;
            }
            isDigging = false;
            return;
        }

        if (getBattery().getEnergyStored() == 0) {
            return;
        }

        if (miner == null) {
            World world = worldObj;

            BlockPos search = pos.down();

            while (world.getBlockState(search).getBlock() == BuildCraftFactory.plainPipeBlock) {
                search = search.down();
            }

            if (search.getY() < 1 || search.getY() < pos.getY() - BuildCraftFactory.miningDepth || !BlockUtil.canChangeBlock(world, search)) {
                isDigging = false;
                // Drain energy, because at 0 energy this will stop doing calculations.
                getBattery().useEnergy(0, 10, false);
                return;
            }

            if (world.isAirBlock(search) || world.getBlockState(search).getBlock().isReplaceable(world, search)) {
                ticksSinceAction = 0;
                world.setBlockState(search, BuildCraftFactory.plainPipeBlock.getDefaultState());
            } else {
                miner = new BlockMiner(world, this, search);
            }
        }

        if (miner != null) {
            isDigging = true;
            ticksSinceAction = 0;

            int usedEnergy = miner.acceptEnergy(getBattery().getEnergyStored());
            getBattery().useEnergy(usedEnergy, usedEnergy, false);

            if (miner.hasFailed()) {
                isDigging = false;
            }

            if (miner.hasFailed() || miner.hasMined()) {
                miner = null;
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (miner != null) {
            miner.invalidate();
        }
        if (worldObj != null && pos.getY() > 2) {
            BuildCraftFactory.miningWellBlock.removePipes(worldObj, pos);
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        super.writeData(stream);

        ledState = (ticksSinceAction < 2 ? 16 : 0) | (getBattery().getEnergyStored() * 15 / getBattery().getMaxEnergyStored());
        stream.writeByte(ledState);
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);

        int newLedState = stream.readUnsignedByte();
        if (newLedState != ledState) {
            ledState = newLedState;
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public boolean hasWork() {
        return isDigging;
    }

    @Override
    public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, EnumFacing with) {
        return type == IPipeTile.PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
    }

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == Mode.Off || mode == Mode.On;
    }
}
