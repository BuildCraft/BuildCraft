package buildcraft.energy.tile;

import buildcraft.api.core.BCLog;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.tile.TilePump;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// We don't extend TileBC here because we have no need of any of its functions.
public class TileSpringOil extends TileEntity implements IDebuggable {

    private final Map<GameProfile, PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();

    /** An approximation of the total number of oil source blocks in the oil spring. The actual number will be less than
     * this, so this is taken as an approximation.
     * <p>
     * Note that this SHOULD NEVER be set! (Except by the generator, and readFromNbt) */
    public int totalSources;

    public void onPumpOil(TilePump pump, BlockPos oilPos) {
        GameProfile profile = pump.getOwner();
        if (profile == null) {
//            BCLog.logger.warn("Unknown owner for pump at " + pump.getPos());
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile, PlayerPumpInfo::new);
        info.lastPumpTick = world.getTotalWorldTime();
        info.sourcesPumped++;

        String name = profile.getName();
//        BCLog.logger.info("Pumped " + info.sourcesPumped + " / " + totalSources + " at " + oilPos + " (for " + System.identityHashCode(this) + ", "+getPos()+")");
        if (info.sourcesPumped >= totalSources * 7 / 8) {
//            BCLog.logger.info("Pumped nearly all oil blocks!");
            if (oilPos.equals(getPos().up())) {
                BCLog.logger.info("Awarding advancement to " + name + "! Or we would do, if this was 1.12...");
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("pumpProgress", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            PlayerPumpInfo info = new PlayerPumpInfo(list.getCompoundTagAt(i));
            pumpProgress.put(info.profile, info);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("totalSources", totalSources);
        NBTTagList list = new NBTTagList();
        for (PlayerPumpInfo info : pumpProgress.values()) {
            list.appendTag(info.writeToNbt());
        }
        nbt.setTag("pumpProgress", list);
        return nbt;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
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
