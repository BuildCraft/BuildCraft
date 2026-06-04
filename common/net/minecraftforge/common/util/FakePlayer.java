// STUB(R.Chen): Forge FakePlayer — compile shim. TODO: FakePlayerBC should extend ServerPlayerEntity directly.
package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class FakePlayer extends ServerPlayerEntity {
    public FakePlayer(ServerWorld world, GameProfile profile) {
        super(world.getServer(), world, profile, null);
    }
}
