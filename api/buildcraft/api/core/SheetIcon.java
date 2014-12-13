package buildcraft.api.core;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SheetIcon {
	private ResourceLocation texture;
	private int u, v;

	public SheetIcon(ResourceLocation texture, int u, int v) {
		this.texture = texture;
		this.u = u;
		this.v = v;
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	public int getU() {
		return u;
	}

	public int getV() {
		return v;
	}
}
