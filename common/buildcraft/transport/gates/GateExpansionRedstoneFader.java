/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.Arrays;
import java.util.Collection;

import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;

public final class GateExpansionRedstoneFader extends GateExpansionBuildcraft implements IGateExpansion {

    public static GateExpansionRedstoneFader INSTANCE = new GateExpansionRedstoneFader();

    private GateExpansionRedstoneFader() {
        super("fader");
    }

    @Override
    public GateExpansionController makeController(TileEntity pipeTile) {
        return new GateExpansionControllerRedstoneFader(pipeTile);
    }

    @Override
    public boolean canAddToGate(int numTriggerParameters, int numActionParameters) {
        return numTriggerParameters >= 1 || numActionParameters >= 1;
    }

    private class GateExpansionControllerRedstoneFader extends GateExpansionController {

        public GateExpansionControllerRedstoneFader(TileEntity pipeTile) {
            super(GateExpansionRedstoneFader.this, pipeTile);
        }

        @Override
        public void addTriggers(Collection<ITriggerInternal> list) {
            super.addTriggers(list);
            list.remove(BuildCraftCore.triggerRedstoneActive);
            list.remove(BuildCraftCore.triggerRedstoneInactive);
            list.addAll(Arrays.asList(BuildCraftTransport.triggerRedstoneFader));
        }

        @Override
        public void addActions(Collection<IActionInternal> list) {
            super.addActions(list);
            list.remove(BuildCraftCore.actionRedstone);
            list.add(BuildCraftTransport.actionRedstoneFader);
        }
    }
}
