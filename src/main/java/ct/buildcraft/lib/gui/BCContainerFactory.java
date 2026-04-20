package ct.buildcraft.lib.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;

public class BCContainerFactory<T extends MenuBC_Neptune> implements IContainerFactory<T>{
	
	final BCMenuSupplier<T> constructor;
	
	public BCContainerFactory(BCMenuSupplier<T> constructor) {
		this.constructor = constructor;
	}
	
	@Override
	public T create(int windowId, Inventory inv, FriendlyByteBuf data) {
		return constructor.create(windowId, inv, data);
/*		if(data != null && data.isReadable())
			containerArchitectTable.clientInit(data);
		return containerArchitectTable;*/
	}
	
	public interface BCMenuSupplier<T extends MenuBC_Neptune>{
		T create(int windowId, Inventory inv, FriendlyByteBuf data);
	}
	
	public static <T extends MenuBC_Neptune> MenuType<T> create(BCMenuSupplier<T> constructor){
		return IForgeMenuType.create(new BCContainerFactory<>(constructor));
	}

}
