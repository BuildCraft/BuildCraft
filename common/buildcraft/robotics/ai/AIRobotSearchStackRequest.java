/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.StackRequest;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationRequestItems;
import buildcraft.robotics.statements.ActionStationRequestItemsMachine;

public class AIRobotSearchStackRequest extends AIRobot {

    public StackRequest request = null;

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
            if (!ai.success()) {
                terminate();
            } else {
                request = getOrderFromRequestingAction(((AIRobotSearchStation) ai).targetStation);

                if (request == null) {
                    request = getOrderFromRequestingStation(((AIRobotSearchStation) ai).targetStation, true);
                }

                terminate();
            }
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
        if (!ActionRobotFilter.canInteractWithItem(station, filter, ActionStationRequestItemsMachine.class)) {
            return null;
        }

        IRequestProvider provider = station.getRequestProvider();
        if (provider == null) {
            return null;
        }

        for (int i = 0; i < provider.getNumberOfRequests(); ++i) {
            StackRequest requestFound = provider.getAvailableRequest(i);

            if (requestFound != null && !isBlacklisted(requestFound.stack) && filter.matches(requestFound.stack)) {
                requestFound.station = station;

                if (take) {
                    if (provider.takeRequest(i, robot)) {
                        return requestFound;
                    }
                } else {
                    return requestFound;
                }
            }
        }

        return null;
    }

    private StackRequest getOrderFromRequestingAction(DockingStation station) {
        for (StatementSlot s : station.getActiveActions()) {
            if (s.statement instanceof ActionStationRequestItems) {
                for (IStatementParameter p : s.parameters) {
                    StatementParameterItemStack param = (StatementParameterItemStack) p;

                    if (param != null && !isBlacklisted(param.getItemStack())) {
                        StackRequest req = new StackRequest();
                        req.station = station;
                        req.stack = param.getItemStack();

                        return req;
                    }
                }
            }
        }

        return null;
    }

    private class StationProviderFilter implements IStationFilter {

        @Override
        public boolean matches(DockingStation station) {
            return getOrderFromRequestingAction(station) != null || getOrderFromRequestingStation(station, false) != null;
        }
    }

}
