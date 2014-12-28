package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashSet;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import buildcraft.api.core.BCLog;

public class PipeThreadManager {
	public abstract class Action {
		public abstract void run();
	}

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
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static final PipeThreadManager INSTANCE = new PipeThreadManager();

	public PipeThreadManager() {

	}

	private final ArrayList<TileGenericPipe> pipes = new ArrayList<TileGenericPipe>(1024);
	private final HashSet<TileGenericPipe> pipesToRemove = new HashSet<TileGenericPipe>();
	private final ArrayList<Action> actions = new ArrayList<Action>(256);

	private Thread thread;
	private Ticker ticker;
	private TickerMonitorObject monitorObject = new TickerMonitorObject();
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

	public void addPipe(TileGenericPipe pipe) {
		pipes.add(pipe);
	}

	/*public void addAction(Action a) {
		synchronized(actions) {
			actions.add(a);
		}
	}*/

	@SubscribeEvent
	public void iterate(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
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
		}/* else if (event.phase == TickEvent.Phase.END) {
			for (Action a : actions) {
				a.run();
			}
		}*/
	}

	public void refreshTicker() {
		if (ticker != null && thread != null) {
			ticker.shouldStop = true;
		}

		ticker = new Ticker();
		thread = null;
	}
}
