package ct.buildcraft.lib.gui;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.gui.elem.ToolTip;
import ct.buildcraft.lib.misc.LocaleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class TankContainerData implements ContainerData{
	
	private static final ForgeRegistry<Fluid> fluids = (ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS;
	public static final int LEN = 5;
	protected final Tank[] tanks ;
	protected final int count;
	
	public TankContainerData(Tank... tanks) {
		this.tanks = tanks;
		this.count = tanks.length;
	}

	@Override
	public int get(int p) {
		if(p >= LEN*count||p<0) {
			BCLog.logger.error("TankContainer.get: %d index out of range : %d",p , count*LEN);
			return 0;
		}
		return switch (p%LEN) {
			case 0 -> fluids.getID(tanks[p/LEN].getFluidType());
			case 1 -> tanks[p/LEN].getFluidAmount();
			case 2 -> fluids.getID(tanks[p/LEN].getFluidForRender().getRawFluid());
			case 3 -> tanks[p/LEN].getFluidForRender().getAmount();
			case 4 -> tanks[p/LEN].getCapacity();
			default -> 0;
		};
		 
	}

	@Override
	public void set(int index, int info) {
/*		if(playerId != 0xFFFF&&(index <size*2 && index >= 0&&(index&1)==0))
			tanks[index/2].onGuiClicked(playerId);*/
	}

	@Override
	public int getCount() {
		return count*LEN;
	}
	
	public FluidStack getFluidStack(int index) {
		return tanks[index].getFluidInTank(0);
	}
	
	public static Fluid getFluid(int id) {
		return fluids.getValue(id);
	}
	
	public static FluidStack getFluidStack(ContainerData data, int point) {
		return new FluidStack(fluids.getValue(data.get(point + 0)), data.get(point + 1));
	}
	
	public static FluidStack getRenderFluidStack(ContainerData data, int point) {
		return new FluidStack(fluids.getValue(data.get(point + 2)), data.get(point + 3));
	}
	
	public static Fluid getFluidType(ContainerData data, int point) {
		return fluids.getValue(data.get(point + 0));
	}
	
	public static short getFluidAmount(ContainerData data, int point) {
		return (short) data.get(point + 1);
	}
	
	public static Fluid getRenderFluidId(ContainerData data, int point) {
		return fluids.getValue(data.get(point + 2));
	}
	
	public static short getRenderFluidAmount(ContainerData data, int point) {
		return (short) data.get(point + 3);
	}
	
	public static short getTankCapacity(ContainerData data, int point) {
		return (short) data.get(point + 4);
	}
	
	public static ToolTip getTankToopTip(ContainerData data, int point) {
		return new ToolTip() {
	        @Override
	        public void refresh() {
	            clear();
	            int amount = data.get(point + 1);
	            FluidStack fluidStack = fluids.getValue(data.get(point + 2)) == Fluids.EMPTY ? FluidStack.EMPTY
	            		: new FluidStack(fluids.getValue(data.get(point + 2)), data.get(point + 3));;
	            if (fluidStack != FluidStack.EMPTY && amount > 0) {
	                add(fluidStack.getDisplayName());
	            }
	            add(LocaleUtil.localizeFluidStaticAmount(amount, data.get(point + 4)).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	            FluidStack serverFluid = new FluidStack(fluids.getValue(data.get(point + 0)), data.get(point + 1));
	            if (serverFluid != FluidStack.EMPTY && serverFluid.getAmount() > 0) {
	                add(Component.literal("BUG: Server-side fluid on client!").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
	                add(serverFluid.getDisplayName());
	                add(LocaleUtil.localizeFluidStaticAmount(serverFluid.getAmount(), data.get(point + 4)));
	            }
	        }
	    };

	}
	

}
