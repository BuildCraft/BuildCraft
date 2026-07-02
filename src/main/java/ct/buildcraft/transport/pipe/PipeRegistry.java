/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.transport.pipe.IItemPipe;
import ct.buildcraft.api.transport.pipe.IPipeRegistry;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.transport.item.ItemPipeHolder;
import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public enum PipeRegistry implements IPipeRegistry {
    INSTANCE;

    private final Map<ResourceLocation, PipeDefinition> definitions = new HashMap<>();
    private final Map<PipeDefinition, IItemPipe> pipeItems = new IdentityHashMap<>();

    @Override
    public void registerPipe(PipeDefinition definition) {
        definitions.put(definition.identifier, definition);
    }

    @Override
    public IItemPipe registryItemForPipe(RegistryObject<Block> block, PipeDefinition definition) {
        ItemPipeHolder item = new ItemPipeHolder(definition);
        if (!definitions.values().contains(definition)) 
        	definitions.put(definition.identifier, definition);
        pipeItems.put(definition, item);
        return item;
    }
    
    @Override
    public ItemPipeHolder createItemForPipe(PipeDefinition definition) {
        ItemPipeHolder item = new ItemPipeHolder(definition);
//        helper.addForcedItem(item);
        if (definitions.values().contains(definition)) {
            setItemForPipe(definition, item);
        }
        return item;
    }
    
    @Override
    public void setItemForPipe(PipeDefinition definition, @Nullable IItemPipe item) {
        if (definition == null) {
            throw new NullPointerException("definition");
        }
        if (item == null) {
        	pipeItems.remove(definition);
        } else {
        	pipeItems.put(definition, item);
        }
    }

    @Override
    public IItemPipe getItemForPipe(PipeDefinition definition) {
        return pipeItems.get(definition);
    }

    @Override
    @Nullable
    public PipeDefinition getDefinition(ResourceLocation identifier) {
        return definitions.get(identifier);
    }

    @Nonnull
    public PipeDefinition loadDefinition(String identifier) throws InvalidInputDataException {
        PipeDefinition def = getDefinition(new ResourceLocation(identifier));
        if (def == null) {
            throw new InvalidInputDataException("Unknown pipe definition " + identifier);
        }
        return def;
    }

    @Override
    public Iterable<PipeDefinition> getAllRegisteredPipes() {
        return ImmutableList.copyOf(definitions.values());
    }

	public Map<PipeDefinition, IItemPipe> getPipeItemsMap() {
		return pipeItems;
	}

}
