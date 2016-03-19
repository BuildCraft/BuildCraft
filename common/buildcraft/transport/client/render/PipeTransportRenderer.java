package buildcraft.transport.client.render;

import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;

import java.util.HashMap;
import java.util.Map;

public abstract class PipeTransportRenderer<T extends PipeTransport> {
	public static final Map<Class<? extends PipeTransport>, PipeTransportRenderer> RENDERER_MAP = new HashMap<Class<? extends PipeTransport>, PipeTransportRenderer>();

	public abstract void render(Pipe<T> pipe, double x, double y, double z, float f);
}
