package buildcraft.transport.api.impl;

import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.item.Item;

import buildcraft.api.transport.pipe_bc8.IPipeRegistry;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.core.lib.HashDefinitionMap;

public enum PipeRegistry implements IPipeRegistry {
    INSTANCE;

    private final HashDefinitionMap<Item, PipeDefinition_BC8> pipeTriMap = HashDefinitionMap.create();

    @Override
    public Set<Triple<String, Item, PipeDefinition_BC8>> getDefinitions() {
        return pipeTriMap.getTripleSet();
    }

    @Override
    public Item getItem(PipeDefinition_BC8 definition) {
        return pipeTriMap.getItem(definition);
    }

    @Override
    public PipeDefinition_BC8 getDefinition(Item item) {
        return pipeTriMap.getDefinition(item);
    }

    @Override
    public String getUniqueTag(Item item) {
        return pipeTriMap.getTag(item);
    }

    @Override
    public Item getItem(String tag) {
        return pipeTriMap.getItem(tag);
    }

    @Override
    public PipeDefinition_BC8 getDefinition(String uniqueTag) {
        return pipeTriMap.getDefinition(uniqueTag);
    }

    @Override
    public String getTag(PipeDefinition_BC8 definition) {
        return pipeTriMap.getTag(definition);
    }

    @Override
    public Item registerDefinition(PipeDefinition_BC8 definition) {
        Item item = null;
        pipeTriMap.put(item, definition);
        return item;
    }
}
