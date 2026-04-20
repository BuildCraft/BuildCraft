package ct.buildcraft.lib.gui;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RecordSlot extends SlotItemHandler {
	
	protected Supplier<ItemStack> backgroundItem ;
	public RecordSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		this.set(stack.isEmpty() ? ItemStack.EMPTY : stack.copy().split(1));
		return false;
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return true;
	}

	@Override
	public ItemStack safeTake(int p_150648_, int p_150649_, Player p_150650_) {
		this.set(ItemStack.EMPTY);
		return ItemStack.EMPTY;
	}

	@Override
	public Optional<ItemStack> tryRemove(int p_150642_, int p_150643_, Player p_150644_) {
		this.set(ItemStack.EMPTY);
		return Optional.of(ItemStack.EMPTY);
	}

	public Slot setBackgroundItem(Supplier<ItemStack> s) {
		this.backgroundItem = s;
		return this;
	}
	
	public ItemStack getBackgroundItem() {
		return backgroundItem == null ? ItemStack.EMPTY : backgroundItem.get();
	}

	
	
	
	
}
	
