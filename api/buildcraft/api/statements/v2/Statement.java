package buildcraft.api.statements.v2;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class Statement {
	public enum Target {
		INTERNAL, EXTERNAL;
	}

	private final String uniqueTag;
	private final Target target;

	public Statement(Target target, String uniqueTag) {
		this.target = target;
		this.uniqueTag = uniqueTag;
	}

	public Target getTarget() {
		return target;
	}

	public String getUniqueTag() {
		return uniqueTag;
	}

	public abstract String[] getDescription();

	public int minParameters(IStatementContainer container) {
		return 0;
	}

	public int maxParameters(IStatementContainer container) {
		return 0;
	}

	public StatementParameter createParameter(IStatementContainer container, int index) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	public abstract IIcon getIcon();

	@SideOnly(Side.CLIENT)
	public abstract void registerIcons(IIconRegister iconRegister);

	public Statement rotateLeft() {
		return this;
	}
}
