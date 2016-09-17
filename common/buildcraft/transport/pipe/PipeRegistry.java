package buildcraft.transport.pipe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.item.ItemManager;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.transport.api_move.IPipeItem;
import buildcraft.transport.api_move.IPipeRegistry;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.item.ItemPipeHolder;

public enum PipeRegistry implements IPipeRegistry {
    INSTANCE;

    private final Map<ResourceLocation, PipeDefinition> definitions = new HashMap<>();

    @Override
    public IPipeItem registerPipeAndItem(PipeDefinition definition) {
        registerPipe(definition);
        ItemPipeHolder item = new ItemPipeHolder(definition);
        ItemManager.register(item);
        return item;
    }

    @Override
    public void registerPipe(PipeDefinition definition) {
        definitions.put(definition.identifier, definition);
    }

    @Nullable
    public PipeDefinition getDefinition(ResourceLocation identifier) {
        return definitions.get(identifier);
    }

    @Nonnull
    public PipeDefinition loadDefinition(String identifier) throws LoadingException {
        PipeDefinition def = getDefinition(new ResourceLocation(identifier));
        if (def == null) {
            throw new LoadingException("Unknown pipe defintion " + identifier);
        }
        return def;
    }

    @SideOnly(Side.CLIENT)
    public void fmlInit() {
        for (PipeDefinition def : definitions.values()) {
            def.initSprites();
        }
    }
}
