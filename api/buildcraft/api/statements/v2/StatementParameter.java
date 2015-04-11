package buildcraft.api.statements.v2;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

/**
 * WARNING! Each StatementParameter is expected to be provided as a class
 * and have an EMPTY constructor, as they will be instantiated
 * dynamically.
 */
public abstract class StatementParameter {
	private final String uniqueTag;

	public StatementParameter(String uniqueTag) {
		this.uniqueTag = uniqueTag;
	}

	public String getUniqueTag() {
		return uniqueTag;
	}

	/**
	 * @return Either an Icon or an ItemStack representing the Icon.
	 */
	public Object getIcon() {
		return null;
	}

	public void registerIcons(IIconRegister iconRegister) {

	}

	public abstract String[] getDescription();

	public void onClick(IStatementContainer source, Click click) {

	}

	public void readFromNBT(NBTTagCompound compound) {

	}

	public void writeToNBT(NBTTagCompound compound) {

	}

	public StatementParameter rotateLeft() {
		return this;
	}
}
