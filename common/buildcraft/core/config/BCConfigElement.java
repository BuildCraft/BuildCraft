package buildcraft.core.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;

public class BCConfigElement<T> extends ConfigElement<T> {
	private ConfigCategory cat;
	private boolean isProp;

	public BCConfigElement(ConfigCategory ctgy) {
		super(ctgy);
		cat = ctgy;
		isProp = false;
	}

	public BCConfigElement(Property prop) {
		super(prop);
		isProp = true;
	}

	@Override
	public List<IConfigElement> getChildElements() {
		if (!isProp) {
			List<IConfigElement> elements = new ArrayList<IConfigElement>();
			Iterator<ConfigCategory> ccI = cat.getChildren().iterator();
			Iterator<Property> pI = cat.getOrderedValues().iterator();

			while (ccI.hasNext()) {
				ConfigCategory child = ccI.next();
				if (!child.parent.getQualifiedName().equals(cat.getQualifiedName())) {
					continue;
				}

				ConfigElement<?> temp = new BCConfigElement<Object>(child);
				if (temp.showInGui()) {
					elements.add(temp);
				}
			}

			while (pI.hasNext()) {
				ConfigElement<?> temp = getTypedElement(pI.next());
				if (temp.showInGui()) {
					elements.add(temp);
				}
			}

			return elements;
		} else {
			return null;
		}
	}
}
