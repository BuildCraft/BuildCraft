// STUB(R.Chen): Forge ItemTooltipEvent — compile shim.
package net.minecraftforge.event.entity.player;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemTooltipEvent {
    private final ItemStack stack;
    private final PlayerEntity player;
    private final List<Text> toolTip;

    public ItemTooltipEvent(ItemStack stack, PlayerEntity player, List<Text> toolTip) {
        this.stack = stack;
        this.player = player;
        this.toolTip = toolTip;
    }

    public ItemStack getItemStack() { return stack; }
    public PlayerEntity getEntityPlayer() { return player; }
    public List<Text> getToolTip() { return toolTip; }
}
