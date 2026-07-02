package ct.buildcraft.lib.gui.containerData;

import java.util.function.IntSupplier;

import net.minecraft.world.inventory.ContainerData;

public class SingleProviderData implements ContainerData {
	
	protected final IntSupplier getter ;

	public SingleProviderData(IntSupplier getter) {
		this.getter = getter;
		
	}
	
	@Override
	public int get(int index) {
		return getter.getAsInt();
	}

	@Override
	public void set(int p_39285_, int p_39286_) {
	}

	@Override
	public int getCount() {
		return 1;
	}

}
