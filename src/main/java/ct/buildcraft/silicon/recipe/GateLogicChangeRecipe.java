package ct.buildcraft.silicon.recipe;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.BCLib;
import ct.buildcraft.silicon.BCSiliconRecipes;
import ct.buildcraft.silicon.gate.EnumGateLogic;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.silicon.item.ItemPluggableGate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GateLogicChangeRecipe extends CustomRecipe{
	
	public GateLogicChangeRecipe(ResourceLocation p_43833_) {
		super(p_43833_);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
	      ItemStack itemstack = ItemStack.EMPTY;
	      for(int i = 0; i < container.getContainerSize(); ++i) {
	         ItemStack itemstack1 = container.getItem(i);
	         if (!itemstack1.isEmpty()) {
	            if (itemstack1.getItem() instanceof ItemPluggableGate) {
	               if (!itemstack.isEmpty()) {
	                  return false;
	               }
	               itemstack = itemstack1;
	            }
	         }
	      }
	      return !itemstack.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
	      ItemStack itemstack = ItemStack.EMPTY;
	      for(int i = 0; i < container.getContainerSize(); ++i) {
	         ItemStack itemstack1 = container.getItem(i);
	         if (!itemstack1.isEmpty()) {
	            if (itemstack1.getItem() instanceof ItemPluggableGate) {
	               if (!itemstack.isEmpty()) {
	                  return ItemStack.EMPTY;
	               }
	               itemstack = itemstack1;
	            }
	         }
	      }
	      if(!itemstack.isEmpty()) {
	    	  try {
		    	  ItemPluggableGate gate = (ItemPluggableGate)itemstack.getItem();
		    	  CompoundTag tag = itemstack.getTag().copy();
		    	  if(tag!=null && !tag.isEmpty()&& tag.contains("gate")) {
		    		  CompoundTag gateTag = tag.getCompound("gate");
		    		  if(gateTag.getInt("logic") == EnumGateLogic.AND.ordinal())
		    			  gateTag.putInt("logic", EnumGateLogic.OR.ordinal());
		    		  else
		    			  gateTag.putInt("logic", EnumGateLogic.AND.ordinal());
		    		  return gate.getStack(new GateVariant(tag));
		    	  }
		    	  BCLog.logger.error("GateLogicChangeRecipe:Encounter a gate with missing gate nbt!");
			} catch (Exception e) {
				BCLog.logger.error("GateLogicChangeRecipe:Encounter a gate with invaild gate nbt!");
				if(BCLib.DEV)//TODO
					e.printStackTrace();
			}

	      }
	      return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int x, int y) {
		return x>=1 && y >=1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return BCSiliconRecipes.GATE_CHANGE_SERIALIZER.get();
	}

}
