package buildcraft.core.gui.widgets;

import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.gui.tooltips.ToolTipLine;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class IndicatorController implements IIndicatorController {

	private final ToolTip tips = new ToolTip() {
		@Override
		public void refresh() {
			refreshToolTip();
		}
	};
	protected ToolTipLine tip = new ToolTipLine();

	public IndicatorController() {
		tips.add(tip);
	}

	protected void refreshToolTip() {
	}

	@Override
	public final ToolTip getToolTip() {
		return tips;
	}
}
