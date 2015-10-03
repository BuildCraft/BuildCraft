package buildcraft.core.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;

public class BCConfigElement<T> extends ConfigElement {
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

                ConfigElement temp = new BCConfigElement(child);
                if (temp.showInGui()) {
                    elements.add(temp);
                }
            }

            while (pI.hasNext()) {
                ConfigElement temp = new ConfigElement(pI.next());
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
