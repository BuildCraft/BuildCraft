package ct.buildcraft.robotics.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.IRequestProvider;
import ct.buildcraft.api.robots.ResourceId;
import ct.buildcraft.lib.inventory.filter.PassThroughStackFilter;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.robotics.IStationFilter;
import ct.buildcraft.robotics.StackRequest;
import net.minecraft.world.item.ItemStack;

/** Searches for an active station item request that a Delivery robot can fulfil. */
public class AIRobotSearchStackRequest extends AIRobot {
    public StackRequest request;
    public DockingStation station;

    private Collection<ItemStack> blackList;
    private IStackFilter filter;

    public AIRobotSearchStackRequest(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotSearchStackRequest(EntityRobotBase robot, IStackFilter filter, Collection<ItemStack> blackList) {
        this(robot);
        this.filter = filter == null ? new PassThroughStackFilter() : filter;
        this.blackList = blackList;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotSearchStation(robot, new StationProviderFilter(), robot.getZoneToWork()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStation search) {
            if (ai.success()) {
                station = search.targetStation;
                request = getOrderFromRequestingStation(station, true);
            }
            terminate();
        }
    }

    @Override
    public boolean success() {
        return request != null;
    }

    private boolean isBlacklisted(ItemStack stack) {
        if (stack == null || stack.isEmpty() || blackList == null) {
            return false;
        }
        for (ItemStack black : blackList) {
            if (!black.isEmpty() && StackUtil.matchesStackOrList(black, stack)) {
                return true;
            }
        }
        return false;
    }

    private StackRequest getOrderFromRequestingStation(DockingStation station, boolean take) {
        for (StackRequest req : getAvailableRequests(station)) {
            ItemStack requestedStack = req.getStack();
            if (!requestedStack.isEmpty() && !isBlacklisted(requestedStack) && filter.matches(requestedStack)) {
                req.setStation(station);
                if (!take) {
                    return req;
                }

                ResourceId resourceId = req.getResourceId(robot.level);
                if (resourceId != null && robot.getRegistry().take(resourceId, robot)) {
                    return req;
                }
            }
        }
        return null;
    }

    private Collection<StackRequest> getAvailableRequests(DockingStation station) {
        List<StackRequest> result = new ArrayList<>();
        if (station == null) {
            return result;
        }

        IRequestProvider provider = station.getRequestProvider();
        if (provider == null) {
            return result;
        }

        for (int i = 0; i < provider.getRequestsCount(); i++) {
            ItemStack stack = provider.getRequest(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            StackRequest req = new StackRequest(provider, i, stack);
            req.setStation(station);
            ResourceId resourceId = req.getResourceId(robot.level);
            if (resourceId != null && !robot.getRegistry().isTaken(resourceId)) {
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
