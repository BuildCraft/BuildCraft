/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.robotics.item.ItemRedstoneBoard;
import buildcraft.robotics.item.ItemRobot;
import buildcraft.robotics.item.ItemRobotGoggles;
import buildcraft.robotics.item.ItemRobotStation;
import buildcraft.robotics.plug.PluggableRobotStation;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;

public class BCRoboticsItems {

    private static final RegistrationHelper HELPER = new RegistrationHelper(BCRobotics.MODID);

    public static final Map<RedstoneBoardRobotNBT, RegistryObject<ItemRobot>> robot = Maps.newConcurrentMap();
    public static RegistryObject<ItemRobotStation> robotStation;
    public static final Map<RedstoneBoardNBT<?>, RegistryObject<ItemRedstoneBoard>> redstoneBoard = Maps.newConcurrentMap();
    public static RegistryObject<ItemRobotGoggles> robotGoggles;

    public static void preInit() {
        robotStation = HELPER.addItem("item.plug.robot_station", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRobotStation(idBC, properties, BCRoboticsPlugs.robotStation, PluggableRobotStation::new));
        List<RedstoneBoardNBT<?>> allBoardNbts = Lists.newArrayList();
        allBoardNbts.addAll(RedstoneBoardRegistry.instance.getAllBoardNBTs());
        allBoardNbts.add(RedstoneBoardRegistry.instance.getEmptyRobotBoard());
        // board
        for (RedstoneBoardNBT<?> boardNBT : allBoardNbts) {
            RegistryObject<ItemRedstoneBoard> boardItemReg = HELPER.addItem("item.redstone_board", boardNBT.getID().getPath(), ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneBoard(idBC, properties, boardNBT));
            redstoneBoard.put(boardNBT, boardItemReg);
            RedstoneBoardRegistry.instance.getBoardNBTItemMap().put(boardNBT, boardItemReg);
        }
        // robot
        for (RedstoneBoardNBT<?> boardNBT : allBoardNbts) {
            if (boardNBT instanceof RedstoneBoardRobotNBT) {
                RedstoneBoardRobotNBT robotNBT = (RedstoneBoardRobotNBT) boardNBT;
                RegistryObject<ItemRobot> itemRobot = HELPER.addItem(
                        "item.robot",
                        robotNBT.getRobotId().getPath(),
                        ItemPropertiesCreator.common1(), (idBC, properties) -> new ItemRobot(idBC, properties, robotNBT)
                );
                robot.put(robotNBT, itemRobot);
            }
        }
        robotGoggles = HELPER.addItem("item.robot_googles", ItemPropertiesCreator.common1(), ItemRobotGoggles::new);
    }
}
