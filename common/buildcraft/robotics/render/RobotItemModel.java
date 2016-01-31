package buildcraft.robotics.render;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.model.ISmartItemModel;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.robotics.ItemRobot;

public class RobotItemModel extends BuildCraftBakedModel implements ISmartItemModel {
    protected RobotItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
    }

    @Override
    public RobotItemModel handleItemState(ItemStack stack) {
        return handle(stack);
    }

    public static RobotItemModel handle(ItemStack stack) {
        /*if (stack == null)*/ return new RobotItemModel(ImmutableList.<BakedQuad> of(), null, DefaultVertexFormats.BLOCK);
//        RedstoneBoardRobotNBT board = ItemRobot.getRobotNBT(stack);
//        board.getRobotTexture();
    }

    public static RobotItemModel create() {
        return new RobotItemModel(null, null, null);
    }
}
