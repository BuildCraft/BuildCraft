package buildcraft.lib.net.command;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import buildcraft.lib.contaienr.BCContainer_BC8;

public class CommandTargets {
    public static final ICommandTarget TARGET_TILE = new CommandTargetTile(null);
    public static final ICommandTarget TARGET_ENTITY = new CommandTargetEntity(null);
    public static final ICommandTarget TARGET_CONTAINER = new CommandTargetContainer(null);

    public static ICommandTarget getForTile(TileEntity tile) {
        return new CommandTargetTile(tile);
    }

    public static ICommandTarget getForEntity(Entity entity) {
        return new CommandTargetEntity(entity);
    }

    public static ICommandTarget getForContainer(BCContainer_BC8 container) {
        return new CommandTargetContainer(container);
    }
}
