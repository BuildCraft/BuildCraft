package buildcraft.transport.render;

import buildcraft.transport.ItemCanister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;

public class CanisterItemRender implements IItemRenderer {

	private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;
	private static final ResourceLocation ITEM_TEXTURE = TextureMap.locationItemsTexture;

	private ItemCanister canister;

	public CanisterItemRender(ItemCanister canister) {
		this.canister = canister;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type.equals(ItemRenderType.ENTITY);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		IIcon overlay = canister.overlay;
		IIcon cannister = canister.getIconFromDamage(0);

		GL11.glPushMatrix();
		if (type.equals(ItemRenderType.EQUIPPED)) {
			GL11.glRotated(180.0D, 0.0D, 0.0D, 1.0D);
			GL11.glTranslated(-1.0D, -1.0D, 0.0D);
		} else if (type.equals(ItemRenderType.ENTITY)) {
			GL11.glRotated(180.0D, 0.0D, 0.0D, 1.0D);
			GL11.glRotated(90.0D, 0.0D, 1.0D, 0.0D);
			GL11.glTranslated(-0.5D, -0.9D, 0.0D);
			if (item.isOnItemFrame()) {
				GL11.glTranslated(0.1D, 0.4D, 0.0D);
				GL11.glScaled(0.85D, 0.85D, 0.85D);
			}
		} else if (type.equals(ItemRenderType.EQUIPPED_FIRST_PERSON)) {
			GL11.glTranslated(1.0D, 1.0D, 0.0D);
			GL11.glRotated(180.0D, 0.0D, 0.0D, 1.0D);
		}

		if (item.stackTagCompound != null && item.getTagCompound().hasKey("Fluid")) {
			Fluid fluid = FluidRegistry.getFluid(item.stackTagCompound.getCompoundTag("Fluid").getString("FluidName"));
			if (fluid != null) {
				Minecraft.getMinecraft().renderEngine.bindTexture(ITEM_TEXTURE);
				renderMask(overlay, fluid.getIcon(), type);
			}
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(ITEM_TEXTURE);

		if (!type.equals(ItemRenderType.INVENTORY))
			ItemRenderer.renderItemIn2D(Tessellator.instance, cannister.getMinU(), cannister.getMaxV(), cannister.getMaxU(), cannister.getMinV(), cannister.getIconWidth(), cannister.getIconHeight(), 0.0625F);
		else
			renderIcon(cannister, 0.0D);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	private void renderMask(IIcon mask, IIcon subIcon, ItemRenderType type) {
		if (mask == null || subIcon == null)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		Tessellator tessellator = Tessellator.instance;

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		if (type.equals(ItemRenderType.INVENTORY))
			preRenderInvIcon(mask, 0.001D);
		else
			preRenderWorldIcon(mask, 0.001D);
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		if (type.equals(ItemRenderType.INVENTORY))
			preRenderInvIcon(mask, -0.0635D);
		else
			preRenderWorldIcon(mask, -0.0635D);
		tessellator.draw();

		Minecraft.getMinecraft().renderEngine.bindTexture(BLOCK_TEXTURE);
		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDepthMask(false);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		if (type.equals(ItemRenderType.INVENTORY))
			preRenderInvIcon(subIcon, 0.001D);
		else
			preRenderWorldIcon(subIcon, 0.001D);
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		if (type.equals(ItemRenderType.INVENTORY))
			preRenderInvIcon(subIcon, -0.0635D);
		else
			preRenderWorldIcon(subIcon, -0.0635D);
		tessellator.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void preRenderInvIcon(IIcon icon, double z) {
		Tessellator.instance.addVertexWithUV(16.0D, 0.0D, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 0.0D, z, icon.getMinU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 16.0D, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16.0D, 16.0D, z, icon.getMaxU(), icon.getMaxV());
	}

	private void preRenderWorldIcon(IIcon icon, double z) {
		Tessellator.instance.addVertexWithUV(1.0D, 0.0D, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 0.0D, z, icon.getMinU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 1.0D, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(1.0D, 1.0D, z, icon.getMaxU(), icon.getMaxV());
	}

	private void renderIcon(IIcon icon, double z) {
		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.addVertexWithUV(16.0D, 0.0D, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 0.0D, z, icon.getMinU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0.0D, 16.0D, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16.0D, 16.0D, z, icon.getMaxU(), icon.getMaxV());
		Tessellator.instance.draw();
	}
}
