package buildcraft.lib.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import buildcraft.api.permission.EnumProtectionStatus;
import buildcraft.api.permission.IOwner;
import buildcraft.api.permission.IOwnershipManager;
import buildcraft.api.permission.IPlayerOwned;

public enum PlayerOwnership implements IOwnershipManager {
    INSTANCE;

    private final Map<UUID, OwnerSettings> settings = new HashMap<>();

    @Override
    @Nonnull
    public EnumProtectionStatus resolveStatus(IPlayerOwned owned) {
        if (owned == null || owned.getOwner() == null) {
            return EnumProtectionStatus.ANYONE;
        }
        EnumProtectionStatus current = owned.getStatus();
        if (current != null) {
            return current;
        }

        UUID uuid = owned.getOwner().getPlayerUUID();
        OwnerSettings s = this.settings.get(uuid);
        if (s == null) {
            return EnumProtectionStatus.ANYONE;
        } else {
            return s.defaultStatus;
        }
    }

    @Override
    public boolean canUse(EntityPlayer attempting, IPlayerOwned owned) {
        EnumProtectionStatus resolved = resolveStatus(owned);
        if (resolved == EnumProtectionStatus.ANYONE) {
            return true;
        }
        UUID playerId = attempting.getGameProfile().getId();
        return canUseInternal(playerId, owned);
    }

    @Override
    public boolean canUse(IOwner attempting, IPlayerOwned owned) {
        EnumProtectionStatus resolved = resolveStatus(owned);
        if (resolved == EnumProtectionStatus.ANYONE) {
            return true;
        }
        if (resolved == EnumProtectionStatus.NO_AUTOMATION) {
            return false;
        }
        UUID playerId = attempting.getPlayerUUID();
        return canUseInternal(playerId, owned);
    }

    private boolean canUseInternal(UUID attempting, IPlayerOwned owned) {
        UUID playerUUID = owned.getOwner().getPlayerUUID();
        if (playerUUID.equals(attempting)) {
            return true;
        }

        OwnerSettings s = settings.get(playerUUID);
        if (s == null) {
            return false;
        } else {
            return s.isFriend(attempting);
        }
    }

    public void save(MinecraftServer server) {

    }

    public void load(MinecraftServer server) {

    }
}
