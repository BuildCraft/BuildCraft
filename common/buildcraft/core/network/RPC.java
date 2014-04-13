package buildcraft.core.network;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cpw.mods.fml.relauncher.Side;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RPC {
	//FIXME: Take into account side constraints when making calls to check
	// they're correclyt made
	public RPCSide value() default RPCSide.BOTH;
}