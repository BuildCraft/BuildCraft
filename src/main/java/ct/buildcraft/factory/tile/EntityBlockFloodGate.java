package ct.buildcraft.factory.tile;

import java.util.ArrayDeque;
import java.util.Deque;

import ct.buildcraft.factory.BCFactoryBlocks;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class EntityBlockFloodGate extends BlockEntity{
	
    private static final Vec3i[] SEARCH_NORMAL = new Vec3i[] { //
            new Vec3i(0,1,0), new Vec3i(1,0,0), new Vec3i(0,0,1), //
            new Vec3i(-1,0,0), new Vec3i(0,0,-1) //
        };

    private static final Vec3i[] SEARCH_GASEOUS = new Vec3i[] { //
            new Vec3i(0,-1,0), new Vec3i(1,0,0), new Vec3i(0,0,1), //
            new Vec3i(-1,0,0), new Vec3i(0,0,-1) //
        };
	
	private FluidTank tank = new FluidTank(2000);
	private BlockPos currentPos ;
	private BlockPos blockpos;
	private final Deque<BlockPos> fluidToCheck = new ArrayDeque<>();
	private final Deque<BlockPos> checked = new ArrayDeque<>();
	private int tick = 0;
	
	
	public EntityBlockFloodGate(BlockPos pos, BlockState bs) {
		super(null, pos, bs);
		blockpos = pos;
	}
	LazyOptional<IFluidHandler> fluidHandlerLazyOptional = LazyOptional.of(() -> tank);

	

	// Supplied instance (e.g. () -> inventoryHandler)
	// Ensure laziness as initialization should only happen when needed

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
	  if (cap == ForgeCapabilities.FLUID_HANDLER) {
	    return fluidHandlerLazyOptional.cast();
	  }
	  return super.getCapability(cap, side);
	}
	@Override
	public void invalidateCaps() {
	  super.invalidateCaps();
	  fluidHandlerLazyOptional.invalidate();
	}
	public boolean fill() {
		Vec3i[] directions = !tank.getFluid().getFluid().getFluidType().isLighterThanAir() ? SEARCH_GASEOUS:SEARCH_NORMAL;
		BlockState block;
		FluidState fs;
		BlockPos offsetPos;
		BlockPos pos ;
		Level lev = getLevel();
		this.fluidToCheck.add(blockpos);
		while(currentPos == null) {
			if(fluidToCheck.isEmpty()) {
				checked.clear();
				return false;
			}
			pos = this.fluidToCheck.pop();
			checked.add(pos);
			for(Vec3i d : directions) {
				offsetPos = pos.offset(d);
				block = lev.getBlockState(offsetPos);
				fs = block.getFluidState();
				if(!fs.isEmpty()) {
					if(fs.isSource()) {
						if(!checked.contains(offsetPos)) {
							this.fluidToCheck.add(offsetPos);
						}
					}
					else {
						LogUtils.getLogger().info("place0"+Boolean.toString(fs.isSource()));
						currentPos = offsetPos;
						break;
					}
				}
				else if(block.isAir()) {
					LogUtils.getLogger().info("place1");
					currentPos = offsetPos;
					break;
				}
				else	continue;
			}
		}
		this.fluidToCheck.clear();
		checked.clear();
		fs = lev.getFluidState(currentPos);
		if(!fs.isEmpty()&&!fs.isSource()) {
			LogUtils.getLogger().info("p");
			lev.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 0);
		}
//		LogUtils.getLogger().info(".....");
		if(FluidUtil.tryPlaceFluid(null, level, null,currentPos, tank, tank.getFluid())) {
			LogUtils.getLogger().info("place fluid "+currentPos.toString());
			currentPos = null;
		}
		else {
			LogUtils.getLogger().info("fail "+currentPos.toShortString());
			currentPos = null;
			return false;
		}
		
		return true;
	}

	public void tick() {

		if(tick>20) {
			tick = 0;
			if(tank.isEmpty()||tank.getFluidAmount()<1000)
				return ;
			fill();
		}
		else tick++;
		
	}
	@Override
	public void load(CompoundTag nbt) {
//		tank.readFromNBT(nbt);
		super.load(nbt);
	}
	@Override
	protected void saveAdditional(CompoundTag nbt) {
		tank.writeToNBT(nbt);
		super.saveAdditional(nbt);
	}
	


}
