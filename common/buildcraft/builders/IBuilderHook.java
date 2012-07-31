package buildcraft.builders;

import java.io.IOException;

import buildcraft.core.BptRootIndex;


public interface IBuilderHook {

	public void rootIndexInitialized(BptRootIndex rootBptIndex) throws IOException;

}
