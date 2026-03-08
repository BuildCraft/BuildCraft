package buildcraft.lib.client.guide.ref;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.statements.IStatement;
import buildcraft.lib.client.guide.entry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class GuideGroupManager {
    public static final List<PageValueType<?>> knownTypes = new ArrayList<>();
    public static final Map<ResourceLocation, GuideGroupSet> sets = new HashMap<>();

    private static final Map<Class<?>, PageValueType<?>> knownClasses = new WeakHashMap<>();
    private static final Map<Class<?>, Function<Object, PageValue<?>>> transformers = new WeakHashMap<>();

    static {
        addValidClass(ItemStackValueFilter.class, PageEntryItemStack.INSTANCE);
        addValidClass(IStatement.class, PageEntryStatement.INSTANCE);
        addTransformer(ItemStack.class, ItemStackValueFilter.class, ItemStackValueFilter::new);
        addTransformer(Item.class, ItemStack.class, ItemStack::new);
        addTransformer(Block.class, ItemStack.class, ItemStack::new);

        temp();
    }

    private static void temp() {
        addEntries("buildcraft", "pipe_power_providers",
                BCItems.Silicon.PLUG_PULSAR,
                BCItems.Transport.PLUG_POWER_ADAPTOR,
                BCBlocks.Core.ENGINE_WOOD,
                BCBlocks.Core.ENGINE_STONE,
                BCBlocks.Core.ENGINE_IRON)//
                .addKeyArray(
                        BCItems.Transport.PIPE_ITEMS_WOOD_COLORLESS,
                        BCItems.Transport.PIPE_ITEMS_DIAMOND_WOOD_COLORLESS,
                        BCItems.Transport.PIPE_ITEMS_EMZULI_COLORLESS,
                        BCItems.Transport.PIPE_FLUIDS_WOOD_COLORLESS,
                        BCItems.Transport.PIPE_FLUIDS_DIAMOND_WOOD_COLORLESS
                );
        addEntries("buildcraft", "full_power_providers",
                BCBlocks.Core.ENGINE_STONE,
                BCBlocks.Core.ENGINE_IRON)
                .addKeyArray(
                        BCBlocks.Builders.BUILDER,
                        BCBlocks.Builders.FILLER,
                        BCBlocks.Builders.QUARRY,
                        BCBlocks.Factory.DISTILLER,
                        BCBlocks.Factory.MINING_WELL,
                        BCBlocks.Factory.PUMP,
                        BCBlocks.Silicon.LASER
                );
        addEntries("buildcraft", "laser_power_providers",
                BCBlocks.Silicon.LASER)
                .addKeyArray(
                        BCBlocks.Silicon.ADVANCED_CRAFTING_TABLE,
                        BCBlocks.Silicon.ASSEMBLY_TABLE,
                        BCBlocks.Silicon.INTEGRATION_TABLE
                );
        addEntries("buildcraft", "area_markers",
                BCBlocks.Core.MARKER_VOLUME,
                BCItems.Core.VOLUME_BOX)
                .addKeyArray(BCBlocks.Builders.QUARRY,
                        BCBlocks.Builders.ARCHITECT,
                        BCBlocks.Builders.FILLER
                );
    }

    // Known types

    public static <F, T> void addTransformer(Class<F> fromClass, Class<T> toClass, Function<F, T> transform) {
        if (isValidClass(fromClass)) {
            throw new IllegalArgumentException("You cannot register a transformer from an already-registered class!");
        }
        PageValueType<?> destType = getEntryType(toClass);
        if (destType == null) {
            // Function<T, PageValue<Dest>> where Dest is presumed to be a valid type
            Function<Object, PageValue<?>> destTransform = getTransform(toClass);
            if (destTransform != null) {
                Function<Object, PageValue<?>> realTransform = o ->
                {
                    F from = fromClass.cast(o);
                    T to = transform.apply(from);
                    return destTransform.apply(to);
                };
                transformers.put(fromClass, realTransform);

                return;
            }
            throw new IllegalArgumentException("You cannot register a transformer to an unregistered class!");
        }
        Function<Object, PageValue<?>> realTransform = o ->
        {
            F from = fromClass.cast(o);
            T to = transform.apply(from);
            return destType.wrap(to);
        };
        transformers.put(fromClass, realTransform);
    }

    public static <T> void addValidClass(Class<T> clazz, PageValueType<T> type) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException("Arrays are never valid!");
        }
        knownClasses.put(clazz, type);
        knownTypes.add(type);
    }

    /** This checks to see if then given object is valid. There are two types of validity:
     * <ul>
     * <li>If the object is of one of the registered classes in {@link GuideGroupManager#knownClasses}</li>
     * <li>If the object is not null or is an invalid value in some other way.</li>
     * </ul>
     * This will throw an exception if the value is not of a registered class, and return false if it is an invalid
     * value in some other way (for example if it is null). */
    static boolean isValidObject(Object value) {
        if (value == null) {
            return false;
        }

        return isValidClass(value.getClass());
    }

    public static PageValue<?> toPageValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof PageValue) {
            return (PageValue<?>) value;
        }
        PageValueType<?> entryType = getEntryType(value.getClass());
        if (entryType != null) {
            return entryType.wrap(value);
        }
        Function<Object, PageValue<?>> transform = getTransform(value.getClass());
        if (transform != null) {
            return transform.apply(value);
        }
        throw new IllegalArgumentException("Unknown " + value.getClass()
                + " - is this a programming mistake, or have you forgotton to register the class as valid?");
    }

    private static boolean isValidClass(Class<?> clazz) {
        return getEntryType(clazz) != null;
    }

    @Nullable
    private static PageValueType<?> getEntryType(Class<?> clazz) {
        if (knownClasses.containsKey(clazz)) {
            return knownClasses.get(clazz);
        }
        PageValueType<?> type = null;
        if (!clazz.isArray()) {
            search:
            {
                Class<?> superClazz = clazz.getSuperclass();
                if (superClazz != null) {
                    type = getEntryType(superClazz);
                    if (type != null) {
                        break search;
                    }
                }
                for (Class<?> cls : clazz.getInterfaces()) {
                    type = getEntryType(cls);
                    if (type != null) {
                        break search;
                    }
                }
            }
            knownClasses.put(clazz, type);
        }
        return type;
    }

    private static Function<Object, PageValue<?>> getTransform(Class<? extends Object> clazz) {
        Function<Object, PageValue<?>> func = transformers.get(clazz);
        if (func != null) {
            return func;
        }
        if (!clazz.isArray()) {
            search:
            {
                Class<?> superClazz = clazz.getSuperclass();
                if (superClazz != null) {
                    func = getTransform(superClazz);
                    if (func != null) {
                        break search;
                    }
                }
                for (Class<?> cls : clazz.getInterfaces()) {
                    func = getTransform(cls);
                    if (func != null) {
                        break search;
                    }
                }
            }
            transformers.put(clazz, func);
        }
        return func;
    }

    // Internals

    @Nullable
    public static GuideGroupSet get(ResourceLocation group) {
        return sets.get(group);
    }

    @Nullable
    public static GuideGroupSet get(String domain, String group) {
        return get(new ResourceLocation(domain, group));
    }

    public static GuideGroupSet getOrCreate(String domain, String group) {
        return sets.computeIfAbsent(new ResourceLocation(domain, group), GuideGroupSet::new);
    }

    // Basic adders

    public static GuideGroupSet addEntry(String domain, String group, Object value) {
        return getOrCreate(domain, group).addSingle(value);
    }

    public static GuideGroupSet addEntries(String domain, String group, Object... values) {
        return getOrCreate(domain, group).addArray(values);
    }

    public static GuideGroupSet addEntries(String domain, String group, Collection<Object> values) {
        return getOrCreate(domain, group).addCollection(values);
    }

    public static GuideGroupSet addKey(String domain, String group, Object value) {
        return getOrCreate(domain, group).addKey(value);
    }

    public static GuideGroupSet addKeys(String domain, String group, Object... values) {
        return getOrCreate(domain, group).addKeyArray(values);
    }

    public static GuideGroupSet addKeys(String domain, String group, Collection<Object> values) {
        return getOrCreate(domain, group).addKeyCollection(values);
    }
}
