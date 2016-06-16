package buildcraft.api;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

/** Stores all of BuildCraft's blocks, from all of its modules. If any of them have been disabled by the user (or it the
 * module is not installed) then they will be null. This is the equivalent of {@link Blocks} */
public class BCBlocks {
    private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.blocks");

    // BC Core
    public static final Block coreDecorated;
    public static final Block coreEngineRedstone;
    public static final Block CORE_MARKER_VOLUME;
    public static final Block CORE_MARKER_PATH;

    // BC Builders

    // BC Energy
    public static final Block energyEngineStirling;
    public static final Block energyEngineCombustion;
    public static final Block energyEngineCreative;

    // BC Factory

    // BC Robotics

    // BC Silicon

    // BC Transport

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("Accessed BC blocks too early! You can only use them from init onwards!");
        }
        String core = "core";
        coreDecorated = getRegisteredBlock(core, "decorated");
        coreEngineRedstone = getRegisteredBlock(core, "engine_redstone");
        CORE_MARKER_VOLUME = getRegisteredBlock(core, "marker_volume");
        CORE_MARKER_PATH = getRegisteredBlock(core, "marker_path");

        String energy = "energy";
        energyEngineStirling = getRegisteredBlock(energy, "engine_stirling");
        energyEngineCombustion = getRegisteredBlock(energy, "engine_combustion");
        energyEngineCreative = getRegisteredBlock(energy, "engine_creative");
    }

    private static Block getRegisteredBlock(String module, String regName) {
        String modid = "buildcraft" + module;
        Block block = Block.REGISTRY.getObject(new ResourceLocation(modid, regName));

        if (block != Blocks.AIR) {
            if (DEBUG) {
                BCLog.logger.info("[api.blocks] Found the block " + regName + " from the module " + module);
            }
            return block;
        }
        if (DEBUG) {
            if (Loader.isModLoaded(modid)) {
                BCLog.logger.info("[api.blocks] Did not find the block " + regName + " dispite the appropriate mod being loaded (" + modid + ")");
            } else {
                BCLog.logger.info("[api.blocks] Did not find the block " + regName + " probably because the mod is not loaded (" + modid + ")");
            }
        }
        return null;
    }
}
