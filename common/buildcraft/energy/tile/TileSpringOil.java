package buildcraft.energy.tile;

import java.util.HashMap;
import java.util.Map;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import buildcraft.factory.tile.TilePump;

// We don't extend TileBC here because we have no need of any of its functions.
public class TileSpringOil extends TileEntity {

    private final Map<GameProfile, PlayerPumpInfo> pumpProgress = new HashMap<>();

    public void onPumpOil(TilePump pump, BlockPos oilPos) {
        GameProfile profile = pump.getOwner();
        if (profile == null) {
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile, PlayerPumpInfo::new);
        info.lastPumpTick = world.getTotalWorldTime();
        info.sourcesPumped++;
    }

    static class PlayerPumpInfo {
        final GameProfile profile;
        long lastPumpTick = -1;
        int sourcesPumped = 0;

        public PlayerPumpInfo(GameProfile profile) {
            this.profile = profile;
        }

        public PlayerPumpInfo(NBTTagCompound nbt) {
            profile = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("profile"));
            lastPumpTick = nbt.getLong("lastPumpTick");
            sourcesPumped = nbt.getInteger("sourcesPumped");
        }

        public NBTTagCompound writeToNbt() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("profile", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
            nbt.setLong("lastPumpTick", lastPumpTick);
            nbt.setInteger("sourcesPumped", sourcesPumped);
            return nbt;
        }
    }
}
