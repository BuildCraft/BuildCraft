package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.FacadeStateManager.FacadeBlockStateInfo;
import buildcraft.transport.plug.FacadeStateManager.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager.FullFacadeInstance;
import buildcraft.transport.plug.PluggableFacade;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Nonnull
    public ItemStack createItemStack(FullFacadeInstance state) {
        ItemStack item = new ItemStack(this);
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        state.writeToNbt(nbt, "states");
        return item;
    }

    public static FullFacadeInstance getStates(@Nonnull ItemStack item) {
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        return FullFacadeInstance.readFromNbt(nbt, "states");
    }

    @Override
    public FacadeType getFacadeType(@Nonnull ItemStack facade) {
        return getStates(facade).type;
    }

    @Override
    public ItemStack getFacadeForBlock(IBlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            return StackUtil.EMPTY;
        } else {
            return createItemStack(FullFacadeInstance.createSingle(info, false));
        }
    }

    @Override
    public IBlockState[] getBlockStatesForFacade(ItemStack facade) {
        FullFacadeInstance info = getStates(facade);
        IBlockState[] states = new IBlockState[info.phasedStates.length];
        for (int i = 0; i < states.length; i++) {
            states[i] = info.phasedStates[i].stateInfo.state;
        }
        return states;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        FullFacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCTransportPlugs.facade, holder, side, fullState);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        // Add a single phased facade as a default
        FacadePhasedState[] states = {//
            new FacadePhasedState(FacadeStateManager.validFacadeStates.get(Blocks.STONE.getDefaultState()), false, null),//
            new FacadePhasedState(FacadeStateManager.validFacadeStates.get(Blocks.PLANKS.getDefaultState()), false, EnumDyeColor.RED),//
            new FacadePhasedState(FacadeStateManager.validFacadeStates.get(Blocks.LOG.getDefaultState()), false, EnumDyeColor.CYAN),//
        };
        FullFacadeInstance inst = new FullFacadeInstance(states);
        subItems.add(createItemStack(inst));

        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                subItems.add(createItemStack(FullFacadeInstance.createSingle(info, false)));
                subItems.add(createItemStack(FullFacadeInstance.createSingle(info, true)));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FullFacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            return super.getItemStackDisplayName(stack) + ": " + displayName;
        } else {
            return LocaleUtil.localize("item.FacadePhased.name");
        }
    }

    public static String getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        String s = assumedStack.getDisplayName();
        if (state.isHollow) {
            s += " (" + LocaleUtil.localize("item.Facade.state_hollow") + ")";
        }
        return s;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        FullFacadeInstance states = getStates(stack);
        // for (FacadePhasedState state : states.phasedStates) {
        // ItemStack requiredStack = state.stateInfo.requiredStack;
        // requiredStack.getItem().addInformation(requiredStack, player, tooltip, advanced);
        // }
        if (states.type == FacadeType.Phased) {
            String stateString = LocaleUtil.localize("item.FacadePhased.state");
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
                tooltip.add(String.format(stateString, LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
            }
        } else {
            String propertiesStart = TextFormatting.GRAY + "" + TextFormatting.ITALIC;
            FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
            for (IProperty prop : info.varyingProperties) {
                Comparable comp = info.state.getValue(prop);
                tooltip.add(propertiesStart + prop.getName() + " = " + prop.getName(comp));
            }
        }
    }
}
