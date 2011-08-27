package net.minecraft.src.buildcraft.core;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TileNetworkData {

	int staticSize () default -1; 

}
