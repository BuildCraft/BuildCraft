package ct.buildcraft.core.lib;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

public class BCEnergyStorage implements IEnergyStorage,INBTSerializable<Tag>{
	
	public static final String ENERGY_TYPE = "MJ";
	
	protected int energy;
	protected int capacity;
	protected int maxInput;
	protected int maxOutput;
	protected boolean canExtract;
	protected boolean canRecive;
	private boolean canReceive;;
	
	
	public BCEnergyStorage(int energy, int capacity, int maxInput, int maxOutput) {
		this.energy = energy;
		this.capacity = capacity;
		this.maxInput = maxInput;
		this.maxOutput = maxOutput;
		this.canExtract = maxOutput > 0;
		this.canRecive = maxInput > 0;
		
	}
	public BCEnergyStorage(int energy,int capacity) {
		this.energy = energy;
		this.capacity = capacity;
		this.maxInput = capacity;
		this.maxOutput = capacity;
		this.canExtract = true;
		this.canReceive = true;
		
	}
	
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canRecive)
            return 0;
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxInput, maxReceive));
        if (!simulate)
            energy += energyReceived;
//        LogUtils.getLogger().info(Integer.toString(energy));
        return energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxOutput, maxExtract));
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
	}

    @Override
    public int getEnergyStored()
    {
        return energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }

    @Override
    public boolean canExtract()
    {
        return energy>0;
    }

    @Override
    public boolean canReceive()
    {
        return energy<capacity;
    }

    @Override
    public Tag serializeNBT()
    {
        return IntTag.valueOf(this.getEnergyStored());
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        if (!(nbt instanceof IntTag intNbt))
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        this.energy = intNbt.getAsInt();
    }
    public float getEnergyPerccent() {
    	return (this.energy)/(float)capacity;
    }
}
