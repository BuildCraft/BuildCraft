package buildcraft.lib.container;

import buildcraft.lib.BCLibItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ContainerGuide extends AbstractContainerMenu {
    public ContainerGuide(@Nullable MenuType<?> menuType, int id) {
        super(menuType, id);
    }

    // 1.20.1 forced
    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        // from 1.18.2 AbstractContainerMenu
        return this.slots.get(p_38942_).getItem();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() == BCLibItems.guide.get() || player.getOffhandItem().getItem() == BCLibItems.guide.get();
    }
}
