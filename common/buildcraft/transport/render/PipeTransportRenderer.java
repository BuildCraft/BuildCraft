package buildcraft.transport.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;

public abstract class PipeTransportRenderer<T extends PipeTransport> {
	public static final Map<Class<? extends PipeTransport>, PipeTransportRenderer> RENDERER_MAP = new HashMap<Class<? extends PipeTransport>, PipeTransportRenderer>();

	public boolean useServerTileIfPresent() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void bindTexture(ResourceLocation location) {
		Minecraft.getMinecraft().renderEngine.bindTexture(location);
	}

	public abstract void render(Pipe<T> pipe, double x, double y, double z, float f);
}
