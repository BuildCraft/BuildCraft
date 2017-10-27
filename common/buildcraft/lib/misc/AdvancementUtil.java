package buildcraft.lib.misc;

import java.util.UUID;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.core.BCLog;

public class AdvancementUtil {

    public static void unlockAdvancement(EntityPlayer player, ResourceLocation advancementName) {
        if (player instanceof EntityPlayerMP) {
            AdvancementManager advancementManager = ((EntityPlayerMP) player).getServerWorld().getAdvancementManager();
            Advancement advancement = advancementManager.getAdvancement(advancementName);
            if (advancement != null) {
                //never assume the advancement exists, we create them but they are removable by datapacks
                ((EntityPlayerMP) player).getAdvancements().grantCriterion(advancement, "code_trigger");
            } else {
                BCLog.logger.warn("Attempted to trigger undefined advancement: " + advancementName);
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
