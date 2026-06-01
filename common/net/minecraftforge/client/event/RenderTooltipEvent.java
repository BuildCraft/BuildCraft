// STUB(R.Chen): Forge RenderTooltipEvent — compile shim.
package net.minecraftforge.client.event;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RenderTooltipEvent {
    public static class Pre extends RenderTooltipEvent {
        private final ItemStack stack;
        private final List<Text> lines;
        private int x, y;
        private boolean canceled;

        public Pre(ItemStack stack, List<Text> lines, int x, int y) {
            this.stack = stack;
            this.lines = lines;
            this.x = x;
            this.y = y;
        }

        public ItemStack getStack() { return stack; }
        public List<Text> getLines() { return lines; }
        public int getX() { return x; }
        public int getY() { return y; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        public boolean isCanceled() { return canceled; }
        public void setCanceled(boolean canceled) { this.canceled = canceled; }
    }

    public static class PostText extends RenderTooltipEvent {}
    public static class Color extends RenderTooltipEvent {}
}
