package ct.buildcraft.api.schematics;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class SchematicEntityFactoryRegistry {
    private static final Set<SchematicEntityFactory<?>> FACTORIES = new TreeSet<>();

    @Deprecated
    /**Check ! */
    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    Predicate<SchematicEntityContext> predicate,
                                                                    Supplier<S> supplier) {
        FACTORIES.add(new SchematicEntityFactory<>(
            new ResourceLocation(name),
            priority,
            predicate,
            supplier
        ));
    }

    @Deprecated
    /**Check ! */
    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    List<ResourceLocation> entities,
                                                                    Supplier<S> supplier) {
        registerFactory(
            name,
            priority,
            context -> entities.contains(ForgeRegistries.ENTITY_TYPES.getKey(context.entity.getType())),
            supplier
        );
    }

    public static List<SchematicEntityFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    @Nonnull
    public static <S extends ISchematicEntity> SchematicEntityFactory<S> getFactoryByInstance(S instance) {
        // noinspection unchecked
        return (SchematicEntityFactory<S>) FACTORIES.stream()
            .filter(schematicEntityFactory -> schematicEntityFactory.clazz == instance.getClass())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicEntityFactory<?> getFactoryByName(ResourceLocation name) {
        return FACTORIES.stream()
            .filter(schematicEntityFactory -> schematicEntityFactory.name.equals(name))
            .findFirst()
            .orElse(null);
    }
}
