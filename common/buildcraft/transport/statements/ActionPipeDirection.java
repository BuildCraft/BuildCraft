/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;

public class ActionPipeDirection extends BCStatement implements IActionInternal {
    public final EnumFacing direction;

    public ActionPipeDirection(EnumFacing direction) {
        super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ROOT));
        this.direction = direction;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.pipe.direction", ColourUtil.getTextFullTooltip(direction));
    }

    @Override
    public IStatement rotateLeft() {
        EnumFacing face = direction.getAxis() == Axis.Y ? direction : direction.rotateY();
        return BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public String toString() {
        return "ActionPipeDirection[" + direction + "]";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCTransportSprites.getPipeDirection(direction);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_DIRECTION;
    }
}
