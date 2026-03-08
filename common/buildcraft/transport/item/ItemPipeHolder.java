/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.TagManager;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.pipe.PipeRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPipeHolder extends BlockItem implements IItemBuildCraft, IItemPipe {
    public final PipeDefinition definition;
    private final String id;
    private String unlocalizedName;
    private final DyeColor colour;
//    private ItemGroup creativeTab;

    // protected ItemPipeHolder(PipeDefinition definition, String tagId)
    protected ItemPipeHolder(PipeDefinition definition, String tagId, DyeColor colour) {
        super(BCTransportBlocks.pipeHolder.get(), definition.properties.tab(CreativeTabManager.getTab(TagManager.getTag(tagId, TagManager.EnumTagType.CREATIVE_TAB))));
        this.definition = definition;
        this.id = tagId;
//        this.setMaxDamage(0);
//        this.setHasSubtypes(true);
        if (!"".equals(id)) {
            init();
        }
        this.colour = colour;
    }

    @Override
    public DyeColor getColour() {
        return colour;
    }


//    /** Creates a new {@link ItemPipeHolder} without requiring a tag. */
//    public static ItemPipeHolder create(PipeDefinition definition) {
//        return new ItemPipeHolder(definition, "");
//    }

    /** Creates a new {@link ItemPipeHolder} with a tag that will be taken from {@link TagManager}. */
//    public static ItemPipeHolder createAndTag(PipeDefinition definition)
    public static RegistryObject<ItemPipeHolder> createAndTag(PipeDefinition definition, DyeColor colour) {
        ResourceLocation reg = definition.identifier;
//        String tagId = "item.pipe." + reg.getResourceDomain() + "." + reg.getResourcePath();
        String suffix = colour == null ? "_colorless" : "_" + colour.getName();
        String tagId = "item.pipe." + reg.getNamespace() + "." + reg.getPath();
        String regName = TagManager.getTag(tagId, TagManager.EnumTagType.REGISTRY_NAME).replace(BCTransport.MODID + ":", "") + suffix;
        return PipeRegistry.helper.addForcedItem(regName, () -> new ItemPipeHolder(definition, tagId, colour));
    }

//    public ItemPipeHolder registerWithPipeApi() {
//        PipeApi.pipeRegistry.setItemForPipe(definition, this);
//        return this;
//    }

    @Override
//    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> items)
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
//        if (this.isInCreativeTab(tab))
        if (allowdedIn(tab)) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public String getIdBC() {
        return id;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (int i = 0; i <= 16; i++) {
//            variants.put(i, new ModelResourceLocation("buildcrafttransport:pipe_item#inventory"));
//        }
//    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
//        String colourComponent = "";
//        int meta = stack.getMetadata();
//        if (meta > 0 && meta <= 16) {
//            EnumDyeColor colour = EnumDyeColor.byMetadata(meta - 1);
//            colourComponent = ColourUtil.getTextFullTooltipSpecial(colour) + " ";
//        }
        String colourComponent = this.colour == null ? "" : (ColourUtil.getTextFullTooltipSpecial(this.colour) + " ");
//        return colourComponent + super.getItemStackDisplayName(stack);
        return new StringTextComponent(colourComponent).append(new TranslationTextComponent(this.getDescriptionId(stack)));
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public FontRenderer getFontRenderer(ItemStack stack) {
//        return SpecialColourFontRenderer.INSTANCE;
//    }

    // ItemBlock overrides these to point to the block

    // Calen: can not override final method
    @Override
    public void setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
//        return this;
    }

    @Override
//    public String getUnlocalizedName()
    public String getDescriptionId(ItemStack stack) {
        return this.unlocalizedName;
    }

//    @Override
//    public String getUnlocalizedName(ItemStack stack) {
//        return unlocalizedName;
//    }

//    @Override
//    public Item setCreativeTab(CreativeTabs tab) {
//        creativeTab = tab;
//        return this;
//    }

//    @Override
//    public CreativeTabs getCreativeTab() {
//        return creativeTab;
//    }

    // Misc usefulness

    @Override
    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
//        String tipName = "tip." + unlocalizedName.replace(".name", "").replace("item.", "");
        String tipName = "tip." + this.unlocalizedName.replace(".name", "").replace("item.", "");
        String localised = I18n.get(tipName);
        if (!localised.equals(tipName)) {
            tooltip.add(new StringTextComponent(TextFormatting.GRAY + localised));
        }
        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
//            tooltip.add(new StringTextComponent(LocaleUtil.localizeFluidFlow(fti.transferPerTick)));
            tooltip.add(LocaleUtil.localizeFluidFlowToTranslatableComponent(fti.transferPerTick));
            tooltip.add(LocaleUtil.localizeFluidFlowToTranslatableComponent(fti.transferPerTick));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
//            tooltip.add(new StringTextComponent(LocaleUtil.localizeMjFlow(pti.transferPerTick)));
            tooltip.add(LocaleUtil.localizeMjFlowComponent(pti.transferPerTick));
        } else if (definition.flowType == PipeApi.flowRf && PipeApi.flowRf != null) {
            PipeApi.RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(definition);
            tooltip.add(new StringTextComponent(pti.transferPerTick + " RF/t"));//TODO: Locale!
        }
    }
}
