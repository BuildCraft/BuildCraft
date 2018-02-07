package buildcraft.builders.gui;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.misc.collect.TypedKeyMap;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.json.SpriteDelegate;

import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.builders.filler.FillerStatementContext;

public class GuiFillerPlanner extends GuiBC8<ContainerFillerPlanner> {
    private static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler_planner.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();

    public GuiFillerPlanner(ContainerFillerPlanner container) {
        super(container, LOCATION);
        BuildCraftJsonGui jsonGui = (BuildCraftJsonGui) mainGui;
        preLoad(jsonGui);
        jsonGui.load();
    }

    protected void preLoad(BuildCraftJsonGui json) {
        TypedKeyMap<String, Object> properties = json.properties;
        FunctionContext context = json.context;

        properties.put("filler.possible", FillerStatementContext.CONTEXT_ALL);
        properties.put("filler.pattern", container.getPatternStatementClient());
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);

        context.put_b("filler.invert", () -> container.addon.inverted);
        properties.put("filler.invert", IButtonBehaviour.TOGGLE);
        properties.put("filler.invert", container.addon.inverted);
        properties.put("filler.invert",
                (IButtonClickEventListener) (b, k) -> container.sendInverted(b.isButtonActive())
        );
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        SPRITE_PATTERN.delegate = container.getPatternStatementClient().get().getSprite();
    }
}
