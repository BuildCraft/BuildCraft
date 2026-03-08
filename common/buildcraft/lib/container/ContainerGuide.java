package buildcraft.lib.container;

import buildcraft.lib.BCLibItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nullable;

public class ContainerGuide extends Container {
    public ContainerGuide(@Nullable ContainerType<?> menuType, int id) {
        super(menuType, id);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return player.getMainHandItem().getItem() == BCLibItems.guide.get() || player.getOffhandItem().getItem() == BCLibItems.guide.get();
    }
}
