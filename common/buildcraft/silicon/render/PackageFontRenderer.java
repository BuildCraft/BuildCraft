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
    private static final RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer realRenderer = mc.fontRendererObj;
    private final ItemStack packageStack;
    private final NBTTagCompound pkgTag;

    public PackageFontRenderer(ItemStack packageStack) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), mc.fontRendererObj.getUnicodeFlag());
        this.packageStack = packageStack;
        this.pkgTag = NBTUtils.getItemData(packageStack);
    }

    @Override
    public int getStringWidth(String s) {
        if (s.indexOf("SPECIAL:") < 0) {
            return realRenderer.getStringWidth(s);
        }

        return 21;
    }

	// TODO: Bug-test!
    @Override
    public int drawString(String s, float x, float y, int color, boolean shadow) {
        if (s.indexOf("SPECIAL:") < 0) {
            return realRenderer.drawString(s, x, y, color, shadow);
        }

        int begin = Integer.parseInt(s.substring(s.length() - 1)) * 3;
        int rx = (int) x;

        for (int slotPos = begin; slotPos < begin + 3; slotPos++) {
            GL11.glPushMatrix();

            if (pkgTag.hasKey("item" + slotPos)) {
                ItemStack slotStack = ItemStack.loadItemStackFromNBT(pkgTag.getCompoundTag("item" + slotPos));
                GL11.glTranslatef(0.0F, 0.0F, 32.0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                FontRenderer font = slotStack.getItem().getFontRenderer(slotStack);
                itemRender.zLevel = 200.0F;

                if (font == null || font instanceof PackageFontRenderer) {
                    font = Minecraft.getMinecraft().fontRendererObj;
                }

                itemRender.renderItemAndEffectIntoGUI(slotStack, rx * 2, (int) (y * 2));
                itemRender.renderItemOverlayIntoGUI(font, slotStack, rx * 2, (int) (y * 2), "");
                itemRender.zLevel = 0.0F;
            }

            rx += 7;

            GL11.glPopMatrix();
        }
        return rx;
    }
}
