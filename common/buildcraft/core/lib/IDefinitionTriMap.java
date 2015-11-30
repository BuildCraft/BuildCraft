package buildcraft.core.lib;

import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.item.Item;

import buildcraft.api.ObjectDefinition;

public interface IDefinitionTriMap<I extends Item, D extends ObjectDefinition> {
    // Additions
    void put(I item, D definition);

    // Removal
    void clear();

    void remove(String tag);

    void remove(I item);

    void remove(D definition);

    // Checks
    boolean containsTag(String tag);

    boolean containsItem(I item);

    boolean containsDefinition(D definition);

    // Getters
    String getTag(I item);

    String getTag(D definition);

    I getItem(String tag);

    I getItem(D definition);

    D getDefinition(String tag);

    D getDefinition(I item);

    Set<Triple<String, I, D>> getTripleSet();
}
