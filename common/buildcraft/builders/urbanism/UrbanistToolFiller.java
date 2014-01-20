package buildcraft.builders.urbanism;

import java.util.ArrayList;
import java.util.LinkedList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.core.Box;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

class UrbanistToolFiller extends UrbanistToolArea {

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/urbanist_tool_filler.png");
	private static final int GUI_TEXTURE_WIDTH = 64;
	private static final int GUI_TEXTURE_HEIGHT = 210;

	LinkedList<FillerSlot> fillerSlots = new LinkedList<FillerSlot>();

	ArrayList<IFillerPattern> patterns = new ArrayList<IFillerPattern>();

	int selection = -1;

	class FillerSlot extends AdvancedSlot {
		public int index;
		public boolean isSelected = false;

		public FillerSlot(GuiAdvancedInterface gui, int index) {
			super(gui, -100, -100);

			this.index = index;
		}

		public ResourceLocation getTexture() {
			return TextureMap.locationBlocksTexture;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public Icon getIcon() {
			if (index < patterns.size()) {
				return patterns.get(index).getIcon();
			} else {
				return null;
			}
		}

		@Override
		public String getDescription() {
			return patterns.get(index).getDisplayName();
		}

		@Override
		public void selected () {
			for (FillerSlot s : fillerSlots) {
				s.isSelected = false;
			}

			isSelected = true;
		}
	}

	public UrbanistToolFiller () {
		for (FillerPattern pattern : FillerPattern.patterns.values()) {
			patterns.add(pattern);
		}
	}

	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Filler);
	}

	@Override
	public String getDescription() {
		return "Build from Filler Pattern";
	}

	@Override
	public void drawGuiContainerBackgroundLayer(GuiUrbanist gui, float f, int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
		gui.drawTexturedModalRect(0, 0, 0, 0, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
	}

	@Override
	public void drawSelection (GuiUrbanist gui, float f, int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);

		for (int i = 0; i < fillerSlots.size(); ++i) {
			if (fillerSlots.get(i).isSelected) {
				gui.drawTexturedModalRect(4, 42 + 18 * i, 64, 0, 18, 18);
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(GuiUrbanist gui, int par1, int par2) {
		gui.getFontRenderer ().drawString("Filler", 4, 4, 0x404040);
	}

	@Override
	public boolean onInterface (int mouseX, int mouseY) {
		if (mouseX < GUI_TEXTURE_WIDTH && mouseY < GUI_TEXTURE_HEIGHT) {
			return true;
		}

		return false;
	}

	@Override
	public void createSlots (GuiUrbanist gui, LinkedList <AdvancedSlot> slots) {
		for (int i = 0; i < 8; ++i) {
			FillerSlot slot = new FillerSlot(gui, i);
			fillerSlots.add(slot);
			slots.add(slot);
		}
	}

	@Override
	public void show () {
		for (int i = 0; i < 8; ++i) {
			fillerSlots.get(i).x = 4;
			fillerSlots.get(i).y = 42 + 18 * i;
		}
	}

	@Override
	public void hide () {
		for (int i = 0; i < 8; ++i) {
			fillerSlots.get(i).x = -100;
		}
	}

	@Override
	public void areaSet (int x1, int y1, int z1, int x2, int y2, int z2) {
		Box box = new Box();
		box.initialize(x1, y1, z1, x2, y2, z2);


	}
}