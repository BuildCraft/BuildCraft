package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import buildcraft.api.core.BCLog;

public class PipeThreadManager {
	public abstract class Action {
		public abstract void run();
	}

	public class PipeWorldThreadManager {
		public class TickerMonitorObject {

		}

		public class Ticker implements Runnable {
			public boolean shouldStop = false;

			private double startTime;

			@Override
			public void run() {
				while (!shouldStop) {
					startTime = System.nanoTime();
					for (TileGenericPipe pipe : ImmutableList.copyOf(pipes)) {
						if (pipe.isInvalid()) {
							pipesToRemove.add(pipe);
						} else {
							pipe.updateThread();
						}
					}
					pipes.removeAll(pipesToRemove);
					pipesToRemove.clear();
					lastTime[lastTimeIndex++] = (System.nanoTime() - startTime) / 1000000.0D;
					lastTimeIndex %= lastTime.length;

					startTime = -1.0D; // Signal stop of processing
					try {
						synchronized (monitorObject) {
							monitorObject.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		protected final TickerMonitorObject monitorObject = new TickerMonitorObject();

		private final ArrayList<TileGenericPipe> pipes = new ArrayList<TileGenericPipe>(1024);
		private final HashSet<TileGenericPipe> pipesToRemove = new HashSet<TileGenericPipe>();

		private Ticker ticker = new Ticker();

		private Thread thread;
		private double[] lastTime = new double[16];
		private int lastTimeIndex = 0;

		public double getCurrentTime() {
			return lastTime[lastTimeIndex];
		}

		public double getAverageTime() {
			double l = 0;
			for (int i = 0; i < lastTime.length; i++) {
				l += lastTime[i];
			}
			return l / lastTime.length;
		}

		public int getPipeCount() {
			return pipes.size();
		}

		protected void unload() {
			if (ticker != null) {
				ticker.shouldStop = true;
			}
		}

		protected void add(TileGenericPipe pipe) {
			pipes.add(pipe);
		}

		protected void tick() {
			if (thread != null) {
				if (ticker.startTime > 0) { // Positive values signify processing, negative means waiting
					BCLog.logger.warn("WARNING: Pipes cannot keep up! [" + pipes.size() + " pipes running]");
					return;
				}
				synchronized (monitorObject) {
					monitorObject.notify();
				}
			} else {
				if (ticker != null) {
					thread = new Thread(ticker);
					thread.start();
				}
			}
		}
	}

	public static final PipeThreadManager INSTANCE = new PipeThreadManager();

	public final HashMap<World, PipeWorldThreadManager> managers = new HashMap<World, PipeWorldThreadManager>();

	public PipeThreadManager() {

	}

	public void addPipe(TileGenericPipe pipe) {
		if (!managers.containsKey(pipe.getWorld())) {
			managers.put(pipe.getWorld(), new PipeWorldThreadManager());
		}
		managers.get(pipe.getWorld()).add(pipe);
	}

	@SubscribeEvent
	public void iterate(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (managers.containsKey(event.world)) {
				managers.get(event.world).tick();
			}
		}/* else if (event.phase == TickEvent.Phase.END) {
			for (Action a : actions) {
				a.run();
			}
		}*/
	}

	@SubscribeEvent
	public void unloadWorld(WorldEvent.Unload event) {
		if (managers.containsKey(event.world)) {
			managers.get(event.world).unload();
			managers.remove(event.world);
		}
	}
}
