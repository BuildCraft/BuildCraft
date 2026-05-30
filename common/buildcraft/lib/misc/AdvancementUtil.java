/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.misc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Yarn 1.20.1 renames:
//   net.minecraft.advancements.Advancement       → net.minecraft.advancement.Advancement
//   AdvancementManager (Forge accessor)           → net.minecraft.server.ServerAdvancementLoader
//   PlayerAdvancements                            → net.minecraft.advancement.PlayerAdvancementTracker
//   EntityPlayer / EntityPlayerMP                 → PlayerEntity / ServerPlayerEntity
//   ResourceLocation                              → Identifier
//   playerMP.getServerWorld()                     → ServerPlayerEntity.getServerWorld()
//   world.getAdvancementManager()                 → server.getAdvancementLoader()
//   loader.getAdvancement(name)                   → loader.get(Identifier)
//   playerMP.getAdvancements()                    → ServerPlayerEntity.getAdvancementTracker()
//   tracker.setPlayer(playerMP)                   → tracker.setOwner(serverPlayer)
//   tracker.grantCriterion(advancement, name)     → tracker.grantCriterion(advancement, name)
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import buildcraft.api.core.BCLog;

public class AdvancementUtil {
    private static final Set<Identifier> UNKNOWN_ADVANCEMENTS = new HashSet<>();

    public static void unlockAdvancement(PlayerEntity player, Identifier advancementName) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            ServerWorld serverWorld = playerMP.getServerWorld();
            ServerAdvancementLoader advancementLoader = serverWorld.getServer().getAdvancementLoader();
            if (advancementLoader == null) {
                // Because this *can* happen
                return;
            }
            Advancement advancement = advancementLoader.get(advancementName);
            if (advancement != null) {
                // never assume the advancement exists, we create them but they are removable by datapacks
                PlayerAdvancementTracker tracker = playerMP.getAdvancementTracker();
                // When the fake player gets constructed it will set itself to the main player advancement tracker
                // (So this just harmlessly removes it)
                tracker.setOwner(playerMP);
                tracker.grantCriterion(advancement, "code_trigger");
            } else if (UNKNOWN_ADVANCEMENTS.add(advancementName)) {
                BCLog.logger.warn("[lib.advancement] Attempted to trigger undefined advancement: " + advancementName);
            }
        }
    }

    // STUB(R.Chen): the Forge global server accessor (FMLCommonHandler.getMinecraftServerInstance()) has no
    // Fabric equivalent until BCLibInitializer stores the MinecraftServer from ServerLifecycleEvents.SERVER_STARTING.
    // Until that holder exists, the UUID overload is a no-op. TODO(R.Chen): resolve the player via
    // server.getPlayerManager().getPlayer(UUID) once the server reference is available.
    public static boolean unlockAdvancement(UUID player, Identifier advancementName) {
        return false;
    }
}
