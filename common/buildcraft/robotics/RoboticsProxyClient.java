/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BCLog;
import buildcraft.robotics.render.RedstoneBoardMeshDefinition;
import buildcraft.robotics.render.RenderRobot;
import buildcraft.robotics.render.RenderZonePlan;
import buildcraft.robotics.render.RobotItemModel;
import buildcraft.robotics.render.RobotStationModel;

public class RoboticsProxyClient extends RoboticsProxy {
    public static Map<String, IBakedModel> robotModel = new HashMap<>();
    public static IBakedModel defaultRobotModel;

    @SubscribeEvent
    public void onPostBake(ModelBakeEvent event) {
        event.modelRegistry.putObject(new ModelResourceLocation("buildcraftrobotics:robot", "inventory"), RobotItemModel.create());
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.map.registerSprite(EntityRobot.ROBOT_BASE);
        for (RedstoneBoardNBT<?> board : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            if (board instanceof RedstoneBoardRobotNBT) {
                RedstoneBoardRobotNBT robotBoard = (RedstoneBoardRobotNBT) board;
                ResourceLocation texture = robotBoard.getRobotTexture();
                event.map.registerSprite(texture);
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Post event) {
        try {
            robotModel.clear();
            defaultRobotModel = null;

            IModel robotModelBase = ModelLoaderRegistry.getModel(new ResourceLocation("buildcraftrobotics:robot"));
            if (robotModelBase instanceof IRetexturableModel) {
                defaultRobotModel = ((IRetexturableModel) robotModelBase).retexture(ImmutableMap.of("all", EntityRobot.ROBOT_BASE.toString()))
                        .bake(robotModelBase.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

                for (RedstoneBoardNBT<?> board : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
                    if (board instanceof RedstoneBoardRobotNBT) {
                        RedstoneBoardRobotNBT robotBoard = (RedstoneBoardRobotNBT) board;
                        ResourceLocation texture = robotBoard.getRobotTexture();
                        robotModel.put(board.getID(),
                                ((IRetexturableModel) robotModelBase).retexture(ImmutableMap.of("all", texture.toString()))
                                        .bake(robotModelBase.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())
                        );
                    }
                }
            } else {
                BCLog.logger.error("Robot model is not an instance of IRetexturableModel! This is not good news!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preInit() {
        ModelLoader.setCustomModelResourceLocation(BuildCraftRobotics.robotItem, 0, new ModelResourceLocation("buildcraftrobotics:robot", "inventory"));
        RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new IRenderFactory<EntityRobot>() {
            @Override
            public Render<? super EntityRobot> createRenderFor(RenderManager manager) {
                return new RenderRobot(manager);
            }
        });
    }

    @Override
    public void init() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(BuildCraftRobotics.redstoneBoard, new RedstoneBoardMeshDefinition());
        ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlan.class, new RenderZonePlan());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(RobotStationModel.INSTANCE);
    }
}
