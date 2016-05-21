package buildcraft.lib.client.guide.parts;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.guide.GuiGuide;

@SideOnly(Side.CLIENT)
public interface GuidePartFactory<T extends GuidePart> {
    T createNew(GuiGuide gui);
}
