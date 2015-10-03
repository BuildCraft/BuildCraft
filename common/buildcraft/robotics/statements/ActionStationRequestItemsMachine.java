/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;

public class ActionStationRequestItemsMachine extends BCStatement implements IActionInternal {

    public ActionStationRequestItemsMachine() {
        super("buildcraft:station.provide_machine_request");
        setLocation("buildcraftrobotics:triggers/action_station_machine_request");
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("gate.action.station.provide_machine_request");
    }

    // @Override
    // public void registerIcons(TextureAtlasSpriteRegister iconRegister) {
    // icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_station_machine_request");
    // }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
