package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashSet;
import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import buildcraft.api.core.BCLog;

public class PipeThreadManager {
	public abstract class Action {
		public abstract void run();
	}

	public class Ticker implements Runnable {
		@Override
		public void run() {
			for (TileGenericPipe pipe : ImmutableList.copyOf(pipes)) {
				if (pipe.isInvalid()) {
					pipesToRemove.add(pipe);
				} else {
					pipe.updateThread();
				}
			}
			pipes.removeAll(pipesToRemove);
			pipesToRemove.clear();
		}
	}

	public static final PipeThreadManager INSTANCE = new PipeThreadManager();

	public PipeThreadManager() {

	}

	private final ArrayList<TileGenericPipe> pipes = new ArrayList<TileGenericPipe>(1024);
	private final HashSet<TileGenericPipe> pipesToRemove = new HashSet<TileGenericPipe>();
	private final ArrayList<Action> actions = new ArrayList<Action>(256);

	private Thread lastThread;

	public void addPipe(TileGenericPipe pipe) {
		pipes.add(pipe);
	}

	public void addAction(Action a) {
		synchronized(actions) {
			actions.add(a);
		}
	}

	@SubscribeEvent
	public void iterate(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (lastThread != null && lastThread.isAlive()) {
				BCLog.logger.warn("WARNING: Pipes cannot keep up! [" + pipes.size() + " pipes running]");
				return;
			} else {
				lastThread = new Thread(new Ticker());
				lastThread.start();
			}
		} else if (event.phase == TickEvent.Phase.END) {
			for (Action a : actions) {
				a.run();
			}
		}
	}
}
