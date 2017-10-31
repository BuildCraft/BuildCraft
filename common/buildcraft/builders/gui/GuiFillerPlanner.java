package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.json.SpriteDelegate;

import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.builders.filler.FillerStatementContext;

public class GuiFillerPlanner extends GuiJson<ContainerFillerPlanner> {
    private static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filling_planner.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();

    public GuiFillerPlanner(ContainerFillerPlanner container) {
        super(container, LOCATION);
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        properties.put("filler.possible", FillerStatementContext.CONTEXT_ALL);
        properties.put("filler.pattern", container.getPatternStatementClient());
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);

        context.put_b("filler.invert", () -> container.addon.inverted);
        properties.put("filler.invert", IButtonBehaviour.TOGGLE);
        properties.put("filler.invert", container.addon.inverted);
        properties.put("filler.invert", (IButtonClickEventListener) (b, k) ->
            container.sendInverted(b.isButtonActive())
        );
    }

    @Override
    protected void postLoad() {
        super.postLoad();
/*        setupButton("filler.invert", b -> {
            b.setBehaviour(IButtonBehaviour.TOGGLE);
            final ToolTip on = ToolTip.createLocalized("tip.filler.invert.on");
            final ToolTip off = ToolTip.createLocalized("tip.filler.invert.off");
            b.setActive(container.addon.inverted);
            IButtonClickEventListener listener = (b2, k) -> {
                b.setToolTip(b.active ? on : off);
                container.setInverted(b.active);
            };
            listener.handleButtonClick(b, 0);
            b.registerListener(listener);
        });*/
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        SPRITE_PATTERN.delegate = container.getPatternStatementClient().get().getSprite();
    }
}
