/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import buildcraft.api.core.SheetIcon;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileGenericPipe;

public class StatementParameterDirection implements IStatementParameter {
	public EnumFacing direction = null;
    
	public StatementParameterDirection() {
		
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		if (source.getTile() instanceof TileGenericPipe) {
			do {
				direction = EnumFacing.getFront((direction.ordinal() + (mouse.getButton() > 0 ? -1 : 1)) % 6);
			} while (((TileGenericPipe) source.getTile()).isPipeConnected(direction));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		if (direction != null) {
			nbt.setByte("direction", (byte) direction.ordinal());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    if (nbt.hasKey("direction")) {
	    	direction = EnumFacing.getFront(nbt.getByte("direction"));
	    } else {
	    	direction = null;
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

	@Override
	public String getDescription() {
		if (direction == null) {
			return "";
		} else {
			return StringUtils.localize("direction." + direction.name().toLowerCase());
		}
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeActionDirection";
	}

	@Override
	public SheetIcon getIcon() {
		return new SheetIcon(BCStatement.STATEMENT_ICONS, 10 + direction.ordinal(), 15);
	}

	@Override
	public IStatementParameter rotateLeft() {
		StatementParameterDirection d = new StatementParameterDirection();
		d.direction = direction.rotateY();
		return d;
	}
}
