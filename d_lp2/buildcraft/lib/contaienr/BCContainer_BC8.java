package buildcraft.lib.contaienr;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public abstract class BCContainer_BC8 extends Container {
    public final EntityPlayer player;

    public BCContainer_BC8(EntityPlayer player) {
        this.player = player;
    }
}
