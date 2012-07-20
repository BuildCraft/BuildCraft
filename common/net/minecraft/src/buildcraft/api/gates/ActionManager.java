package net.minecraft.src.buildcraft.api.gates;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipe;

public class ActionManager {

	public static Trigger[] triggers = new Trigger[1024];
	public static Action[] actions = new Action[1024];

	private static LinkedList<ITriggerProvider> triggerProviders = new LinkedList<ITriggerProvider>();
	private static LinkedList<IActionProvider> actionProviders = new LinkedList<IActionProvider>();

	public static void registerTriggerProvider(ITriggerProvider provider) {
		if (provider != null && !triggerProviders.contains(provider)) {
			triggerProviders.add(provider);
		}
	}

	public static LinkedList<Trigger> getNeighborTriggers(Block block, TileEntity entity) {
		LinkedList<Trigger> triggers = new LinkedList<Trigger>();

		for (ITriggerProvider provider : triggerProviders) {
			LinkedList<Trigger> toAdd = provider.getNeighborTriggers(block, entity);

			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}

		return triggers;
	}

	public static void registerActionProvider(IActionProvider provider) {
		if (provider != null && !actionProviders.contains(provider)) {
			actionProviders.add(provider);
		}
	}

	public static LinkedList<Action> getNeighborActions(Block block, TileEntity entity) {
		LinkedList<Action> actions = new LinkedList<Action>();

		for (IActionProvider provider : actionProviders) {
			LinkedList<Action> toAdd = provider.getNeighborActions(block, entity);

			if (toAdd != null) {
				for (Action t : toAdd) {
					if (!actions.contains(t)) {
						actions.add(t);
					}
				}
			}
		}

		return actions;
	}

	public static LinkedList<Trigger> getPipeTriggers(IPipe pipe) {
		LinkedList<Trigger> triggers = new LinkedList<Trigger>();

		for (ITriggerProvider provider : triggerProviders) {
			LinkedList<Trigger> toAdd = provider.getPipeTriggers(pipe);

			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}

		return triggers;
	}


}
