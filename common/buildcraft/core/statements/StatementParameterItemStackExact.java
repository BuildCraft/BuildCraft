package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

public class StatementParameterItemStackExact implements IStatementParameter {
	protected ItemStack stack;
	private int availableSlots;

	public StatementParameterItemStackExact() {
		this(-1);
	}

	public StatementParameterItemStackExact(int availableSlots) {
		this.availableSlots = availableSlots;
	}

	@Override
	public IIcon getIcon() {
		return null;
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}

	@Override
	public void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
		if (stack != null) {
			if (areItemsEqual(this.stack, stack)) {
				if (mouse.getButton() == 0) {
					this.stack.stackSize += (mouse.isShift()) ? 16 : 1;

					int maxSize = availableSlots < 0 ? 64 : Math.min(64, this.stack.getMaxStackSize() * availableSlots);
					if (this.stack.stackSize > maxSize) {
						this.stack.stackSize = maxSize;
					}
				} else {
					this.stack.stackSize -= (mouse.isShift()) ? 16 : 1;
					if (this.stack.stackSize <= 0) {
						this.stack = null;
					}
				}
			} else {
				this.stack = stack.copy();
			}
		} else {
			if (this.stack != null) {
				if (mouse.getButton() == 0) {
					this.stack.stackSize += (mouse.isShift()) ? 16 : 1;

					int maxSize = availableSlots < 0 ? 64 : Math.min(64, this.stack.getMaxStackSize() * availableSlots);
					if (this.stack.stackSize > maxSize) {
						this.stack.stackSize = maxSize;
					}
				} else {
					this.stack.stackSize -= (mouse.isShift()) ? 16 : 1;
					if (this.stack.stackSize <= 0) {
						this.stack = null;
					}
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (stack != null) {
			NBTTagCompound tagCompound = new NBTTagCompound();
			stack.writeToNBT(tagCompound);
			compound.setTag("stack", tagCompound);
		}

		compound.setInteger("availableSlots", availableSlots);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("availableSlots")) {
			availableSlots = compound.getInteger("availableSlots");
		}
		stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StatementParameterItemStackExact) {
			StatementParameterItemStackExact param = (StatementParameterItemStackExact) object;

			return areItemsEqual(stack, param.stack);
		} else {
			return false;
		}
	}

	private boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
		if (stack1 != null) {
			return stack2 != null && stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
		} else {
			return stack2 == null;
		}
	}

	@Override
	public String getDescription() {
		if (stack != null) {
			return stack.getDisplayName();
		} else {
			return "";
		}
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:stackExact";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {

	}

	@Override
	public IStatementParameter rotateLeft() {
		return this;
	}
}
