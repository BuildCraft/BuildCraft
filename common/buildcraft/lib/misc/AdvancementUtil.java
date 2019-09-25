package buildcraft.lib.misc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.core.BCLog;

public class AdvancementUtil {
    private static final Set<ResourceLocation> UNKNOWN_ADVANCEMENTS = new HashSet<>();

    public static void unlockAdvancement(EntityPlayer player, ResourceLocation advancementName) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            AdvancementManager advancementManager = playerMP.getServerWorld().getAdvancementManager();
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
                tracker.grantCriterion(advancement, "code_trigger");
            } else if (UNKNOWN_ADVANCEMENTS.add(advancementName)) {
                BCLog.logger.warn("[lib.advancement] Attempted to trigger undefined advancement: " + advancementName);
            }
        }
    }

    public static boolean unlockAdvancement(UUID player, ResourceLocation advancementName) {
        Entity entity = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(player);
        if (entity != null && entity instanceof EntityPlayerMP) {
            unlockAdvancement((EntityPlayer) entity, advancementName);
            return true;
        }
        return false;
    }
}
