package buildcraft.robotics.render;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.lib.client.model.BuildCraftBakedModel;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RoboticsProxyClient;

public class RobotItemModel extends BuildCraftBakedModel implements ISmartItemModel {
    protected RobotItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
        // FIXME: Add perspective transformation
    }

    @Override
    public RobotItemModel handleItemState(ItemStack stack) {
        return handle(stack);
    }

    public static RobotItemModel handle(ItemStack stack) {
        if (stack != null) {
            RedstoneBoardRobotNBT board = ItemRobot.getRobotNBT(stack);
            IBakedModel model = board != null ? RoboticsProxyClient.robotModel.get(board.getID()) : null;
            if (model == null) {
                model = RoboticsProxyClient.defaultRobotModel;
            }
            if (model != null) {
                return new RobotItemModel(ImmutableList.copyOf(model.getGeneralQuads()), model.getParticleTexture(), DefaultVertexFormats.ITEM);
            }
        }

        return new RobotItemModel(ImmutableList.<BakedQuad> of(), null, DefaultVertexFormats.ITEM);
    }

    public static RobotItemModel create() {
        return new RobotItemModel(null, null, null);
    }
}
