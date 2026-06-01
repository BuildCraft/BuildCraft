// STUB(R.Chen): Forge IConfigElement — compile shim.
package net.minecraftforge.fml.client.config;

import java.util.List;

public interface IConfigElement {
    String getName();
    String getComment();
    boolean isDefault();
    boolean requiresMcRestart();
    boolean requiresWorldRestart();
    List<IConfigElement> getChildElements();
}
