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

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;
import net.minecraft.src.buildcraft.core.utils.StringUtil;
import net.minecraft.src.buildcraft.transport.Gate.GateKind;

import org.lwjgl.opengl.GL11;

public class GuiGateInterface extends GuiAdvancedInterface {

	IInventory playerInventory;
	
	private final CraftingGateInterface _container;


	private int nbEntries;

	class TriggerSlot extends AdvancedSlot {
		
		Pipe pipe;
		int slot;

		public TriggerSlot (int x, int y, Pipe pipe, int slot) {
			super (x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription () {
			Trigger trigger = pipe.getTrigger(slot);
			if (trigger != null)
				return trigger.getDescription();
			else
				return "";
		}

		@Override
		public String getTexture () {
			Trigger trigger = pipe.getTrigger(slot);
			if (trigger != null)
				return trigger.getTextureFile();
			else
				return "";
		}

		@Override
		public int getTextureIndex () {
			Trigger trigger = pipe.getTrigger(slot);
			if (trigger != null)
				return trigger.getIndexInTexture();
			else
				return 0;
		}

		@Override
		public boolean isDefined () {
			return pipe.getTrigger(slot) != null;
		}
		
		public Trigger getTrigger() {
			return pipe.getTrigger(slot);
		}
	}

	class ActionSlot extends AdvancedSlot {
		
		Pipe pipe;
		int slot;

		public ActionSlot (int x, int y, Pipe pipe, int slot) {
			super (x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription () {
			Action action = pipe.getAction(slot);
			if (action != null)
				return action.getDescription();
			else
				return "";
		}

		@Override
		public String getTexture () {
			Action action = pipe.getAction(slot);
			if (action != null)
				return action.getTexture();
			else
				return "";
		}

		@Override
		public int getTextureIndex () {
			Action action = pipe.getAction(slot);
			if (action != null)
				return action.getIndexInTexture();
			else
				return 0;
		}

		@Override
		public boolean isDefined () {
			return pipe.getAction(slot) != null;
		}
		
		public Action getAction() {
			return pipe.getAction(slot);
		}
	}

	class TriggerParameterSlot extends AdvancedSlot {
		
		Pipe pipe;
		int slot;

		public TriggerParameterSlot(int x, int y, Pipe pipe, int slot) {
			super(x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public boolean isDefined () {
			return pipe.getTriggerParameter(slot) != null;
		}

		@Override
		public ItemStack getItemStack () {
			TriggerParameter parameter = pipe.getTriggerParameter(slot);
			if (parameter != null)
				return parameter.getItem();
			else
				return null;
		}
		
		public TriggerParameter getTriggerParameter() {
			return pipe.getTriggerParameter(slot);
		}
	}

	public GuiGateInterface(IInventory playerInventory, Pipe pipe) {
		super(new CraftingGateInterface(playerInventory, pipe));
		
		_container = (CraftingGateInterface) this.inventorySlots;
		
		this.playerInventory = playerInventory;
		xSize = 175;
		ySize = 207;

		int position = 0;

		if (pipe.gate.kind == GateKind.Single) {
			nbEntries = 1;

			slots = new AdvancedSlot [2];
			slots [0] = new TriggerSlot (65, 54, pipe, 0);
			slots [1] = new ActionSlot (99, 54, pipe, 0);
		} else if (pipe.gate.kind == GateKind.AND_2 || pipe.gate.kind == GateKind.OR_2) {
			nbEntries = 2;

			slots = new AdvancedSlot [4];

			slots [0] = new TriggerSlot (65, 46, pipe, 0);
			slots [1] = new TriggerSlot (65, 64, pipe, 1);
			slots [2] = new ActionSlot (99, 46, pipe, 0);
			slots [3] = new ActionSlot (99, 64, pipe, 1);
		} else if (pipe.gate.kind == GateKind.AND_3 || pipe.gate.kind == GateKind.OR_3) {
			nbEntries = 4;

			slots = new AdvancedSlot [12];

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerSlot (60, 36 + 18 * k, pipe, position);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots [position] = new ActionSlot (112, 36 + 18 * k, pipe, position - 4);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerParameterSlot (78, 36 + 18 * k, pipe, position - 8);
				position++;

			}
		} else if (pipe.gate.kind == GateKind.AND_4 || pipe.gate.kind == GateKind.OR_4) {
			nbEntries = 8;

			slots = new AdvancedSlot [24];

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerSlot (8, 36 + 18 * k, pipe, position);
				position++;
				slots [position] = new TriggerSlot (100, 36 + 18 * k, pipe, position);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots [position] = new ActionSlot (60, 36 + 18 * k, pipe, position - 8);
				position++;
				slots [position] = new ActionSlot (152, 36 + 18 * k, pipe, position - 8);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots [position] = new TriggerParameterSlot (26, 36 + 18 * k, pipe, position - 16);
				position++;
				slots [position] = new TriggerParameterSlot (118, 36 + 18 * k, pipe, position - 16);
				position++;
			}
		}


	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		String name = _container.getGateName();

		fontRenderer.drawString(name, getCenteredOffset(name), 15, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, ySize - 97, 0x404040);

		drawForegroundSelection ();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		
		_container.synchronize();
		int texture = 0;

		texture = mc.renderEngine.getTexture(_container.getGateGuiFile());

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots [s];

			if (slot instanceof TriggerSlot) {
				Trigger trigger = ((TriggerSlot) slot).getTrigger();

				if (_container.getGateOrdinal() >= GateKind.AND_3.ordinal()) {
					TriggerParameter parameter = null;

					if (slots [s + nbEntries * 2] != null && slots [s + nbEntries * 2].isDefined())
						parameter = ((TriggerParameterSlot) slots [s + nbEntries * 2]).getTriggerParameter();

					if (_container.isNearbyTriggerActive(trigger, parameter)) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 35, cornerY + slot.y + 6, 176,
								18, 18, 4);
					}

					if (trigger == null || !trigger.hasParameter()) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y - 1, 176,
								0, 18, 18);
					}
				} else if (_container.isNearbyTriggerActive(trigger, null)) {
					mc.renderEngine.bindTexture(texture);

					drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y + 6, 176,
							18, 18, 4);
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

		if (position < 0)
			return;
			
		slot = slots[position];

		if (slot instanceof TriggerSlot && _container.hasTriggers()) {
			TriggerSlot triggerSlot = (TriggerSlot) slot;

			Trigger changed = null;
			if (triggerSlot.getTrigger() == null) {

				if (k == 0)
					changed = _container.getFirstTrigger();
				else
					changed = _container.getLastTrigger();

			} else {
				Iterator<Trigger> it = _container.getTriggerIterator(k != 0);
				
				for (; it.hasNext();) {
					Trigger trigger = it.next();

					if (!it.hasNext()) {
						changed = null;
						break;
					}

					if (trigger == triggerSlot.getTrigger()) {
						changed = it.next();
						break;
					}
				}
			}

			_container.setTrigger(position, changed, true);

			if (_container.getGateOrdinal() >= GateKind.AND_3.ordinal()) {
				_container.setTriggerParameter(position, null, true);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;

			Action changed = null;
			if (actionSlot.getAction() == null) {

				if (k == 0)
					changed = _container.getFirstAction();
				else
					changed = _container.getLastAction();

			} else {
				Iterator<Action> it = _container.getActionIterator(k != 0);

				for (;it.hasNext();) {
					Action action = it.next();

					if (!it.hasNext()) {
						changed = null;
						break;
					}

					if (action == actionSlot.getAction()) {
						changed = it.next();
						break;
					}
				}
			}

			_container.setAction(position - nbEntries, changed, true);
		} else if (slot instanceof TriggerParameterSlot) {
			TriggerSlot trigger = (TriggerSlot) slots [position - nbEntries * 2];

			if (trigger.isDefined() && trigger.getTrigger().hasParameter()) {
				TriggerParameter param = trigger.getTrigger().createParameter();

				if (param != null) {
					param.set(mc.thePlayer.inventory.getItemStack());
					_container.setTriggerParameter(position - nbEntries * 2, param, true);
				}
			}
		}
		
		_container.markDirty();
	}
}
