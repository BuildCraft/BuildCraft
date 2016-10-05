package buildcraft.transport.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.api_move.IItemPluggable;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.gate.EnumGateLogic;
import buildcraft.transport.gate.EnumGateMaterial;
import buildcraft.transport.gate.EnumGateModifier;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.plug.PluggableGate;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemPluggableGate extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableGate(String id) {
        super(id);
    }

    public static GateVariant getVariant(ItemStack stack) {
        return new GateVariant(NBTUtils.getItemData(stack).getCompoundTag("gate"));
    }

    public ItemStack getStack(GateVariant variant) {
        ItemStack stack = new ItemStack(this);
        NBTUtils.getItemData(stack).setTag("gate", variant.writeToNbt());
        return stack;
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, EnumFacing side) {
        GateVariant variant = getVariant(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.getDefaultState());
        PluggableDefinition def = BCTransportPlugs.gate;
        return new PluggableGate(def, holder, side, variant);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        GateVariant variant = getVariant(stack);
        if (variant.material == EnumGateMaterial.CLAY_BRICK) {
            return I18n.translateToLocal("gate.name.basic");
        } else {
            String gateName = I18n.translateToLocal("gate.name");
            String materialName = I18n.translateToLocal("gate.material." + variant.material.tag);
            Object logicName = I18n.translateToLocal("gate.logic." + variant.logic.tag);
            return String.format(gateName, materialName, logicName);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        GateVariant variant = getVariant(stack);
        if (variant.modifier != EnumGateModifier.NO_MODIFIER && variant.material.canBeModified) {
            tooltip.add(I18n.translateToLocal("gate.modifier." + variant.modifier.tag));
            tooltip.add(I18n.translateToLocal("gate.modifier.desc." + variant.modifier.tag));
            if (variant.modifier.slotDivisor != 1) {
                tooltip.add(I18n.translateToLocal("gate.modifier.divisor"));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        subItems.add(new ItemStack(this));
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (!material.canBeModified) {
                continue;
            }
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    subItems.add(getStack(new GateVariant(logic, material, modifier)));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        variants.put(0, new ModelResourceLocation("buildcrafttransport:gate_item#inventory"));
    }
}
