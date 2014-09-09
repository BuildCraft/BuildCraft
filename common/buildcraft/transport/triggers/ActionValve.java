/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IGate;
import buildcraft.api.transport.IPipe;
import buildcraft.core.triggers.BCActionActive;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;


public class ActionValve extends BCActionActive {

    public enum ValveState {
	OPEN(true, true),
	INPUT_ONLY(true, false),
	OUTPUT_ONLY(false, true),
	CLOSED(false, false);

	public static final ValveState[] VALUES = values();
	public final boolean inputOpen;
	public final boolean outputOpen;

	private ValveState(boolean in, boolean out) {
	    inputOpen = in;
	    outputOpen = out;
	}
    }

    public final ValveState state;


    public ActionValve(ValveState valveState) {
	super("buildcraft:pipe.valve." + valveState.name().toLowerCase(Locale.ENGLISH));
	state = valveState;
    }

    @Override
    public String getDescription() {
	return StringUtils.localize("gate.action.pipe.valve." + state.name().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
	icon = iconRegister.registerIcon("buildcraft:triggers/action_valve_" + state.name().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public int maxParameters() {
	return 1;
    }

    @Override
    public int minParameters() {
	return 1;
    }

    @Override
    public IActionParameter createParameter(int index) {
	IActionParameter param = null;

	if (index == 0) {
	    param = new ActionParameterDirection();
	}

	return param;
    }

    @Override
    public void actionActivate(IGate gate, IActionParameter[] parameters) {
	if (parameters[0] != null) {
	    IPipe pipe = gate.getPipe();

	    if (pipe != null && pipe instanceof Pipe) {
		PipeTransport transport = ((Pipe) pipe).transport;
		ForgeDirection side = ((ActionParameterDirection) parameters[0]).direction;

		if (side != ForgeDirection.UNKNOWN) {
		    transport.allowInput(side, state.inputOpen);
		    transport.allowOutput(side, state.outputOpen);
		}
	    }
	}
    }
}
