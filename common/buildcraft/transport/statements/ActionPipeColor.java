package buildcraft.transport.statements;

import net.minecraft.item.EnumDyeColor;

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

public class ActionPipeColor extends BCStatement implements IActionInternal {

    public final EnumDyeColor color;

    public ActionPipeColor(EnumDyeColor color) {
        super("buildcraft:pipe.color." + color.getName(), "buildcraft.pipe." + color.getName());
        this.color = color;
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.action.pipe.item.color"), ColourUtil.getTextFullTooltip(color));
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // Pipes listen for this -- we don't need to do anything here
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_COLOUR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getGuiSprite() {
        return BCTransportSprites.ACTION_PIPE_COLOUR[color.ordinal()];
    }
}
