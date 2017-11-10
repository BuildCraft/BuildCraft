/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.StackRequest;

public class AIRobotSearchStackRequest extends AIRobot {

	public StackRequest request = null;
	public DockingStation station = null;

	private Collection<ItemStack> blackList;

	private IStackFilter filter;

	public AIRobotSearchStackRequest(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchStackRequest(EntityRobotBase iRobot, IStackFilter iFilter, Collection<ItemStack> iBlackList) {
		this(iRobot);

		blackList = iBlackList;
		filter = iFilter;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchStation(robot, new StationProviderFilter(), robot.getZoneToWork()));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchStation) {
			if (ai.success()) {
				request = getOrderFromRequestingStation(((AIRobotSearchStation) ai).targetStation, true);
			}

			terminate();
		}
	}

	@Override
	public boolean success() {
		return request != null;
	}

	private boolean isBlacklisted(ItemStack stack) {
		for (ItemStack black : blackList) {
			if (StackHelper.isMatchingItem(stack, black)) {
				return true;
			}
		}

		return false;
	}

	private StackRequest getOrderFromRequestingStation(DockingStation station, boolean take) {

		for (StackRequest req : getAvailableRequests(station)) {
			if (!isBlacklisted(req.getStack()) && filter.matches(req.getStack())) {
				req.setStation(station);
				if (take) {
					if (robot.getRegistry().take(req.getResourceId(robot.worldObj), robot)) {
						return req;
					}
				} else {
					return req;
				}
			}
		}

		return null;
	}

	private Collection<StackRequest> getAvailableRequests(DockingStation station) {
		List<StackRequest> result = new ArrayList<StackRequest>();

		IRequestProvider provider = station.getRequestProvider();
		if (provider == null) {
			return result;
		}

		for (int i = 0; i < provider.getRequestsCount(); i++) {
			if (provider.getRequest(i) == null) {
				continue;
			}
			StackRequest req = new StackRequest(provider, i, provider.getRequest(i));
			req.setStation(station);
			if (!robot.getRegistry().isTaken(req.getResourceId(robot.worldObj))) {
				result.add(req);
			}
		}
		return result;
	}

	private class StationProviderFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			return getOrderFromRequestingStation(station, false) != null;
		}
	}

}
