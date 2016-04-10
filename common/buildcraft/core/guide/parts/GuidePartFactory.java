package buildcraft.core.guide.parts;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.guide.GuiGuide;

@SideOnly(Side.CLIENT)
public abstract class GuidePartFactory<T extends GuidePart> {
    public abstract T createNew(GuiGuide gui);
}
