package ct.buildcraft.core.blockEntity;

import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.core.block.BlockEngine;
import ct.buildcraft.core.lib.BCEnergyStorage;
import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class TileEngineBase_0 extends BlockEntity implements ICapabilityProvider{
	
    public static final double HEAT_PER_MJ = 0.0023;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;
    
    protected double heat = MIN_HEAT;// TODO: sync gui data
	public int power = 0;
	public float progress = 0;
	protected boolean isRedstonePowered;
	public boolean CONNECTED = true;
	protected boolean a = true;
    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected BCEnergyStorage battery = new BCEnergyStorage(0, 50);
    protected int ticks = 0;
	

	public TileEngineBase_0(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState bs) {
		super(p_155228_, p_155229_, bs);
		isRedstonePowered = bs.getValue(BlockEngine.ENABLED);
		
	}
	
	LazyOptional<IEnergyStorage> EnergyLazyOptional = LazyOptional.of(() -> battery);

	// Supplied instance (e.g. () -> inventoryHandler)
	// Ensure laziness as initialization should only happen when needed

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
	  if (cap == ForgeCapabilities.ENERGY) {
	    return EnergyLazyOptional.cast();
	  }
	  return super.getCapability(cap, side);
	}

	public void tick() {
//		LogUtils.getLogger().info("ss");

//       LogUtils.getLogger().info(Double.toString(heat));
		if(ticks >20)
			ticks = 0;
		ticks ++;
		isRedstonePowered = this.getBlockState().getValue(BlockEngine.ENABLED);
        if(isRedstonePowered) {
    		updataEngine();
            getPowerStage();
        }
        else {
    		if (heat > MIN_HEAT) {
                heat -= 5f;
                if (heat < MIN_HEAT) {
                    heat = MIN_HEAT;
                }
            }
        	power = power >0 ? power-1:0;
        }
        
        if (level.isClientSide()) {
            if (CONNECTED&&isRedstonePowered) {
            	if(a)
                progress += getPistonSpeed();
            	else
            	progress -= getPistonSpeed();

                if (progress >= 1) {
                    progress = 1;
                    a = false;
                }
                else if(progress <= 0){
                	progress = 0;
                	a=true;
                }
            } else if (progress > 0) {
                progress -= getPistonSpeed();
            }
//            LogUtils.getLogger().info(Double.toString(getPistonSpeed()));
            return;
        }
	}
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.02;
            case GREEN:
                return 0.04;
            case YELLOW:
                return 0.08;
            case RED:
                return 0.12;
            default:
                return 0;
        }
    }
    protected EnumPowerStage computePowerStage() {
        double heatLevel = (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);;
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }
    public EnumPowerStage getPowerStage() {
 //       if (!level.isClientSide()) {
            EnumPowerStage newStage = computePowerStage();
//            LogUtils.getLogger().info("ss");
            if (powerStage != newStage) {
                powerStage = newStage;
  //              sendNetworkUpdate(NET_RENDER_DATA);
//            }
        }

        return powerStage;
    }
    public abstract void updataEngine() ;
    
    public double getPowerLevel() {
        return power / (double) getMaxPower();
    }
    
    protected abstract int getMaxPower();

	public void updateHeatLevel() {
        heat = ((MAX_HEAT - MIN_HEAT) * getPowerLevel()) + MIN_HEAT;
    }

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getIdealHeatLevel() {
        return heat / IDEAL_HEAT;
    }
    
    protected void engineUpdate() {
        if (!isRedstonePowered) {
            if (power >= 1) {
                power -= 1;
            } else if (power < 1) {
                power = 0;
            }
        }
        else {
        	
        }
    }

    @OnlyIn(Dist.CLIENT)
	public abstract TextureAtlasSprite getTextureBack();

    @OnlyIn(Dist.CLIENT)
	public abstract TextureAtlasSprite getTextureSide();


	

}
