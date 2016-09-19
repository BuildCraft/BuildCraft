package buildcraft.transport.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.api_move.IPipeItem;
import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PipeDefinition;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemPipeHolder extends ItemBlock implements IItemBuildCraft, IPipeItem {
    public final PipeDefinition definition;
    private final String id;
    private String unlocalisedName;
    private CreativeTabs creativeTab;

    public ItemPipeHolder(PipeDefinition definition) {
        super(BCTransportBlocks.pipeHolder);
        this.definition = definition;
        this.id = "item.pipe." + definition.identifier.getResourceDomain() + "." + definition.identifier.getResourcePath();
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        init();
    }

    public ItemPipeHolder registerWithPipeApi() {
        PipeAPI.pipeRegistry.setItemForPipe(definition, this);
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public PipeDefinition getDefiniton() {
        return definition;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (int i = 0; i <= 16; i++) {
            variants.put(i, new ModelResourceLocation("buildcrafttransport:pipe_item#inventory"));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String colourComponent = "";
        int meta = stack.getMetadata();
        if (meta > 0 && meta <= 16) {
            EnumDyeColor colour = EnumDyeColor.byMetadata(meta - 1);
            colourComponent = ColourUtil.getTextFullTooltip(colour) + " ";
        }
        return colourComponent + super.getItemStackDisplayName(stack);
    }

    // ItemBlock overrides these to point to the block

    @Override
    public ItemBlock setUnlocalizedName(String unlocalizedName) {
        this.unlocalisedName = "item." + unlocalizedName;
        return this;
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalisedName;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return unlocalisedName;
    }

    @Override
    public Item setCreativeTab(CreativeTabs tab) {
        creativeTab = tab;
        return this;
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return creativeTab;
    }
}
