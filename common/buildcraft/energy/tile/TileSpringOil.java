package buildcraft.energy.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.tile.ITileOilSpring;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.lib.misc.AdvancementUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// We don't extend TileBC here because we have no need of any of its functions.
public class TileSpringOil extends TileEntity implements IDebuggable, ITileOilSpring {

    private static final ResourceLocation ADVANCEMENT_PUMP_LARGE_OIL_WELL = new ResourceLocation(
            "buildcraftfactory:black_gold"
    );

    private final Map<GameProfile, PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();

    /** An approximation of the total number of oil source blocks in the oil spring. The actual number will be less than
     * this, so this is taken as an approximation.
     * <p>
     * Note that this SHOULD NEVER be set! (Except by the generator, and readFromNbt) */
    public int totalSources;

    public TileSpringOil() {
        super(BCEnergyBlocks.springTile.get());
    }

    @Override
    public void onPumpOil(GameProfile profile, BlockPos oilPos) {
        if (profile == null) {
            // BCLog.logger.warn("Unknown owner for pump at " + pump.getPos());
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile, PlayerPumpInfo::new);
        info.lastPumpTick = level.getGameTime();
        info.sourcesPumped++;

        // BCLog.logger.info("Pumped " + info.sourcesPumped + " / " + totalSources + " at " + oilPos + " (for " +
        // System.identityHashCode(this) + ", "+getPos()+")");
        if (info.sourcesPumped >= totalSources * 7 / 8) {
            // BCLog.logger.info("Pumped nearly all oil blocks!");
            if (oilPos.equals(getBlockPos().above())) {
                AdvancementUtil.unlockAdvancement(profile.getId(), ADVANCEMENT_PUMP_LARGE_OIL_WELL);
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        ListNBT list = nbt.getList("pumpProgress", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            PlayerPumpInfo info = new PlayerPumpInfo(list.getCompound(i));
            pumpProgress.put(info.profile, info);
        }
    }

    @Override
//    public CompoundNBT writeToNBT(CompoundNBT nbt) {
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putInt("totalSources", totalSources);
        ListNBT list = new ListNBT();
        for (PlayerPumpInfo info : pumpProgress.values()) {
            list.add(info.writeToNbt());
        }
        nbt.put("pumpProgress", list);
        return nbt;
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("totalSources = " + totalSources);
        left.add(new StringTextComponent("totalSources = " + totalSources));
        boolean added = false;
        for (PlayerPumpInfo info : pumpProgress.values()) {
            if (!added) {
//                left.add("PlayerEntity Progress:");
                left.add(new StringTextComponent("PlayerEntity Progress:"));
                added = true;
            }
//            left.add("  " + info.profile.getName() + " = " + info.sourcesPumped + " ( " + (level.getGameTime() - info.lastPumpTick) / 20 + "s )");
            left.add(new StringTextComponent("  " + info.profile.getName() + " = " + info.sourcesPumped + " ( " + (level.getGameTime() - info.lastPumpTick) / 20 + "s )"));
        }
    }

    static class PlayerPumpInfo {
        final GameProfile profile;
        long lastPumpTick = -1;
        int sourcesPumped = 0;

        public PlayerPumpInfo(GameProfile profile) {
            this.profile = profile;
        }

        public PlayerPumpInfo(CompoundNBT nbt) {
            profile = NBTUtil.readGameProfile(nbt.getCompound("profile"));
            lastPumpTick = nbt.getLong("lastPumpTick");
            sourcesPumped = nbt.getInt("sourcesPumped");
        }

        public CompoundNBT writeToNbt() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("profile", NBTUtil.writeGameProfile(new CompoundNBT(), profile));
            nbt.putLong("lastPumpTick", lastPumpTick);
            nbt.putInt("sourcesPumped", sourcesPumped);
            return nbt;
        }
    }
}
