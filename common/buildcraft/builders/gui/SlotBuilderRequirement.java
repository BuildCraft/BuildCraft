package buildcraft.builders.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;

import buildcraft.core.blueprints.RequirementItemStack;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;

public class SlotBuilderRequirement extends AdvancedSlot {
	public RequirementItemStack stack;

	public SlotBuilderRequirement(GuiAdvancedInterface gui, int x, int y) {
		super(gui, x, y);
	}

	@Override
	public ItemStack getItemStack() {
		return stack != null ? stack.stack : null;
	}

	@Override
	public void drawStack(ItemStack item) {
		int cornerX = (gui.width - gui.getXSize()) / 2;
		int cornerY = (gui.height - gui.getYSize()) / 2;

		gui.drawStack(item, cornerX + x, cornerY + y);

		if (stack != null) {
			// Render real stack size
			String s = String.valueOf(stack.size > 999 ? Math.min(99, stack.size / 1000) + "K" : stack.size);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			gui.getFontRenderer().drawStringWithShadow(s,
					cornerX + x + 17 - gui.getFontRenderer().getStringWidth(s), cornerY + y + 9, 16777215);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}
}
