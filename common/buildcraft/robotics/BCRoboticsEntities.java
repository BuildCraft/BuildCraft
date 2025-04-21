package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.robotics.boards.BCBoardNBT;
import buildcraft.robotics.entity.EntityRobot;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;

public class BCRoboticsEntities {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCRobotics.MODID);

    public static final Map<RedstoneBoardNBT<?>, RegistryObject<EntityType<EntityRobot>>> robotMap = Maps.newConcurrentMap();

    public static void preInit() {
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            if (boardNBT instanceof BCBoardNBT) {
                RegistryObject<EntityType<EntityRobot>> robot = HELPER.addEntity("entity.robot", () -> EntityType.Builder.<EntityRobot>of((type, world) -> new EntityRobot(type, world, ((BCBoardNBT) boardNBT)), EntityClassification.MISC).fireImmune().sized(0.5F, 0.5F), ((BCBoardNBT) boardNBT).getRobotId().getPath(), EntityRobot::createAttributes);
                robotMap.put(boardNBT, robot);
            }
        }
    }
}
