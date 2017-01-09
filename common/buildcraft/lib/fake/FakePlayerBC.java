package buildcraft.lib.fake;

import com.mojang.authlib.GameProfile;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBC extends FakePlayer {
    public FakePlayerBC(WorldServer world, GameProfile name) {
        super(world, name);
    }

    @Override
    public void openEditSign(TileEntitySign signTile) {
    }
}
