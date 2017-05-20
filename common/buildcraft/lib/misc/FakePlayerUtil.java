package buildcraft.lib.misc;

import buildcraft.api.core.IBCFakePlayer;
import buildcraft.lib.fake.FakePlayerBC;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum FakePlayerUtil implements IBCFakePlayer {
    INSTANCE;

    private final Map<GameProfile, FakePlayerBC> players = new HashMap<>();

    @Deprecated
    public final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

    @Override
    @Deprecated
    public FakePlayerBC getBuildCraftPlayer(WorldServer world) {
        return getFakePlayer(world, BlockPos.ORIGIN, gameProfile);
    }

    @Override
    public FakePlayerBC getFakePlayer(WorldServer world, GameProfile profile) {
        return getFakePlayer(world, BlockPos.ORIGIN, profile);
    }

    public FakePlayerBC getFakePlayer(WorldServer world, BlockPos pos, GameProfile profile) {
        players.computeIfAbsent(profile, p -> new FakePlayerBC(world, profile));
        FakePlayerBC player = players.get(profile);
        player.world = world;
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return player;
    }

    public void unloadWorld(WorldServer world) {
        players.values().removeIf(entry -> entry.world == world);
    }
}
