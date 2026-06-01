package buildcraft.energy.tile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.AdvancementUtil;

import buildcraft.core.tile.ITileOilSpring;
import net.minecraft.nbt.NbtElement;

// We don't extend TileBC here because we have no need of any of its functions.
public class TileSpringOil extends BlockEntity implements IDebuggable, ITileOilSpring {

    private static final Identifier ADVANCEMENT_PUMP_LARGE_OIL_WELL = new Identifier(
        "buildcraftfactory:black_gold"
    );

    private final Map<GameProfile, PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();

    /** An approximation of the total number of oil source blocks in the oil spring. The actual number will be less than
     * this, so this is taken as an approximation.
     * <p>
     * Note that this SHOULD NEVER be set! (Except by the generator, and readFromNbt) */
    public int totalSources;

    @Override
    public void onPumpOil(GameProfile profile, BlockPos oilPos) {
        if (profile == null) {
            // BCLog.logger.warn("Unknown owner for pump at " + pump.getPos());
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile, PlayerPumpInfo::new);
        info.lastPumpTick = world.getTotalWorldTime();
        info.sourcesPumped++;

        // BCLog.logger.info("Pumped " + info.sourcesPumped + " / " + totalSources + " at " + oilPos + " (for " +
        // System.identityHashCode(this) + ", "+getPos()+")");
        if (info.sourcesPumped >= totalSources * 7 / 8) {
            // BCLog.logger.info("Pumped nearly all oil blocks!");
            if (oilPos.equals(getPos().up())) {
                AdvancementUtil.unlockAdvancement(profile.getId(), ADVANCEMENT_PUMP_LARGE_OIL_WELL);
            }
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void readFromNBT(NbtCompound nbt) {
        super.readFromNBT(nbt);
        NbtList list = nbt.getList("pumpProgress", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            PlayerPumpInfo info = new PlayerPumpInfo(list.getCompoundTagAt(i));
            pumpProgress.put(info.profile, info);
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public NbtCompound writeToNBT(NbtCompound nbt) {
        super.writeToNBT(nbt);
        nbt.putInt("totalSources", totalSources);
        NbtList list = new NbtList();
        for (PlayerPumpInfo info : pumpProgress.values()) {
            list.appendTag(info.writeToNbt());
        }
        nbt.put("pumpProgress", list);
        return nbt;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("totalSources = " + totalSources);
        boolean added = false;
        for (PlayerPumpInfo info : pumpProgress.values()) {
            if (!added) {
                left.add("Player Progress:");
                added = true;
            }
            left.add("  " + info.profile.getName() + " = " + info.sourcesPumped + " ( "
                + (world.getTotalWorldTime() - info.lastPumpTick) / 20 + "s )");
        }
    }

    static class PlayerPumpInfo {
        final GameProfile profile;
        long lastPumpTick = -1;
        int sourcesPumped = 0;

        public PlayerPumpInfo(GameProfile profile) {
            this.profile = profile;
        }

        public PlayerPumpInfo(NbtCompound nbt) {
            profile = net.minecraft.nbt.NbtHelper.toGameProfile(nbt.getCompound("profile"));
            lastPumpTick = nbt.getLong("lastPumpTick");
            sourcesPumped = nbt.getInt("sourcesPumped");
        }

        public NbtCompound writeToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.put("profile", net.minecraft.nbt.NbtHelper.writeGameProfile(new NbtCompound(), profile));
            nbt.putLong("lastPumpTick", lastPumpTick);
            nbt.putInt("sourcesPumped", sourcesPumped);
            return nbt;
        }
    }
}
