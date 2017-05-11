package buildcraft.builders;

import buildcraft.builders.snapshot.ClientSnapshots;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum BCBuildersEventDist {
    INSTANCE;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostText(RenderTooltipEvent.PostText event) {
        if (BCBuildersItems.snapshot.getHeader(event.getStack()) != null) {
            int pX = event.getX();
            int pY = event.getY() + event.getHeight() + 10;
            int sX = 100;
            int sY = 100;

            // Copy from GuiUtils#drawHoveringText
            int zLevel = 300;
            int backgroundColor = 0xF0100010;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 4, pX + sX + 3, pY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 3, pX + sX + 3, pY + sY + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 4, pY - 3, pX - 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 3, pY - 3, pX + sX + 4, pY + sY + 3, backgroundColor, backgroundColor);
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3 + 1, pX - 3 + 1, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 2, pY - 3 + 1, pX + sX + 3, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 2, pX + sX + 3, pY + sY + 3, borderColorEnd, borderColorEnd);

            ClientSnapshots.INSTANCE.renderSnapshot(
                    BCBuildersItems.snapshot.getHeader(event.getStack()),
                    pX,
                    pY,
                    sX,
                    sY
            );
        }
    }
}
