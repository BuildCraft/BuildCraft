package buildcraft.robotics;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.plug.PlugBakerSimple;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.robotics.client.model.RoboticsNodeTypes;
import buildcraft.robotics.client.model.key.KeyPlugRobotStation;
import buildcraft.robotics.client.render.PlugRobotStationRenderer;
import buildcraft.robotics.client.render.RenderRobot;
import buildcraft.robotics.client.render.RenderZonePlanner;
import buildcraft.robotics.plug.PluggableRobotStation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

@OnlyIn(Dist.CLIENT)
public class BCRoboticsModels {
    public static final ModelHolderStatic ROBOT_STATION_STATIC;
    public static final ModelHolderVariable ROBOT_STATION_DYNAMIC;

    public static final IPluggableStaticBaker<KeyPlugRobotStation> BAKER_PLUG_ROBOT_STATION;

    static {
        // Calen: ensure ExpressionCompat ENUM_FACING = new NodeType<>("Facing", Direction.UP); run, or will cause IllegalArgumentException: Unknown NodeType class net.minecraft.core.Direction
        ExpressionCompat.setup();
        RoboticsNodeTypes.setup();

        ROBOT_STATION_STATIC = getStaticModel("plugs/robot_station_static");
        ROBOT_STATION_DYNAMIC = getModel("plugs/robot_station_dynamic", PluggableRobotStation.MODEL_FUNC_CTX);

        BAKER_PLUG_ROBOT_STATION = new PlugBakerSimple<>(ROBOT_STATION_STATIC::getCutoutQuads);
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic("buildcraftrobotics:models/" + str + ".json");
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftrobotics:models/" + str + ".json", fnCtx);
    }

    public static void fmlPreInit() {
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCRobotics.MODID).get()).getEventBus();
        modEventBus.register(BCRoboticsModels.class);
    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntityRenderer(BCRoboticsBlocks.zonePlannerTile.get(), RenderZonePlanner::new);

        ClientRegistry.bindTileEntityRenderer(BCRoboticsBlocks.zonePlannerTile.get(), RenderZonePlanner::new);
        EntityRendererManager entityRendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
        BCRoboticsEntities.robotMap.values().forEach(robot -> entityRendererManager.register(robot.get(), new RenderRobot(entityRendererManager)));

        PipeApiClient.IClientRegistry pipeRegistryClient = PipeApiClient.registry;
        if (pipeRegistryClient != null) {
            pipeRegistryClient.registerBaker(KeyPlugRobotStation.class, BAKER_PLUG_ROBOT_STATION);
            pipeRegistryClient.registerRenderer(PluggableRobotStation.class, PlugRobotStationRenderer.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        PluggableRobotStation.setModelVariablesForItem();
        putModel(event, "robot_station#inventory", new ModelPluggableItem(ROBOT_STATION_STATIC.getCutoutQuads(), ROBOT_STATION_DYNAMIC.getCutoutQuads()));

        PlugRobotStationRenderer.onModelBake();
    }

    private static void putModel(ModelBakeEvent event, String str, IBakedModel model) {
        event.getModelRegistry().replace(BCModules.ROBOTICS.createModelLocation(str), model);
    }
}
