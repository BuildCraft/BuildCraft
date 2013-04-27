package buildcraft.core.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
@SideOnly(Side.CLIENT)
public class GuiMultiButtonSmall extends GuiMultiButton {

    public GuiMultiButtonSmall(int id, int x, int y, int width, MultiButtonController control) {
        super(id, x, y, width, control);
        height = 15;
        texOffset = 168;
    }
}
