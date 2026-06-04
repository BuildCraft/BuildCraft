/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipeRegistry;
import buildcraft.api.transport.pipe.PipeDefinition;

public enum PipeRegistry implements IPipeRegistry {
    INSTANCE;

    private final Map<Identifier, PipeDefinition> definitions = new HashMap<>();
    private final Map<PipeDefinition, IItemPipe> pipeItems = new IdentityHashMap<>();

    @Override
    public void registerPipe(PipeDefinition definition) {
        definitions.put(definition.identifier, definition);
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
    public IItemPipe createItemForPipe(PipeDefinition definition) {
        // STUB(R.Chen): item creation deferred — blocked by transport.item.ItemPipeHolder
        // (extends BlockItem, pulls in BCTransportBlocks.pipeHolder / BlockPipeHolder) and
        // lib.registry.RegistrationHelper (Forge RegistryEvent / OreDictionaryStub / GameRegistry).
        // Restore once those migrate. The definition→item map (setItemForPipe/getItemForPipe)
        // and the lookup half (registerPipe/getDefinition/loadDefinition) remain fully functional.
        throw new UnsupportedOperationException("createItemForPipe is not yet migrated to Fabric");
    }

    @Override
    public IItemPipe createUnnamedItemForPipe(PipeDefinition definition, Consumer<Item> postCreate) {
        // STUB(R.Chen): see createItemForPipe — deferred alongside ItemPipeHolder / RegistrationHelper.
        throw new UnsupportedOperationException("createUnnamedItemForPipe is not yet migrated to Fabric");
    }

    @Override
    public IItemPipe getItemForPipe(PipeDefinition definition) {
        return pipeItems.get(definition);
    }

    @Override
    @Nullable
    public PipeDefinition getDefinition(Identifier identifier) {
        return definitions.get(identifier);
    }

    @Nonnull
    public PipeDefinition loadDefinition(String identifier) throws InvalidInputDataException {
        PipeDefinition def = getDefinition(new Identifier(identifier));
        if (def == null) {
            throw new InvalidInputDataException("Unknown pipe definition " + identifier);
        }
        return def;
    }

    @Override
    public Iterable<PipeDefinition> getAllRegisteredPipes() {
        return ImmutableList.copyOf(definitions.values());
    }
}
