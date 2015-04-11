package buildcraft.api.statements.v2.util;

import net.minecraft.entity.player.EntityPlayer;

public class Click {
	public final EntityPlayer player;
	public final int button;

	public Click(EntityPlayer player, int button) {
		this.player = player;
		this.button = button;
	}
}
