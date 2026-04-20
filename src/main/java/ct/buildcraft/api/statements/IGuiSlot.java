package ct.buildcraft.api.statements;

import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.IConvertable;
import ct.buildcraft.api.core.render.ISprite;
import com.google.common.collect.ImmutableList;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IGuiSlot extends IConvertable {
    /** Every statement needs a unique tag, it should be in the format of "&lt;modid&gt;:&lt;name&gt;".
     *
     * @return the unique id */
    String getUniqueTag();

    /** Return the description in the UI. Note that this should NEVER be called directly, instead this acts as a bridge
     * for {@link #getTooltip()}. (As such this might return null or throw an exception) */
    @OnlyIn(Dist.CLIENT)
    Component getDescription();

    /** @return The full tooltip for the UI. */
    @OnlyIn(Dist.CLIENT)
    default List<Component> getTooltip() {
    	Component desc = getDescription();
        if (desc == null) {
            return ImmutableList.of();
        }
        return ImmutableList.of(desc);
    }

    /** @return A sprite to show in a GUI, or null if this should not render a sprite. */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    ISprite getSprite();
}
