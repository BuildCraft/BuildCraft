package buildcraft.lib.misc;

import java.lang.ref.WeakReference;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayerFactory;

import buildcraft.api.core.IBCFakePlayer;

public enum FakePlayerUtil implements IBCFakePlayer {
    INSTANCE;

    public final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");
    private WeakReference<EntityPlayer> fakeBCPlayer = new WeakReference<>(null);

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world) {
        EntityPlayer player = FakePlayerFactory.get(world, gameProfile);

        return new WeakReference<>(player);
    }

    private WeakReference<EntityPlayer> createNewPlayer(WorldServer world, BlockPos pos) {
        EntityPlayer player = FakePlayerFactory.get(world, gameProfile);
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return new WeakReference<>(player);
    }

    @Override
    public final WeakReference<EntityPlayer> getBuildCraftPlayer(WorldServer world) {
        if (fakeBCPlayer.get() == null) {
            fakeBCPlayer = createNewPlayer(world);
        } else {
            fakeBCPlayer.get().world = world;
        }

        return fakeBCPlayer;
    }

    public final WeakReference<EntityPlayer> getBuildCraftPlayer(WorldServer world, BlockPos pos) {
        if (fakeBCPlayer.get() == null) {
            fakeBCPlayer = createNewPlayer(world, pos);
        } else {
            fakeBCPlayer.get().world = world;
            fakeBCPlayer.get().posX = pos.getX();
            fakeBCPlayer.get().posY = pos.getY();
            fakeBCPlayer.get().posZ = pos.getZ();
        }

        return fakeBCPlayer;
    }
}
