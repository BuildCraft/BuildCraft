package ct.buildcraft.compat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.lib.misc.CapUtil;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public enum CompatCapTransfromer {
	INSTANCE;
	
	Map<Class<?>, BiFunction<?, Direction, IFluidHandler>> fluidCapRegistry = new HashMap<>();
	
	public <T extends CapabilityProvider<?>> IFluidHandler transfromFluidCap(T provider, Direction face) {
		BiFunction<T, Direction, IFluidHandler> function = (BiFunction<T, Direction, IFluidHandler>) fluidCapRegistry.get(provider.getClass());
		return function == null ? null : function.apply(provider, face);
	}
	
	public <T extends CapabilityProvider<?>> void registryFluidCapTransform(Class<T> clazz, BiFunction<T, Direction, IFluidHandler> function) {
		fluidCapRegistry.put(clazz, function);
	}
	
	public <T extends CapabilityProvider<?>, E> LazyOptional<E> getCap(T provider, Capability<E> capability, Direction face){
		LazyOptional<E> orginCap = provider.getCapability(capability, face);
		if(orginCap.isPresent()) return orginCap;
		if(capability == CapUtil.CAP_FLUIDS) {
			IFluidHandler transfromFluidCap = transfromFluidCap(provider, face);
			return transfromFluidCap == null ? LazyOptional.empty() : LazyOptional.of(() -> transfromFluidCap).cast();
		}
		return LazyOptional.empty();
	}
}
