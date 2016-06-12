package buildcraft.lib.permission;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;

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

    public static GameProfile lookup(GameProfile from) {
        try {
            return lookupLater(from).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return from;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Future<GameProfile> lookupLater(GameProfile from) {
        if (from == null) {
            throw new NullPointerException("from");
        }
        if (from.isComplete() && from.getProperties().containsKey("textures")) {
            return Futures.immediateFuture(from);
        }
        Callable<GameProfile> callable = new TaskLookupGameProfile(from);
        return WorkerThreadUtil.executeWorkTask(callable);
    }

    @Override
    public GameProfile call() throws Exception {
        GameProfile profile;
        if (!StringUtils.isNullOrEmpty(from.getName())) {
//            getProfileCache().addEntry(from);
            profile = getProfileCache().getGameProfileForUsername(from.getName());
        } else {
            profile = getProfileCache().getProfileByUUID(from.getId());
        }
        if (profile == null) {
            throw new IllegalStateException("Null Profile!");
        }
        profile = TileEntitySkull.updateGameprofile(profile);
        if (profile == null) {
            throw new IllegalStateException("Null Profile!");
        }
        return profile;
    }

    public static PlayerProfileCache getProfileCache() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache();
    }
}
