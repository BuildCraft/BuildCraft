package buildcraft.lib.misc;

import java.util.UUID;

import buildcraft.lib.fake.FakePlayerFactoryBC;
import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import buildcraft.api.core.IBCFakePlayer;

public enum FakePlayerUtil implements IBCFakePlayer {
    INSTANCE;

    @Deprecated
    public final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

    @Override
    @Deprecated
    public FakePlayer getBuildCraftPlayer(WorldServer world) {
        return getFakePlayer(world, BlockPos.ORIGIN, gameProfile);
    }

    @Override
    public FakePlayer getFakePlayer(WorldServer world, GameProfile profile) {
        return getFakePlayer(world, BlockPos.ORIGIN, profile);
    }

    public FakePlayer getFakePlayer(WorldServer world, BlockPos pos, GameProfile profile) {
        FakePlayer player = FakePlayerFactoryBC.get(world, profile);
        player.world = world;
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return player;
    }
}
