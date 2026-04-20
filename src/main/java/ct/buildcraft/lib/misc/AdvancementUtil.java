package ct.buildcraft.lib.misc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.advancements.Advancement;
//import net.minecraft.entity.player.PlayerMP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.advancements.AdvancementManager;
//import net.minecraft.advancements.PlayerAdvancements;
//import net.minecraft.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

//import net.minecraftforge.fml.common.FMLCommonHandler;

//import ct.buildcraft.api.core.BCLog;

public class AdvancementUtil {
    private static final Set<ResourceLocation> UNKNOWN_ADVANCEMENTS = new HashSet<>();

    public static void unlockAdvancement(Player player, ResourceLocation advancementName) {
        if (player instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) player;
            ServerAdvancementManager advancementManager = playerMP.getServer().getAdvancements();
            if (advancementManager == null) {
                // Because this *can* happen
                return;
            }
            Advancement advancement = advancementManager.getAdvancement(advancementName);
            if (advancement != null) {
                // never assume the advancement exists, we create them but they are removable by datapacks
                PlayerAdvancements tracker = playerMP.getAdvancements();
                // When the fake player gets constructed it will set itself to the main player advancement tracker
                // (So this just harmlessly removes it)
                tracker.setPlayer(playerMP);
                tracker.award(advancement, "code_trigger");
            } else if (UNKNOWN_ADVANCEMENTS.add(advancementName)) {
                BCLog.logger.warn("[lib.advancement] Attempted to trigger undefined advancement: " + advancementName);
            }
        }
    }

    public static boolean unlockAdvancement(UUID player, ResourceLocation advancementName) {
        ServerPlayer playermp = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player);
        if (playermp != null) {
            unlockAdvancement((Player) playermp, advancementName);
            return true;
        }
        return false;
    }
}
