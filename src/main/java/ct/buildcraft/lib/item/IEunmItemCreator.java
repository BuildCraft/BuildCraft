package ct.buildcraft.lib.item;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item.Properties;

@FunctionalInterface
public interface IEunmItemCreator<E extends Enum<E> & StringRepresentable, P extends ItemByEnum<?>>{
	public P create(Properties p_40566_, E type);
}
