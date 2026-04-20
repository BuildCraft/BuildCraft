package ct.buildcraft.lib.block.fluid;

import org.jetbrains.annotations.NotNull;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;


public class BCTank implements IFluidHandler,IFluidTank{

	protected int tanks = 1;
	protected FluidStack fluid = FluidStack.EMPTY;
	protected int capacity;
	public BCTank(int capacity,Fluid flu,int amount) {
		this.fluid = new FluidStack(flu,amount);
		this.capacity = capacity;
		this.setTanks((int)(capacity/8000));
	}
	public BCTank(int capacity) {
		this.setTanks((int)(capacity/8000));
		this.capacity = capacity;
	}

	public void declineAboveTank(int tank_num) {
		int newTankCapacity = getCapacity() - tank_num * 8000;
		if(!fluid.isEmpty()&&getFluidAmount()>newTankCapacity) fluid.setAmount(newTankCapacity);
		capacity = newTankCapacity;
		setTanks(getTanks() - tank_num);
	}
	public int getCapacity() {
		// TODO Auto-generated method stub
		return capacity;
	}
	public Boolean tryAddTank(BCTank subtank) {
		if(subtank.isEmpty()) {
			this.setCapacity(this.getCapacity() + subtank.getCapacity());
			setTanks(getTanks() + subtank.getTanks());
		}
		else if(fluid.isEmpty()) {
			this.setCapacity(this.getCapacity() + subtank.getCapacity());
			this.fluid = subtank.fluid.copy();
			setTanks(getTanks() + subtank.getTanks());
		}
		else if(fluid.isFluidEqual(subtank.getFluid())) {
			this.setCapacity(this.getCapacity() + subtank.getCapacity());
			fluid.setAmount(subtank.getFluidAmount() + fluid.getAmount());
			setTanks(getTanks() + subtank.getTanks());
		}
		else return false;
		
		return true;
	}
	public @NotNull FluidStack getFluid() {
		// TODO Auto-generated method stub
		return fluid;
	}
	public int getAmountOnNUM(int num) {
		int i = getFluidAmount() - (getTanks()-num) * 8000;
		if(i>8000)
			return 8000;
		else if(i<0)
			return 0;
		else 
			return i;
	}
	public boolean isEmpty() {
		return fluid.isEmpty();
	}
    public BCTank setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }
	@Override
	public int getTanks() {
		// TODO Auto-generated method stub
		return tanks;
	}
	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		int i = fluid.getAmount() - tank*8000;
		if(i<0)
			return FluidStack.EMPTY;
		else if(i>8000)
			return new FluidStack(fluid, 8000);
		return new FluidStack(fluid, i);
	}
	@Override
	public int getTankCapacity(int tank) {
		// TODO Auto-generated method stub
		return capacity;
	}
	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return stack.isFluidEqual(fluid);
	}
	   @Override
	    public int fill(FluidStack resource, FluidAction action)
	    {
	        if (resource.isEmpty() || !isFluidValid(resource))
	        {
	            return 0;
	        }
	        if (action.simulate())
	        {
	            if (fluid.isEmpty())
	            {
	                return Math.min(capacity, resource.getAmount());
	            }
	            if (!fluid.isFluidEqual(resource))
	            {
	                return 0;
	            }
	            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
	        }
	        if (fluid.isEmpty())
	        {
	            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
	            return fluid.getAmount();
	        }
	        if (!fluid.isFluidEqual(resource))
	        {
	            return 0;
	        }
	        int filled = capacity - fluid.getAmount();

	        if (resource.getAmount() < filled)
	        {
	            fluid.grow(resource.getAmount());
	            filled = resource.getAmount();
	        }
	        else
	        {
	            fluid.setAmount(capacity);
	        }
	     
	        return filled;
	    }
	@Override
	public boolean isFluidValid(FluidStack resource) {
		
		return isEmpty()?true:resource.isFluidEqual(fluid);
	}
    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid))
        {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        int drained = maxDrain;
        if (fluid.getAmount() < drained)
        {
            drained = fluid.getAmount();
        }
        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0)
        {
            fluid.shrink(drained);
        }
        return stack;
    }
	public void setTanks(int tanks) {
		this.tanks = tanks;
	}
	public int getFluidAmount() {
		// TODO Auto-generated method stub
		return fluid.getAmount();
	}
	public void writeToNBT(CompoundTag tag) {
		tag.putInt("tanks", tanks);
		tag.putInt("capacity", capacity);
		fluid.writeToNBT(tag);
//		LogUtils.getLogger().info(Integer.toString(tag.getInt("Amount")));
	}
	public void readFromNBT(CompoundTag tag) {
		tanks = tag.getInt("tanks");
		capacity = tag.getInt("capacity");
		fluid = FluidStack.loadFluidStackFromNBT(tag);
//		LogUtils.getLogger().info(Integer.toString(tag.getInt("tanks"))+"tanks");
	}
	
	
    	
}
