package buildcraft.datagen.base;

import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class BCBaseAdvancementGenerator implements DataProvider {

    protected static Advancement ROOT;
    protected static Advancement GUIDE;
    public static Advancement MARKERS;
    public static Advancement GEARS;
    public static Advancement WRENCHED;

    private final PackOutput output;
    private final ExistingFileHelper fileHelperIn;

    protected static final ImpossibleTrigger.TriggerInstance IMPOSSIBLE = new ImpossibleTrigger.TriggerInstance();

    public BCBaseAdvancementGenerator(PackOutput output, ExistingFileHelper fileHelperIn) {
        this.output = output;
        this.fileHelperIn = fileHelperIn;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = this.output.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        List<CompletableFuture<?>> list = new ArrayList<CompletableFuture<?>>();
        Consumer<Advancement> consumer = (advancement) ->
        {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = createPath(path, advancement);

                list.add(DataProvider.saveStable(cache, advancement.deconstruct().serializeToJson(), path1));
            }
        };

        registerAdvancements(consumer, this.fileHelperIn);

        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    protected abstract void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper);

    private static Path createPath(Path path, Advancement advancement) {
        return path.resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");
    }

    protected static ItemPredicate tag(TagKey<Item> tag) {
        return new ItemPredicate(tag, null, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY);
    }
}
