package buildcraft.builders.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.json.SpriteDelegate;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.core.builders.patterns.PatternNone;

public class GuiFiller2 extends GuiJson<ContainerFiller> {

    public static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();

    private final List<FillerWrapper> possible = new LinkedList<>();

    public GuiFiller2(ContainerFiller container) {
        super(container, LOCATION);
    }

    @Override
    protected void preLoad() {
        sprites.put("filler.pattern.sprite", SPRITE_PATTERN);

        IFillerPattern patternNone = null;
        for (IFillerPattern pattern : FillerManager.registry.getPatterns()) {
            if (pattern instanceof PatternNone && patternNone == null) {
                patternNone = pattern;
                continue;
            }
            possible.add(new FillerWrapper(pattern));
        }
        Collections.sort(possible);
        if (patternNone != null) {
            possible.add(0, new FillerWrapper(patternNone));
        }
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
        SPRITE_PATTERN.delegate = container.tile.pattern.getGuiSprite();
    }
}
