package buildcraft.silicon.render;

import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.utils.NBTUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageFontRenderer extends FontRenderer {
	private static final RenderItem itemRender = new RenderItem();
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final FontRenderer realRenderer = mc.fontRenderer;
	private final NBTTagCompound pkgTag;
	private final Pattern stringPattern = Pattern.compile("^\\{\\{BC_PACKAGE_SPECIAL:([0-2])}}$");

	public PackageFontRenderer(ItemStack packageStack) {
		super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), mc.fontRenderer.getUnicodeFlag());
		this.pkgTag = NBTUtils.getItemData(packageStack);
	}

	@Override
	public int getStringWidth(String s) {
		Matcher m = stringPattern.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(s));
		if (!m.find()) {
			return realRenderer.getStringWidth(s);
		}

		return 21;
	}

	@Override
	public int drawString(String s, int x, int y, int color, boolean shadow) {
		Matcher m = stringPattern.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(s));
		if (!m.find()) {
			return realRenderer.drawString(s, x, y, color, shadow);
		}

		int begin = Integer.parseInt(m.group(1)) * 3;
		int rx = x;

		for (int slotPos = begin; slotPos < begin + 3; slotPos++) {
			GL11.glPushMatrix();

			if (pkgTag.hasKey("item" + slotPos)) {
				ItemStack slotStack = ItemStack.loadItemStackFromNBT(pkgTag.getCompoundTag("item" + slotPos));
				if (slotStack != null) {
					GL11.glTranslatef(0.0F, 0.0F, 32.0F);
					GL11.glScalef(0.5F, 0.5F, 0.5F);
					FontRenderer font = slotStack.getItem().getFontRenderer(slotStack);
					itemRender.zLevel = 200.0F;

					if (font == null || font instanceof PackageFontRenderer) {
						font = Minecraft.getMinecraft().fontRenderer;
					}

					itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), slotStack, rx * 2, y * 2);
					itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), slotStack, rx * 2, y * 2);
					itemRender.zLevel = 0.0F;
				} else {
					realRenderer.drawString("X", rx, y, 0xFF0000);
				}
			}

			rx += 7;

			GL11.glPopMatrix();
		}
		return rx;
	}
}
