/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;
import net.minecraft.src.buildcraft.transport.Pipe.GateKind;

public class GuiGateInterface extends GuiAdvancedInterface {
	
	IInventory playerInventory;
	Pipe pipe;
	
	LinkedList<Trigger> potentialTriggers = new LinkedList <Trigger> ();
	LinkedList<Action> potentialActions = new LinkedList <Action> ();
	
	private int nbEntries;
	
	class TriggerSlot extends AdvancedSlot {
		Trigger trigger;
		
		public TriggerSlot (int x, int y, Trigger trigger) {
			super (x, y);
			
			this.trigger = trigger;
		}				
		
		@Override
		public String getDescription () {
			if (trigger != null) {
				return trigger.getDescription();
			} else {
				return "";
			}
		}
		
		@Override
		public String getTexture () {
			if (trigger != null) {
				return trigger.getTextureFile();
			} else {
				return "";
			}
		}
		
		@Override
		public int getTextureIndex () {
			if (trigger != null) {
				return trigger.getIndexInTexture();
			} else {
				return 0;
			}
		}
		
		@Override
		public boolean isDefined () {
			return trigger != null;
		}
	}
	
	class ActionSlot extends AdvancedSlot {
		Action action;
		
		public ActionSlot (int x, int y, Action action) {
			super (x, y);
			
			this.action = action;
		}				
		
		@Override
		public String getDescription () {
			
			if (action != null) {
				return action.getDescription();
			} else {
				return "";
			}
		}
		
		@Override
		public String getTexture () {
			if (action != null) {
				return action.getTexture();
			} else {
				return "";
			}
		}
		
		@Override
		public int getTextureIndex () {
			if (action != null) {
				return action.getIndexInTexture();	
			} else {
				return 0;
			}
		}
		
		@Override
		public boolean isDefined () {
			return action != null;
		}
	}
	
	class TriggerParameterSlot extends AdvancedSlot {
		TriggerParameter parameter;
		
		public TriggerParameterSlot(int x, int y, TriggerParameter parameter) {			
			super(x, y);
			
			this.parameter = parameter;
		}			

		@Override
		public boolean isDefined () {
			return parameter != null;			
		}
		
		@Override
		public ItemStack getItemStack () {
			if (this.parameter != null) {
				return this.parameter.getItem();
			} else {
				return null;
			}
		}
	}
	
