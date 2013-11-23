package buildcraft.core.gui.tooltips;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IToolTipProvider {

	ToolTip getToolTip();

	boolean isToolTipVisible();

	boolean isMouseOver(int mouseX, int mouseY);
}
