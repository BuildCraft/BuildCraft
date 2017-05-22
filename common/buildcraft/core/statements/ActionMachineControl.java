/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import buildcraft.api.tiles.TilesAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

public class ActionMachineControl extends BCStatement implements IActionExternal {
    public final Mode mode;

    public ActionMachineControl(Mode mode) {
        super("buildcraft:machine." + mode.name().toLowerCase(Locale.ROOT), "buildcraft.machine." + mode.name().toLowerCase(Locale.ROOT));
        this.mode = mode;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        IControllable controllable = target.getCapability(TilesAPI.CAP_CONTROLLABLE, side.getOpposite());
        if (controllable != null) {
            controllable.setControlMode(mode, false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getGuiSprite() {
        return BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.ACTION_MACHINE_CONTROL;
    }
}
