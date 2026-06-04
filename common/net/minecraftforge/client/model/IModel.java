// STUB(R.Chen): Forge IModel — compile shim.
package net.minecraftforge.client.model;

import java.util.Collection;
import net.minecraft.util.Identifier;

public interface IModel {
    Collection<Identifier> getDependencies();
    Collection<Identifier> getTextures();
}
