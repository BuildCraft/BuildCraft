/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipeRegistry;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.transport.BCTransport;
import buildcraft.transport.item.ItemPipeHolder;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public enum PipeRegistry implements IPipeRegistry {
    INSTANCE;

    public static final RegistrationHelper helper = new RegistrationHelper(BCTransport.MODID);

    private final Map<ResourceLocation, PipeDefinition> definitions = new HashMap<>();
    // private final Map<PipeDefinition, IItemPipe> pipeItems = new IdentityHashMap<>();
    private final Map<PipeDefinition, Map<DyeColor, RegistryObject<? extends IItemPipe>>> pipeItems = new IdentityHashMap<>();

    @Override
    public void registerPipe(PipeDefinition definition) {
        definitions.put(definition.identifier, definition);
    }

    @Override
//    public void setItemForPipe(PipeDefinition definition, @Nullable IItemPipe item)
    public void setItemForPipe(PipeDefinition definition, Map<DyeColor, RegistryObject<? extends IItemPipe>> item) {
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
//    public ItemPipeHolder createItemForPipe(PipeDefinition definition)
    public Map<DyeColor, RegistryObject<? extends IItemPipe>> createItemForPipe(PipeDefinition definition) {
        Map<DyeColor, RegistryObject<? extends IItemPipe>> map = new HashMap<>();
//        ItemPipeHolder item = ItemPipeHolder.createAndTag(definition);
        // colorless
        RegistryObject<ItemPipeHolder> item = ItemPipeHolder.createAndTag(definition, null);
        if (definitions.values().contains(definition)) {
//            setItemForPipe(definition, item);
            setItemForPipe(definition, map);
        }
        map.put(null, item);

        // 16 colous
        for (DyeColor colour : DyeColor.values()) {
            item = ItemPipeHolder.createAndTag(definition, colour);
            map.put(colour, item);
        }
//        return item;
        return map;
    }

//    @Override
//    public IItemPipe createUnnamedItemForPipe(PipeDefinition definition, Consumer<Item> postCreate) {
//        ItemPipeHolder item = ItemPipeHolder.create(definition);
//        postCreate.accept(item);
//        helper.addForcedItem(item);
//        if (definitions.values().contains(definition)) {
//            setItemForPipe(definition, item);
//        }
//        return item;
//    }

    @Override
//    public IItemPipe getItemForPipe(PipeDefinition definition)
    public IItemPipe getItemForPipe(PipeDefinition definition, DyeColor colour) {
//        return pipeItems.get(definition).get();
        return pipeItems.get(definition).get(colour).get();
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
}
