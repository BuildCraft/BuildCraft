package buildcraft.lib.permission;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.StringUtils;

import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.WorkerThreadUtil;

public class TaskLookupGameProfile implements Callable<GameProfile> {
    private final GameProfile from;

    public TaskLookupGameProfile(GameProfile from) {
        this.from = from;
    }

    public static Future<GameProfile> lookupLater(GameProfile from, boolean requireTexture) {
        if (from == null) {
            throw new NullPointerException("from");
        }
        if (from.isComplete() && (!requireTexture || from.getProperties().containsKey("textures"))) {
            return Futures.immediateFuture(from);
        }
        if (getProfileCache() == null) {
            return Futures.immediateFuture(from);
        }
        Callable<GameProfile> callable = new TaskLookupGameProfile(from);
        return WorkerThreadUtil.executeWorkTask(callable);
    }

    @Override
    public GameProfile call() throws Exception {
        long sleep = (long) ((Math.random() * 4000) + 3000);
        Thread.sleep(sleep);
        PlayerProfileCache cache = getProfileCache();
        if (cache == null) {
            return from;
        }
        GameProfile profile;
        if (!StringUtils.isNullOrEmpty(from.getName())) {
            profile = cache.getGameProfileForUsername(from.getName());
        } else {
            profile = cache.getProfileByUUID(from.getId());
        }
        if (profile == null) {
            // Thats not good: the lookup failed
            return from;
        }
        profile = TileEntitySkull.updateGameprofile(profile);
        if (profile == null) {
            throw new IllegalStateException("Null Profile!");
        }
        BCLog.logger.info("[lib.perm.profile] Successfully looked up the owner of " + from + " as " + profile);
        return profile;
    }

    @Nullable
    public static PlayerProfileCache getProfileCache() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return null;
        }
        return server.getPlayerProfileCache();
    }
}
