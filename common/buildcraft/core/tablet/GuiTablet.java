package buildcraft.core.tablet;

import java.util.Date;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.tablet.TabletBitmap;
import buildcraft.core.lib.render.DynamicTextureBC;
import buildcraft.core.tablet.manager.TabletManagerClient;
import buildcraft.core.tablet.manager.TabletThread;

public class GuiTablet extends GuiScreen {
	private static final boolean ENABLE_HIGHLIGHT = false;

	private static final int[] PALETTE = new int[]{
			0x00000000, 0x1c000000, 0x30000000, 0x48000000,
			0x60000000, 0x78000000, 0x9a000000, 0xbc000000
	};
	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftcore", "textures/gui/tablet.png");
	private static final int X_SIZE = 142;
	private static final int Y_SIZE = 180;
	private final DynamicTextureBC display;
	private final TabletThread tabletThread;
	private final TabletClient tablet;
	private int guiLeft, guiTop;
	private long lastDate;
	private float glScale = 1.0f;
	private int buttonState = 1;

	public GuiTablet(EntityPlayer player) {
		super();

		this.tabletThread = TabletManagerClient.INSTANCE.get();
		this.tablet = (TabletClient) tabletThread.getTablet();
		this.lastDate = (new Date()).getTime();
		this.display = new DynamicTextureBC(tablet.getScreenWidth(), tablet.getScreenHeight());

		tablet.updateGui(0.0F, this, true);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();
		// recalculate width/height
		int oldScale = mc.gameSettings.guiScale;
		ScaledResolution realRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		mc.gameSettings.guiScale = realRes.getScaleFactor() == 1 ? 2 : (realRes.getScaleFactor() & (~1));
		ScaledResolution currentRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		mc.gameSettings.guiScale = oldScale;

		glScale = (float) (currentRes.getScaledWidth_double() / realRes.getScaledWidth_double());

		this.guiLeft = (currentRes.getScaledWidth() - X_SIZE) / 2;
		this.guiTop = (currentRes.getScaledHeight() - Y_SIZE) / 2;
	}

	public void bindTexture(ResourceLocation texture) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	}

	public void copyDisplay(TabletBitmap display) {
		for (int j = 0; j < display.height; j++) {
			for (int i = 0; i < display.width; i++) {
				this.display.setColor(i, j, PALETTE[display.get(i, j) & 7]);
			}
		}
	}

	@Override
	public void updateScreen() {
		long date = (new Date()).getTime();
		float time = (float) (date - lastDate) / 1000.0F;
		tabletThread.tick(time);
		lastDate = date;
		tablet.updateGui(time, this, false);
	}

	private boolean isButton(int mx, int my) {
		return mx >= (guiLeft + 65) && my >= (guiTop + 167) && mx < (guiLeft + 65 + 18) && my < (guiTop + 167 + 8);
	}

	@Override
	public void handleMouseInput() {
		int x = (int) (Mouse.getEventX() * this.width / this.mc.displayWidth * glScale);
		int y = (int) ((this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1) * glScale);
		int k = Mouse.getEventButton();

		if (k == 0) {
			if (Mouse.getEventButtonState()) {
				if (isButton(x, y)) {
					buttonState = 2;
				}
			} else if (buttonState == 2) {
				if (isButton(x, y)) {
					buttonState = ENABLE_HIGHLIGHT ? 0 : 1;
				} else {
					buttonState = 1;
				}
			}
		} else if (ENABLE_HIGHLIGHT && k == -1 && buttonState != 2) {
			if (isButton(x, y)) {
				buttonState = 0;
			} else {
				buttonState = 1;
			}
		}
	}

	@Override
	public void drawScreen(int fmx, int fmy, float p) {
		this.drawDefaultBackground();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPushMatrix();
		GL11.glScalef(1.0F / glScale, 1.0F / glScale, 1.0F / glScale);

		bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, X_SIZE, Y_SIZE);
		drawTexturedModalRect(guiLeft + 65, guiTop + 167, 142, 147 + (buttonState * 10), 18, 8);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

		GL11.glScalef(0.5F, 0.5F, 0.5F);
		display.draw((guiLeft + 10 + 1) * 2, (guiTop + 8 + 1) * 2, zLevel);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopAttrib();

		GL11.glPopMatrix();
	}
}
