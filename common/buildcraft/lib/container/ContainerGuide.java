package buildcraft.lib.container;

import buildcraft.lib.BCLibItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

public class ContainerGuide extends AbstractContainerMenu {
    public ContainerGuide(@Nullable MenuType<?> menuType, int id) {
        super(menuType, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() == BCLibItems.guide.get() || player.getOffhandItem().getItem() == BCLibItems.guide.get();
    }
}
