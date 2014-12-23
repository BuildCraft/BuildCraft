package buildcraft.transport.gates;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;
import buildcraft.BuildCraftBuilders;

public class GateItemModel implements ISmartItemModel {
	private ItemStack stack;
	private IBakedModel baseModel;

	private GateItemModel(ItemStack stack) {
		this.stack = stack;
		this.baseModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(BuildCraftBuilders.blueprintItem));
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return new GateItemModel(stack);
	}

	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	@Override
	public List getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return null;
	}
}
