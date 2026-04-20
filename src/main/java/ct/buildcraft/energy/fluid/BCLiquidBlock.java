package ct.buildcraft.energy.fluid;

import java.util.Optional;
import java.util.function.Supplier;

import ct.buildcraft.energy.BCEnergyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class BCLiquidBlock extends LiquidBlock{
	
	public final boolean isStick;
	public final int igniteOdds;
	public final int burnOdds;

	public BCLiquidBlock(Supplier<? extends FlowingFluid> source, Properties propertes, boolean isStick, int igniteOdds, int burnOdds) {
		super(source, propertes);
		this.isStick = isStick;
		this.igniteOdds = igniteOdds;
		this.burnOdds = burnOdds;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if(BCEnergyConfig.oilIsSticky && isStick)
			entity.makeStuckInBlock(state	, new Vec3(0.25D, (double)0.05F, 0.25D));
		if(getFluid().getFluidType().getTemperature()>350) {
			entity.lavaHurt();
		}
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		// TODO Auto-generated method stub
		return super.getPickupSound();
	}//TODO

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return BCEnergyConfig.enableOilBurn ? igniteOdds : 0;
	}
	
	@Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return BCEnergyConfig.enableOilBurn ? burnOdds : 0;
    }

	@Override
	public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
		return false;//BCEnergyConfig.enableOilBurn && igniteOdds > 0;
	}
	
}
