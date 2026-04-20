package ct.buildcraft.builders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.builders.BCBuildersItems;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.ClientSnapshots;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ScreenUtils;

public class BlueprintTooltipComponent implements ClientTooltipComponent {

	final ItemStack blueprint;
	
	public BlueprintTooltipComponent(BlueprintTooltip tooltip){
		this.blueprint = tooltip.getBlueprint();
	}

	@Override
	public int getHeight() {
		return 100;
	}

	@Override
	public int getWidth(Font p_169952_) {
		return 100;
	}

	@Override
	public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer render,
			int z) {
        Snapshot snapshot = null;
        Header header = BCBuildersItems.BLUEPRINT.get() != null &&  BCBuildersItems.TEMPLATE.get() != null? ItemSnapshot.getHeader(blueprint) : null;
        if (header != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        } else if (BCBuildersItems.SCHEMATIC_SINGLE.get() != null) {
            ISchematicBlock schematicBlock = ItemSchematicSingle.getSchematicSafe(blueprint);
            if (schematicBlock != null) {
                Blueprint blueprint = new Blueprint();
                blueprint.size = new BlockPos(1, 1, 1);
                blueprint.offset = BlockPos.ZERO;
                blueprint.data = new int[] { 0 };
                blueprint.palette.add(schematicBlock);
                blueprint.computeKey();
                snapshot = blueprint;
            }
        }

        if (snapshot != null) {
        	poseStack.pushPose();
            int pX = x;
            int pY = y  + 10;
            int sX = 100;
            int sY = 100;

            Matrix4f pose = poseStack.last().pose();
            ScreenUtils.drawGradientRect(pose, z, x, y, pX, pY, sX, sY);
            // Copy from GuiUtils#drawHoveringText
            int backgroundColor = 0xF0100010;
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY - 4, pX + sX + 3, pY - 3, backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY + sY + 3, pX + sX + 3, pY + sY + 4, backgroundColor,
                backgroundColor);
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY - 3, pX + sX + 3, pY + sY + 3, backgroundColor,
                backgroundColor);
            ScreenUtils.drawGradientRect(pose, z, pX - 4, pY - 3, pX - 3, pY + sY + 3, backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(pose, z, pX + sX + 3, pY - 3, pX + sX + 4, pY + sY + 3, backgroundColor,
                backgroundColor);
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY - 3 + 1, pX - 3 + 1, pY + sY + 3 - 1, borderColorStart,
                borderColorEnd);
            ScreenUtils.drawGradientRect(pose, z, pX + sX + 2, pY - 3 + 1, pX + sX + 3, pY + sY + 3 - 1, borderColorStart,
                borderColorEnd);
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY - 3, pX + sX + 3, pY - 3 + 1, borderColorStart,
                borderColorStart);
            ScreenUtils.drawGradientRect(pose, z, pX - 3, pY + sY + 2, pX + sX + 3, pY + sY + 3, borderColorEnd,
                borderColorEnd);

            ClientSnapshots.INSTANCE.renderSnapshot(poseStack, snapshot, pX, pY, sX, sY);
            poseStack.popPose();
        }
	}
	

}
