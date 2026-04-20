package ct.buildcraft.lib.gui;

import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.json.BuildCraftJsonGui;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.GuiUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Reference implementation for a gui that delegates to a {@link BuildCraftGui} for most of its functionality. */
public class GuiScreenBuildCraft extends Screen {

    public final BuildCraftGui mainGui;

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the entire screen. */
    public GuiScreenBuildCraft(Component title) {
        this(g -> new BuildCraftGui(g), title);
    }

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the given {@link IGuiArea} Call
     * {@link GuiUtil#moveAreaToCentre(IGuiArea)} if you want a centred gui. (Ignoring ledgers, which will display off
     * to the side) */
    public GuiScreenBuildCraft(IGuiArea area, Component title) {
        this(g -> new BuildCraftGui(g, area), title);
    }

    public GuiScreenBuildCraft(Function<GuiScreenBuildCraft, BuildCraftGui> constructor, Component title) {
    	super(title);
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef, Component title) {
    	super(title);
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. Like
     * {@link #GuiScreenBuildCraft(IGuiArea)} this will occupy only the given {@link IGuiArea} */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef, IGuiArea area, Component title) {
        super(title);
		BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, area, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    private final void standardLedgerInit() {
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        mainGui.tick();
    }
    
    

    @Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        mainGui.drawBackgroundLayer(pose, partialTicks, mouseX, mouseY, () -> drawMenuBackground(pose));
        mainGui.drawElementBackgrounds(pose);
        mainGui.drawElementForegrounds(pose, () -> drawMenuBackground(pose));
	}


    private void drawMenuBackground(PoseStack pose) {
        this.renderBackground(pose, 0);
    }
    
    

    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!mainGui.onMouseClicked(mouseX, mouseY, mouseButton)) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		boolean result = false;
        result |= super.mouseReleased(mouseX, mouseY, state);
        mainGui.onMouseReleased(mouseX, mouseY, state);
        return result;
	}

	@Override
	public boolean mouseDragged(double startX, double startY, int clickedMouseButton, double finalX, double finalY) {
		boolean result = super.mouseDragged(startX, startY, clickedMouseButton, finalX, finalY);
        mainGui.onMouseDragged(startX, startY, clickedMouseButton, finalX, finalY);
        return result;
	}


    @Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_96554_) {
        if (!mainGui.onKeyTyped(p_96554_, InputConstants.getKey(p_97765_, p_97766_))) {
            return super.keyPressed(p_97765_, p_97766_, p_96554_);
        }
        return true;
	}
}
