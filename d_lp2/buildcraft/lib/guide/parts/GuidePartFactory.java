package buildcraft.lib.guide.parts;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.guide.GuiGuide;

@SideOnly(Side.CLIENT)
public interface GuidePartFactory<T extends GuidePart> {
    T createNew(GuiGuide gui);
}
