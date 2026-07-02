package ct.buildcraft.robotics;

import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BCRoboticsEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BCRobotics.MODID);

    public static final RegistryObject<EntityType<EntityRobot>> ROBOT = ENTITIES.register("robot", () ->
            EntityType.Builder.<EntityRobot>of(EntityRobot::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(BCRobotics.MODID + ":robot"));

    private BCRoboticsEntities() {
    }

    public static void registry(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
