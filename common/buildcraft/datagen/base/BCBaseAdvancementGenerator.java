package buildcraft.datagen.base;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.IntBound;
import net.minecraft.advancements.criterion.NBTPredicate;
import net.minecraft.data.AdvancementProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BCBaseAdvancementGenerator extends AdvancementProvider {

    protected static Advancement ROOT;
    protected static Advancement GUIDE;
    public static Advancement MARKERS;
    public static Advancement GEARS;
    public static Advancement WRENCHED;

    protected static final ImpossibleTrigger.Instance IMPOSSIBLE = new ImpossibleTrigger.Instance();

    public BCBaseAdvancementGenerator(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    protected static ItemPredicate tag(INamedTag<Item> tag) {
        return new ItemPredicate(tag, null, IntBound.ANY, IntBound.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NBTPredicate.ANY);
    }
}
