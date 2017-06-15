package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.json.SpriteDelegate;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.patterns.FillerStatementContext;

public class GuiFiller2 extends GuiJson<ContainerFiller> {

    public static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();

    public GuiFiller2(ContainerFiller container) {
        super(container, LOCATION);
    }

    @Override
    protected void preLoad() {
        miscProperties.put("filler.pattern.sprite", SPRITE_PATTERN);
        miscProperties.put("filler.possible", FillerStatementContext.CONTEXT_ALL);
        miscProperties.put("filler.pattern", container.tile.pattern);
    }

    @Override
    protected void postLoad() {
        setupButton("filler.no_excavate", b -> {
            b.setBehaviour(IButtonBehaviour.TOGGLE);
            final ToolTip active = ToolTip.createLocalized("tip.filler.excavate.off");
            final ToolTip inactive = ToolTip.createLocalized("tip.filler.excavate.on");
            b.setActive(!container.tile.canExcavate());
            IButtonClickEventListener listener = (b2, k) -> {
                b.setToolTip(b.active ? active : inactive);
                container.tile.sendCanExcavate(!b.active);
            };
            listener.handleButtonClick(b, 0);
            b.registerListener(listener);
        });
        setupButton("filler.invert", b -> {
            b.setBehaviour(IButtonBehaviour.TOGGLE);
            final ToolTip on = ToolTip.createLocalized("tip.filler.invert.on");
            final ToolTip off = ToolTip.createLocalized("tip.filler.invert.off");
            IButtonClickEventListener listener = (b2, k) -> {
                b.setToolTip(b.active ? on : off);
            };
            listener.handleButtonClick(b, 0);
            b.registerListener(listener);
        });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        SPRITE_PATTERN.delegate = container.tile.pattern.get().getSprite();
    }
}
