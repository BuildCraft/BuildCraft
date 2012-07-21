package net.minecraft.src.buildcraft.api.gates;

public interface IAction {

	int getId();
	String getTexture();
	int getIndexInTexture();
	boolean hasParameter();
	String getDescription();

}