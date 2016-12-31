/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.*;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

public class TriggerPower extends BCStatement implements ITriggerInternal, ITriggerExternal {
    public static class Neighbor {
        public TileEntity tile;
        public EnumPipePart side;

        public Neighbor(TileEntity tile, EnumPipePart side) {
            this.tile = tile;
            this.side = side;
        }
    }

    private final boolean high;

    public TriggerPower(boolean high) {
        super("buildcraft:energyStored" + (high ? "high" : "low"));
        this.high = high;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
    }

    public boolean isTriggeredMjConnector(IMjReadable readable) {
        if (readable == null) {
            return false;
        }
        long stored = readable.getStored();
        long max = readable.getCapacity();

        if (max > 0) {
            double level = stored / (double) max;
            if (high) {
                return level > 0.95;
            } else {
                return level < 0.05;
            }
        }
        return false;
    }

    public static boolean isTriggeringTile(TileEntity tile) {
        return isTriggeringTile(tile, null);
    }

    public static boolean isTriggeringTile(TileEntity tile, EnumFacing face) {
        return tile.getCapability(MjAPI.CAP_READABLE, face) != null;
    }

    protected boolean isActive(ICapabilityProvider tile, EnumPipePart side) {
        return isTriggeredMjConnector(tile.getCapability(MjAPI.CAP_READABLE, side.face));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(source.getTile(), EnumPipePart.CENTER);
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        return isActive(target, EnumPipePart.fromFacing(side.getOpposite()));
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER;
    }
}
