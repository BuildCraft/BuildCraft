package buildcraft.lib;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.container.ContainerGuide;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class BCLibScreenConstructors {
    public static final ScreenManager.IScreenFactory<ContainerGuide, GuiGuide> GUIDE = new ScreenManager.IScreenFactory<ContainerGuide, GuiGuide>() {
        @Nonnull
        @Override
        public GuiGuide create(@Nonnull ContainerGuide container, @Nonnull PlayerInventory inv, @Nonnull ITextComponent title) {
            PlayerEntity player = inv.player;
            ItemStack stack;
            if (player.getMainHandItem().getItem() == BCLibItems.guide.get()) {
                stack = player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() == BCLibItems.guide.get()) {
                stack = player.getOffhandItem();
            } else {
                stack = StackUtil.EMPTY;
            }
            String name = ItemGuide.getBookName(stack);
            if (name == null || name.isEmpty()) {
                return new GuiGuide(container, title);
            } else {
                return new GuiGuide(container, name, title);
            }
        }
    };
}
