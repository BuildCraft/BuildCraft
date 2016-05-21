package buildcraft.core.lib;

import java.util.Collections;
import java.util.Set;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.item.Item;

import buildcraft.api.ObjectDefinition;

/* Essentially an implementation of a "TriMap"- but we cannot create a truly generic version because that would require
 * six methods all based on the formula "getAfromB" where A and B differ between A, B and C. (Not pleasant to use or
 * read). If such a class could easily exist, then this would extend it with "TriMap<String, Item, ObjectDefinition>" */
public class HashDefinitionMap<I extends Item, D extends ObjectDefinition> implements IDefinitionTriMap<I, D> {
    // "core" maps -- these go forwards in the structure then loop back (string -> item -> definition -> string ...)
    private final BiMap<String, I> tagItem;
    private final BiMap<I, D> itemDefinition;
    private final BiMap<D, String> definitionTag;

    // "reverse" maps -- these go backwards in the structure then loop back (string <- item <- definition <- string ...)
    private final BiMap<I, String> itemTag;
    private final BiMap<D, I> definitionItem;
    private final BiMap<String, D> tagDefinition;

    // The set that contains all the elements
    private final Set<Triple<String, I, D>> elementSet, unmodifiableSet;

    public static <I extends Item, D extends ObjectDefinition> HashDefinitionMap<I, D> create() {
        return new HashDefinitionMap<>();
    }

    public HashDefinitionMap() {
        tagItem = HashBiMap.create();
        itemDefinition = HashBiMap.create();
        definitionTag = HashBiMap.create();

        itemTag = tagItem.inverse();
        definitionItem = itemDefinition.inverse();
        tagDefinition = definitionTag.inverse();

        elementSet = Sets.newHashSet();
        unmodifiableSet = Collections.unmodifiableSet(elementSet);
    }

    @Override
    public void put(I item, D definition) {
        if (item == null || definition == null) {
            throw new NullPointerException("Cannot put any null values!");
        }
        String tag = definition.globalUniqueTag;
        if (containsTag(tag) || containsItem(item) || containsDefinition(definition)) {
            throw new IllegalArgumentException("Cannot re-put any values into the map!");
        }
        tagItem.put(tag, item);
        itemDefinition.put(item, definition);
        definitionTag.put(definition, tag);
        elementSet.add(createTriple(tag, item, definition));
    }

    private Triple<String, I, D> createTriple(String tag, I item, D definition) {
        return ImmutableTriple.of(tag, item, definition);
    }

    @Override
    public boolean containsTag(String tag) {
        return tagItem.containsKey(tag);
    }

    @Override
    public boolean containsItem(I item) {
        return itemDefinition.containsKey(item);
    }

    @Override
    public boolean containsDefinition(D definition) {
        return definitionTag.containsKey(definition);
    }

    @Override
    public void clear() {
        tagItem.clear();
        itemDefinition.clear();
        definitionTag.clear();
        elementSet.clear();
    }

    @Override
    public void remove(String tag) {
        if (!containsTag(tag)) {
            return;
        }
        I item = getItem(tag);
        D definition = getDefinition(tag);
        remove(tag, item, definition);
    }

    @Override
    public void remove(I item) {
        if (!containsItem(item)) {
            return;
        }
        D definition = getDefinition(item);
        String tag = getTag(item);
        remove(tag, item, definition);
    }

    @Override
    public void remove(D definition) {
        if (!containsDefinition(definition)) {
            return;
        }
        String tag = getTag(definition);
        I item = getItem(definition);
        remove(tag, item, definition);
    }

    private void remove(String tag, I item, D definition) {
        tagItem.remove(tag);
        itemDefinition.remove(item);
        definitionTag.remove(definition);
        Triple<String, I, D> toRemove = null;
        for (Triple<String, I, D> triple : elementSet) {
            if (triple.getMiddle() == item) {
                toRemove = triple;
                break;
            }
        }
        elementSet.remove(toRemove);
    }

    @Override
    public String getTag(I item) {
        return itemTag.get(item);
    }

    @Override
    public String getTag(D definition) {
        return definitionTag.get(definition);
    }

    @Override
    public I getItem(String tag) {
        return tagItem.get(tag);
    }

    @Override
    public I getItem(D definition) {
        return definitionItem.get(definition);
    }

    @Override
    public D getDefinition(String tag) {
        return tagDefinition.get(tag);
    }

    @Override
    public D getDefinition(I item) {
        return itemDefinition.get(item);
    }

    @Override
    public Set<Triple<String, I, D>> getTripleSet() {
        return unmodifiableSet;
    }
}
