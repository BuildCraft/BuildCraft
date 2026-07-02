/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.boards;

import java.util.Collection;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class RedstoneBoardRegistry {
    public static RedstoneBoardRegistry instance;

    /** Register a redstone board type. The energy cost is measured in BuildCraft microjoules. */
    public abstract void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, int energyCost);

    /** Deprecated 1.7 compatibility entry point. Prefer {@link #registerBoardType(RedstoneBoardNBT, int)}. */
    @Deprecated
    public abstract void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability);

    public abstract void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT);

    public abstract RedstoneBoardRobotNBT getEmptyRobotBoard();

    public abstract RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag nbt);

    public abstract RedstoneBoardNBT<?> getRedstoneBoard(String id);

    @OnlyIn(Dist.CLIENT)
    public abstract void registerSprites(Consumer<ResourceLocation> spriteRegistrar);

    /** Compatibility hook for code that still calls the old method name while being ported. */
    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public void registerIcons(Object iconRegister) {
        registerSprites(location -> {});
    }

    public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();

    public abstract int getEnergyCost(RedstoneBoardNBT<?> board);
}
