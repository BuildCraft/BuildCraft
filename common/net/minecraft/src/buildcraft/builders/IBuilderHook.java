package net.minecraft.src.buildcraft.builders;

import java.io.IOException;

import net.minecraft.src.buildcraft.core.BptRootIndex;

public interface IBuilderHook {
	
	public void rootIndexInitialized (BptRootIndex rootBptIndex) throws IOException;

}
