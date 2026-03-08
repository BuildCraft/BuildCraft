package buildcraft.datagen.base;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BCBaseAdvancementGenerator extends AdvancementProvider {

    protected static Advancement ROOT;
    protected static Advancement GUIDE;
    public static Advancement MARKERS;
    public static Advancement GEARS;
    public static Advancement WRENCHED;

    protected static final ImpossibleTrigger.TriggerInstance IMPOSSIBLE = new ImpossibleTrigger.TriggerInstance();

    public BCBaseAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    protected static ItemPredicate tag(TagKey<Item> tag) {
        return new ItemPredicate(tag, null, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY);
    }
}
