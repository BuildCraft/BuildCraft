package buildcraft.robotics.render;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

import buildcraft.core.lib.render.BuildCraftBakedModel;

public class RobotItemModel extends BuildCraftBakedModel implements ISmartItemModel {
    protected RobotItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
    }

    @Override
    public RobotItemModel handleItemState(ItemStack stack) {
        return handle(stack);
    }

    public static RobotItemModel handle(ItemStack stack) {
        return new RobotItemModel(ImmutableList.<BakedQuad> of(), null, DefaultVertexFormats.BLOCK);
    }

    public static RobotItemModel create() {
        return new RobotItemModel(null, null, null);
    }
}
