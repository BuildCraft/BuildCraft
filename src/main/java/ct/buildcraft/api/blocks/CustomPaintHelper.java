package ct.buildcraft.api.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/** Provides a simple way to paint a single block, iterating through all {@link ICustomPaintHandler}'s that are
 * registered for the block. */
public enum CustomPaintHelper {
    INSTANCE;

    /* If you want to test your class-based rotation registration then add the system property
     * "-Dbuildcraft.api.rotation.debug.class=true" to your launch. */
    private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.painting");
    
    protected static final Map<Class<? extends Block>,String> RECOLOR_MAP = new HashMap<>(); 

    private final Map<Class<? extends Block>, List<ICustomPaintHandler>> handlers = Maps.newIdentityHashMap();
    private final List<ICustomPaintHandler> allHandlers = Lists.newArrayList();
    
    static {
    	RECOLOR_MAP.put(GlazedTerracottaBlock.class, "_glazed_terracotta");
		RECOLOR_MAP.put(CandleBlock.class, "_candle");
		RECOLOR_MAP.put(CandleCakeBlock.class, "_candle_cake");
		RECOLOR_MAP.put(StainedGlassBlock.class, "_stained_glass");
		RECOLOR_MAP.put(StainedGlassPaneBlock.class, "_stained_glass_pane");
		RECOLOR_MAP.put(WoolCarpetBlock.class, "_carpet");
		RECOLOR_MAP.put(BannerBlock.class, "_banner");
		RECOLOR_MAP.put(WallBannerBlock.class, "_wall_banner");
		RECOLOR_MAP.put(ShulkerBoxBlock.class, "_shulker_box");
		RECOLOR_MAP.put(ConcretePowderBlock.class, "_concrete_powder");
    }

    /** Registers a handler that will be called LAST for ALL blocks, if all other paint handlers have returned PASS or
     * none are registered for that block. */
    public void registerHandlerForAll(ICustomPaintHandler handler) {
        if (DEBUG) {
            BCLog.logger.info("[api.painting] Adding a paint handler for ALL blocks (" + handler.getClass() + ")");
        }
        allHandlers.add(handler);
    }


    public void registerHandler(Class<? extends Block> block, ICustomPaintHandler handler) {
        if (registerHandlerInternal(block, handler)) {
            if (DEBUG) {
                BCLog.logger.info("[api.painting] Setting a paint handler for block " + block.getClass().getSimpleName() + "(" + handler.getClass() + ")");
            }
        } else if (DEBUG) {
            BCLog.logger.info("[api.painting] Adding another paint handler for block " + block.getClass().getSimpleName() + "(" + handler.getClass() + ")");
        }
    }

    private boolean registerHandlerInternal(Class<? extends Block> block, ICustomPaintHandler handler) {
        if (!handlers.containsKey(block)) {
            List<ICustomPaintHandler> forBlock = Lists.newArrayList();
            forBlock.add(handler);
            handlers.put(block, forBlock);
            return true;
        } else {
            handlers.get(block).add(handler);
            return false;
        }
    }

    /** Attempts to paint a block at the given position. Basically iterates through all registered paint handlers. */
    public InteractionResult attemptPaintBlock(Level world, BlockPos pos, BlockState state, Vec3 hitPos, @Nullable Direction hitSide, @Nullable DyeColor paint) {
        Block block = state.getBlock();
        Class<? extends Block> Cblock = block.getClass();
        if (block instanceof ICustomPaintHandler) {
            return ((ICustomPaintHandler) block).attemptPaint(world, pos, state, hitPos, hitSide, paint);
        }
        List<ICustomPaintHandler> custom = handlers.get(Cblock);
        if (custom == null || custom.isEmpty()) {
            return defaultAttemptPaint(world, pos, state, hitPos, hitSide, paint);
        }
        for (ICustomPaintHandler handler : custom) {
            InteractionResult result = handler.attemptPaint(world, pos, state, hitPos, hitSide, paint);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return defaultAttemptPaint(world, pos, state, hitPos, hitSide, paint);
    }

    private InteractionResult defaultAttemptPaint(Level world, BlockPos pos, BlockState state, Vec3 hitPos, Direction hitSide, @Nullable DyeColor paint) {
        for (ICustomPaintHandler handler : allHandlers) {
            InteractionResult result = handler.attemptPaint(world, pos, state, hitPos, hitSide, paint);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (paint == null) {
            return InteractionResult.FAIL;
        }
        if (recolorBlock(world, pos, paint)) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
    /**
     * registry your block that can be recolored
     * @param block the Block class
     * @param name the registry name 
     */
    public static void registryRecolor(Class<? extends Block> block, String name) {
    	RECOLOR_MAP.put(block, name);
    }
    
    /**
     * Tool to recolor vanilla block, except for woof and concrete
     * @warning if the DyeColor of vanilla block changed, bugs may occur!
     * @param world
     * @param pos
     * @param paint
     * @return If the recoloring was successful
     */
    public static boolean recolorBlock(Level world, BlockPos pos, DyeColor paint) {
    	BlockState state = world.getBlockState(pos);
    	Block block = state.getBlock();
    	Class<? extends Block> Cblock = block.getClass();
    	String type = "";
    	if(RECOLOR_MAP.containsKey(Cblock))
    		type = RECOLOR_MAP.get(Cblock);
    	else return false;
    	Block newblock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(paint.getName() + type));
    	if(newblock == null) return false;
    	BlockState[] newState = {newblock.defaultBlockState()};
    	state.getValues().keySet().forEach(k -> newState[0] = newState[0].setValue((Property)k, state.getValue(k)));
		world.setBlockAndUpdate(pos, newState[0]);
    	return true;
    	
    		
    		
    }
}
