/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;


//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.NetworkData;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipeTile;

public class StatementParameterDirection implements IActionParameter, ITriggerParameter {

    //        static IIcon[] icons = new IIcon[ForgeDirection.values().length];

    	@NetworkData
	public ForgeDirection direction = ForgeDirection.UNKNOWN;

	public StatementParameterDirection() {
	}

    /*
        @Override
        public void registerIcons(IIconRegister register) {
	    for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
		icons[d.ordinal()] = register.registerIcon("buildcraft:triggers/trigger_dir_" + d.name().toLowerCase());
	    }
	}

	@Override
	public IIcon getIconToDraw() {
	     return icons[direction.ordinal()];
	}

    */

	@Override
	public ItemStack getItemStackToDraw() {
		return null;
	}

	@Override
	public IIcon getIconToDraw() {
	    if (direction == ForgeDirection.UNKNOWN) {
		return null;
	    } else {
		return StatementIconProvider.INSTANCE.getIcon(StatementIconProvider.Action_Parameter_Direction_Down + direction.ordinal());
	    }
	}

	@Override
	public void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack) {
	    do {
		direction = ForgeDirection.getOrientation((direction.ordinal() + 1) % ForgeDirection.values().length);
	    } while (direction != ForgeDirection.UNKNOWN && !pipe.isPipeConnected(direction));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
	    nbt.setInteger("direction", direction.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    if (nbt.hasKey("direction")) {
		direction = ForgeDirection.values()[nbt.getInteger("direction")];
	    } else {
		direction = ForgeDirection.UNKNOWN;
	    }
	}

	@Override
	public boolean equals(Object object) {
	    if (object instanceof StatementParameterDirection) {
		StatementParameterDirection param = (StatementParameterDirection) object;
		return param.direction == this.direction;
	    }
	    return false;
	}
}
