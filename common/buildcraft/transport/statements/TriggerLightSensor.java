package buildcraft.transport.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.core.Position;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;

public class TriggerLightSensor extends BCStatement implements ITriggerInternal {
	private final boolean bright;

	public TriggerLightSensor(boolean bright) {
		super("buildcraft:light_" + (bright ? "bright" : "dark"));
		this.bright = bright;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.light." + (bright ? "bright" : "dark"));
	}

	@Override
	public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
		TileEntity tile = source.getTile();
		Position pos = new Position(tile);
		pos.orientation = ((ISidedStatementContainer) source).getSide();
		pos.moveForwards(1.0);

		int lightLevel = tile.getWorldObj().getBlockLightValue((int) pos.x, (int) pos.y, (int) pos.z);

		return (lightLevel < 8) ^ bright;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcrafttransport:triggers/trigger_light_" + (bright ? "bright" : "dark"));
	}
}