	public GuiGateInterface(IInventory playerInventory, Pipe pipe) {
		super(new CraftingGateInterface(playerInventory, pipe));
		this.playerInventory = playerInventory;
		this.pipe = pipe;
		xSize = 175;
		ySize = 207;
		
		potentialActions.addAll(pipe.getActions());
				
		potentialTriggers.addAll (BuildCraftAPI.getPipeTriggers(pipe));
	
		for (Orientations o : Orientations.dirs()) {
			Position pos = new Position (pipe.xCoord, pipe.yCoord, pipe.zCoord, o);
			pos.moveForwards(1.0);
			TileEntity tile = pipe.worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
			int blockID = pipe.worldObj.getBlockId((int) pos.x, (int) pos.y, (int) pos.z);
			Block block = Block.blocksList [blockID];
			
			LinkedList <Trigger> nearbyTriggers = BuildCraftAPI.getNeighborTriggers(block, tile);
			
			for (Trigger t : nearbyTriggers) {
				if (!potentialTriggers.contains(t)) {
					potentialTriggers.add(t);
				}
			}
			
			LinkedList <Action> nearbyActions = BuildCraftAPI.getNeighborActions(block, tile);
			
			for (Action a : nearbyActions) {
				if (!potentialActions.contains(a)) {
					potentialActions.add(a);
				}
			}
		}
		
		int position = 0;
		
		if (pipe.gateKind == GateKind.Single) {
			nbEntries = 1;
			
			slots = new AdvancedSlot [2];
			slots [0] = new TriggerSlot (65, 54, pipe.getTrigger(0));
			slots [1] = new ActionSlot (99, 54, pipe.getAction(0));
		} else if (pipe.gateKind == GateKind.AND_2 || pipe.gateKind == GateKind.OR_2) {
			nbEntries = 2;
			
			slots = new AdvancedSlot [4];

			slots [0] = new TriggerSlot (65, 46, pipe.getTrigger(0));
			slots [1] = new TriggerSlot (65, 64, pipe.getTrigger(1));
			slots [2] = new ActionSlot (99, 46, pipe.getAction(0));
			slots [3] = new ActionSlot (99, 64, pipe.getAction(1));
		} else if (pipe.gateKind == GateKind.AND_3 || pipe.gateKind == GateKind.OR_3) {
			nbEntries = 4;
			
			slots = new AdvancedSlot [12];

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerSlot (60, 36 + 18 * k, pipe.getTrigger(position));
				position++;
			}	

			for (int k = 0; k < 4; ++k) {
				slots [position] = new ActionSlot (112, 36 + 18 * k, pipe.getAction(position - 4));
				position++;
			}		

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerParameterSlot (78, 36 + 18 * k, pipe.getTriggerParameter(position - 8));
				position++;

			}
		} else if (pipe.gateKind == GateKind.AND_4 || pipe.gateKind == GateKind.OR_4) {
			nbEntries = 8;
			
			slots = new AdvancedSlot [24];

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerSlot (8, 36 + 18 * k, pipe.getTrigger(position));
				position++;
				slots [position] = new TriggerSlot (100, 36 + 18 * k, pipe.getTrigger(position));
				position++;
			}	

			for (int k = 0; k < 4; ++k) {
				slots [position] = new ActionSlot (60, 36 + 18 * k, pipe.getAction(position - 8));
				position++;
				slots [position] = new ActionSlot (152, 36 + 18 * k, pipe.getAction(position - 8));
				position++;
			}		

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerParameterSlot (26, 36 + 18 * k, pipe.getTriggerParameter(position - 16));
				position++;
				slots [position] = new TriggerParameterSlot (118, 36 + 18 * k, pipe.getTriggerParameter(position - 16));
				position++;
			}
		}
		
		
	}
	
	public void addTriggerToPotentialList (Trigger t) {
		if (!potentialTriggers.contains(t)) {
			potentialTriggers.add(t);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		switch (pipe.gateKind) {
		case Single:
			fontRenderer.drawString("Gate", 75, 15, 0x404040);
			break;
		case AND_2:
			fontRenderer.drawString("Iron AND Gate", 60, 15, 0x404040);
			break;
		case AND_3:
			fontRenderer.drawString("Golden AND Gate", 60, 15, 0x404040);
			break;
		case AND_4:
			fontRenderer.drawString("Diamond AND Gate", 60, 15, 0x404040);
			break;
		case OR_2:
			fontRenderer.drawString("Iron OR Gate", 60, 15, 0x404040);
			break;
		case OR_3:
			fontRenderer.drawString("Golden OR Gate", 60, 15, 0x404040);
			break;
		case OR_4:
			fontRenderer.drawString("Diamond OR Gate", 60, 15, 0x404040);
			break;
		}
		
		fontRenderer.drawString("Inventory", 8, ySize - 97,
				0x404040);
		
		drawForegroundSelection ();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int texture = 0;
		
		if (pipe.gateKind == GateKind.Single) {
			texture = mc.renderEngine
			.getTexture("/net/minecraft/src/buildcraft/transport/gui/gate_interface_1.png");
		} else if (pipe.gateKind == GateKind.AND_2 || pipe.gateKind == GateKind.OR_2) {
			texture = mc.renderEngine
			.getTexture("/net/minecraft/src/buildcraft/transport/gui/gate_interface_2.png");
		} else if (pipe.gateKind == GateKind.AND_3 || pipe.gateKind == GateKind.OR_3) {
			texture = mc.renderEngine
			.getTexture("/net/minecraft/src/buildcraft/transport/gui/gate_interface_3.png");
		} else if (pipe.gateKind == GateKind.AND_4 || pipe.gateKind == GateKind.OR_4) {
			texture = mc.renderEngine
			.getTexture("/net/minecraft/src/buildcraft/transport/gui/gate_interface_4.png");
		}		
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots [s];
			
			if (slot instanceof TriggerSlot) {
				Trigger trigger = ((TriggerSlot) slot).trigger;
				
				if (pipe.gateKind.ordinal() >= GateKind.AND_3.ordinal()) { 
					TriggerParameter parameter = null;

					if (slots [s + nbEntries * 2] != null && slots [s + nbEntries * 2].isDefined()) {
						parameter = ((TriggerParameterSlot) slots [s + nbEntries * 2]).parameter;
					}

					if (pipe.isNearbyTriggerActive(trigger, parameter)) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 35, cornerY + slot.y + 6, 176,
								18, 18, 4);			
					}			

					if (trigger == null || !trigger.hasParameter()) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y - 1, 176,
								0, 18, 18);
					}
				} else {
					if (pipe.isNearbyTriggerActive(trigger, null)) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y + 6, 176,
								18, 18, 4);			
					}	
				}
			}		
		}
		
		drawBackgroundSlots();
	}
		
	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		
		int position = getSlotAtLocation (i - cornerX, j - cornerY);
		
		AdvancedSlot slot = null;

		if (position != -1) {
			slot = slots[position];
		}

		if (slot instanceof TriggerSlot && potentialTriggers.size() > 0) {
			TriggerSlot triggerSlot = (TriggerSlot) slot;
				
			if (triggerSlot.trigger == null) {
				triggerSlot.trigger = potentialTriggers.getFirst();
			} else {
				Iterator<Trigger> it;

				if (k == 0) {
					it = potentialTriggers.iterator();
				} else {
					it = potentialTriggers.descendingIterator();
				}

				for (; it.hasNext();) {
					Trigger trigger = it.next();

					if (!it.hasNext()) {
						triggerSlot.trigger = null;
						break;
					}

					if (trigger == triggerSlot.trigger) {
						triggerSlot.trigger = it.next();
						break;
					}
				}
			}

			pipe.setTrigger(position, triggerSlot.trigger);
			
			if (pipe.gateKind.ordinal() >= GateKind.AND_3.ordinal()) {
				((TriggerParameterSlot) slots [position + nbEntries * 2]).parameter = null;
				pipe.setTriggerParameter(position, null);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;
			
			if (actionSlot.action == null) {
				actionSlot.action = potentialActions.getFirst();
			} else {
				Iterator<Action> it;
				
				if (k == 0) {
					it = potentialActions.iterator();
				} else {
					it = potentialActions.descendingIterator();
				}
				
				for (;it.hasNext();) {
					Action action = it.next();

					if (!it.hasNext()) {
						actionSlot.action = null;
						break;
					}

					if (action == actionSlot.action) {
						actionSlot.action = it.next();
						break;
					}
				}
			}

			pipe.setAction(position - nbEntries, actionSlot.action);
		} else if (slot instanceof TriggerParameterSlot) {
			TriggerSlot trigger = (TriggerSlot) slots [position - nbEntries * 2];
			
			if (trigger.isDefined() && trigger.trigger.hasParameter()) {
				TriggerParameter param = trigger.trigger.createParameter();
				
				if (param != null) {
					param.set(mc.thePlayer.inventory.getItemStack());
					((TriggerParameterSlot) slot).parameter = param;

					pipe.setTriggerParameter(position - nbEntries * 2, param);
				}
			}
		}
	}
}
