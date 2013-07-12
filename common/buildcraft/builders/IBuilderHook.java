package buildcraft.builders;

import buildcraft.core.blueprints.BptRootIndex;
import java.io.IOException;

public interface IBuilderHook {

	public void rootIndexInitialized(BptRootIndex rootBptIndex) throws IOException;

}
