package ct.buildcraft.robotics;

import ct.buildcraft.robotics.block.BlockRequester;
import ct.buildcraft.robotics.block.BlockZonePlanner;
import ct.buildcraft.robotics.tile.TileRequester;
import ct.buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BCRoboticsBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCRobotics.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BCRobotics.MODID);

    public static final RegistryObject<BlockZonePlanner> ZONE_PLANNER = BLOCKS.register("zone_planner", BlockZonePlanner::new);
    public static final RegistryObject<BlockRequester> REQUESTER = BLOCKS.register("requester", BlockRequester::new);

    public static final RegistryObject<BlockEntityType<TileZonePlanner>> ZONE_PLANNER_TILE = BLOCK_ENTITYS.register(
            "entity_zone_planner",
            () -> BlockEntityType.Builder.of(TileZonePlanner::new, ZONE_PLANNER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<TileRequester>> REQUESTER_TILE = BLOCK_ENTITYS.register(
            "entity_requester",
            () -> BlockEntityType.Builder.of(TileRequester::new, REQUESTER.get()).build(null)
    );

    private BCRoboticsBlocks() {
    }

    public static void registry(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITYS.register(bus);
    }
}
