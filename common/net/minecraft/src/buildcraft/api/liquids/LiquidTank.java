package net.minecraft.src.buildcraft.api.liquids;

public class LiquidTank {
	private LiquidStack liquid;
	private int capacity;
	
	public LiquidTank(int liquidId, int quantity, int capacity) {
		this(new LiquidStack(liquidId, quantity), capacity);
	}
	public LiquidTank(LiquidStack liquid, int capacity) {
		this.liquid = liquid;
		this.capacity = capacity;
	}
	
	public LiquidStack getLiquid() {
		return this.liquid;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
}
