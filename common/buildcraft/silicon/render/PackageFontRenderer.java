package buildcraft.silicon.render;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import buildcraft.core.lib.utils.NBTUtils;

public class PackageFontRenderer extends FontRenderer {
	private static final RenderItem itemRender = new RenderItem();
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final FontRenderer realRenderer = mc.fontRenderer;
	private final ItemStack packageStack;
	private final NBTTagCompound pkgTag;

	public PackageFontRenderer(ItemStack packageStack) {
		super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), mc.fontRenderer.getUnicodeFlag());
		this.packageStack = packageStack;
		this.pkgTag = NBTUtils.getItemData(packageStack);
	}

	@Override
	public int getStringWidth(String s) {
		if (s.indexOf("|") < 0) {
			return realRenderer.getStringWidth(s);
		}

		String output = s;
		int w = 0;

		while (output.indexOf("|") >= 0) {
			String pre = output.substring(0, output.indexOf("|"));
			w += realRenderer.getStringWidth(pre);

			String text = output.substring(output.indexOf("|") + 1).split(" ")[0];
			if (text.startsWith("S")) {
				w += 5;
			}

			output = output.substring(output.indexOf("|") + text.length() + 1);
		}
		w += realRenderer.getStringWidth(output);
		return w;
	}

	@Override
	public int drawString(String s, int x, int y, int color, boolean shadow) {
		if (s.indexOf("|") < 0) {
			return realRenderer.drawString(s, x, y, color, shadow);
		}

		String output = s;
		int rx = x;

		while (output.indexOf("|") >= 0) {
			String pre = output.substring(0, output.indexOf("|"));
			rx = realRenderer.drawString(pre, rx, y, color, shadow);
			GL11.glPushMatrix();

			String text = output.substring(output.indexOf("|") + 1).split(" ")[0];
			if (text.startsWith("S")) {
				int slotPos = Integer.parseInt(text.substring(1));
				if (pkgTag.hasKey("item" + slotPos)) {
					ItemStack slotStack = ItemStack.loadItemStackFromNBT(pkgTag.getCompoundTag("item" + slotPos));
					GL11.glTranslatef(0.0F, 0.0F, 32.0F);
					GL11.glScalef(0.5F, 0.5F, 0.5F);
					FontRenderer font = slotStack.getItem().getFontRenderer(slotStack);
					itemRender.zLevel = 200.0F;

					if (font == null || font instanceof PackageFontRenderer) {
						font = Minecraft.getMinecraft().fontRenderer;
					}

					itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), slotStack, rx * 2, y * 2);
					itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), slotStack, rx * 2, y * 2);
					itemRender.zLevel = 0.0F;
				}

				rx += 5;
			}

			GL11.glPopMatrix();
			output = output.substring(output.indexOf("|") + text.length() + 1);
		}
		rx = realRenderer.drawString(output, rx, y, color, shadow);
		return rx;
	}
}
