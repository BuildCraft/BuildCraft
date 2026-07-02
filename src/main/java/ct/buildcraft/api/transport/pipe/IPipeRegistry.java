package ct.buildcraft.api.transport.pipe;

import ct.buildcraft.transport.item.ItemPipeHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public interface IPipeRegistry {
    PipeDefinition getDefinition(ResourceLocation identifier);

    void registerPipe(PipeDefinition definition);
    
    IItemPipe getItemForPipe(PipeDefinition definition);

    /** Creates and registries an {@link IItemPipe} for the given {@link Block} and {@link PipeDefinition}. 
     *  The item will be automatically registered with forge.
     **/
    IItemPipe registryItemForPipe(RegistryObject<Block> block, PipeDefinition definition);

    /** Identical to {@link #createItemForPipe(PipeDefinition)}, but doesn't require registering tags with buildcraft
     * lib in order to register.
     * 
     * @param postCreate A function to call in order to setup the {@link Item#setRegistryName(ResourceLocation)} and
     *            {@link Item#setUnlocalizedName(String)}. */
//    IItemPipe createUnnamedItemForPipe(PipeDefinition definition, Consumer<Item> postCreate);

    Iterable<PipeDefinition> getAllRegisteredPipes();

	void setItemForPipe(PipeDefinition definition, IItemPipe item);

	ItemPipeHolder createItemForPipe(PipeDefinition definition);
}
