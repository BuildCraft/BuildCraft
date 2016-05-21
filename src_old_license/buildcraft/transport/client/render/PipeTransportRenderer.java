package buildcraft.transport.client.render;

import java.util.HashMap;
import java.util.Map;

import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;

public abstract class PipeTransportRenderer<T extends PipeTransport> {
	public static final Map<Class<? extends PipeTransport>, PipeTransportRenderer> RENDERER_MAP = new HashMap<>();

	public abstract void render(Pipe<T> pipe, double x, double y, double z, float f);
}
