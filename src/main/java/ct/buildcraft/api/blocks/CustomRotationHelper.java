package ct.buildcraft.api.blocks;

import java.util.Map;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum CustomRotationHelper {
    INSTANCE;

    /* If you want to test your class-based rotation registration then add the system property
     * "-Dbuildcraft.api.rotation.debug.class=true" to your launch. */
    private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.rotation");

    private final Map<Class<? extends Block>,ICustomRotationHandler> handlers = Maps.newIdentityHashMap();


    public void registerHandler(Class<? extends Block> block, ICustomRotationHandler handler) {
        if (registerHandlerInternal(block, handler)) {
            if (DEBUG) {
                BCLog.logger.info("[api.rotation] Setting a rotation handler for block " + block.getClass().getSimpleName());
            }
        } else if (DEBUG) {
            BCLog.logger.info("[api.rotation] Adding another rotation handler for block " + block.getClass().getSimpleName());
        }
    }

    private boolean registerHandlerInternal(Class<? extends Block> block, ICustomRotationHandler handler) {
        if (!handlers.containsKey(block)) {
            handlers.put(block, handler);
            return true;
        }
        return false;
    }

    public InteractionResult attemptRotateBlock(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        Block block = state.getBlock();
        Class<? extends Block> Cblock = block.getClass();
        if (block instanceof ICustomRotationHandler) {
            return ((ICustomRotationHandler) block).attemptRotation(world, pos, state, sideWrenched);
        }
        if (!handlers.containsKey(Cblock)) return InteractionResult.PASS;
        
        InteractionResult result = handlers.get(Cblock).attemptRotation(world, pos, state, sideWrenched);
        if (result != InteractionResult.PASS) {
        	return result;
        }
        return InteractionResult.PASS;
    }
}
