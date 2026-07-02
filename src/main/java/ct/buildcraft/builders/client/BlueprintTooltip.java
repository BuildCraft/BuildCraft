package ct.buildcraft.builders.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class BlueprintTooltip implements TooltipComponent {
	private final ItemStack blueprint;
	
	public BlueprintTooltip(ItemStack blueprint) {
		this.blueprint = blueprint;
	}

	public ItemStack getBlueprint() {
		return blueprint;
	}

}
