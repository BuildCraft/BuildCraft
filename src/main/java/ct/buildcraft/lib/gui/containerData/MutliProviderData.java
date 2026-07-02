package ct.buildcraft.lib.gui.containerData;

import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import net.minecraft.world.inventory.ContainerData;

public class MutliProviderData implements ContainerData{
	
	protected final IntUnaryOperator getter;
	protected final IntSupplier size;
	
	public MutliProviderData(IntUnaryOperator getter, IntSupplier size) {
		this.getter = getter;
		this.size = size;
	}

	@Override
	public int get(int p_39284_) {
		return getter.applyAsInt(p_39284_);
	}

	@Override
	public void set(int p_39285_, int p_39286_) {
	}

	@Override
	public int getCount() {
		return size.getAsInt();
	}

}
