package buildcraft.core.gui.widgets;

import buildcraft.core.gui.tooltips.ToolTip;


/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IIndicatorController {

    ToolTip getToolTip();

    int getScaledLevel(int size);

}
