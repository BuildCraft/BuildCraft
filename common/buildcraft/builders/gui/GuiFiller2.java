package buildcraft.builders.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

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
    public void updateScreen() {
        super.updateScreen();
        int i = (int) ((System.currentTimeMillis() / 2000) % possible.size());
        IFillerPattern pattern = possible.get(i);
        SPRITE_PATTERN.delegate = pattern.getGuiSprite();
    }
}
