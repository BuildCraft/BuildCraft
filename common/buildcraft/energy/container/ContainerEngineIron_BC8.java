package buildcraft.energy.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;

public class ContainerEngineIron_BC8 extends ContainerBCTile<TileEngineIron_BC8> {
    private static final int PLAYER_INV_START = 95;

    public final WidgetFluidTank widgetTankFuel;
    public final WidgetFluidTank widgetTankCoolant;
    public final WidgetFluidTank widgetTankResidue;

    public ContainerEngineIron_BC8(EntityPlayer player, TileEngineIron_BC8 engine) {
        super(player, engine);

        addFullPlayerInventory(PLAYER_INV_START);

        widgetTankFuel = addWidget(new WidgetFluidTank(this, engine.tankFuel));
        widgetTankCoolant = addWidget(new WidgetFluidTank(this, engine.tankCoolant));
        widgetTankResidue = addWidget(new WidgetFluidTank(this, engine.tankResidue));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }
}
