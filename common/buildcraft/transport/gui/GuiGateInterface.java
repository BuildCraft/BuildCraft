/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.GateDefinition;

public class GuiGateInterface extends GuiAdvancedInterface {

	IInventory playerInventory;
	private final ContainerGateInterface container;
	private final Pipe pipe;
	private int numSlots;

	class TriggerSlot extends AdvancedSlot {

		Pipe pipe;
		int slot;

		public TriggerSlot(int x, int y, Pipe pipe, int slot) {
			super(GuiGateInterface.this, x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription() {
			ITrigger trigger = pipe.gate.getTrigger(slot);

			if (trigger != null) {
				return trigger.getDescription();
			} else {
				return "";
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IIcon getIcon() {
			ITrigger trigger = pipe.gate.getTrigger(slot);

			if (trigger != null) {
				return trigger.getIcon();
			} else {
				return null;
			}
		}

		@Override
		public boolean isDefined() {
			return pipe.gate.getTrigger(slot) != null;
		}

		public ITrigger getTrigger() {
			return pipe.gate.getTrigger(slot);
		}
	}

	class ActionSlot extends AdvancedSlot {

		Pipe pipe;
		int slot;

		public ActionSlot(int x, int y, Pipe pipe, int slot) {
			super(GuiGateInterface.this, x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription() {
			IAction action = pipe.gate.getAction(slot);
			if (action != null) {
				return action.getDescription();
			} else {
				return "";
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IIcon getIcon() {
			IAction action = pipe.gate.getAction(slot);

			if (action != null) {
				return action.getIcon();
			} else {
				return null;
			}
		}

		@Override
		public ResourceLocation getTexture() {
			IAction action = pipe.gate.getAction(slot);

			if (action instanceof BCAction) {
				BCAction bcAction = (BCAction) action;

				if (bcAction.getTextureMap() == 0) {
					return TextureMap.locationBlocksTexture;
				}
			}

			return super.getTexture();
		}

		@Override
		public boolean isDefined() {
			return pipe.gate.getAction(slot) != null;
		}

		public IAction getAction() {
			return pipe.gate.getAction(slot);
		}
	}

	class TriggerParameterSlot extends AdvancedSlot {

		Pipe pipe;
		int slot;

		public TriggerParameterSlot(int x, int y, Pipe pipe, int slot) {
			super(GuiGateInterface.this, x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public boolean isDefined() {
			return pipe.gate.getTriggerParameter(slot) != null;
		}

		@Override
		public ItemStack getItemStack() {
			ITriggerParameter parameter = pipe.gate.getTriggerParameter(slot);

			if (parameter != null) {
				return parameter.getItemStack();
			} else {
				return null;
			}
		}

		public ITriggerParameter getTriggerParameter() {
			return pipe.gate.getTriggerParameter(slot);
		}
	}

	public GuiGateInterface(IInventory playerInventory, Pipe pipe) {
		super(new ContainerGateInterface(playerInventory, pipe), null, null);

		container = (ContainerGateInterface) this.inventorySlots;
		this.pipe = pipe;

		this.playerInventory = playerInventory;
		xSize = 176;
		ySize = pipe.gate.material.guiHeight;

		int position = 0;
		numSlots = pipe.gate.material.numSlots;

		if (numSlots == 1) {
			slots = new AdvancedSlot[2];

			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new ActionSlot(98, 26, pipe, 0);
		} else if (numSlots == 2) {
			slots = new AdvancedSlot[4];

			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new TriggerSlot(62, 44, pipe, 1);
			slots[2] = new ActionSlot(98, 26, pipe, 0);
			slots[3] = new ActionSlot(98, 44, pipe, 1);
		} else if (numSlots == 4) {
			slots = new AdvancedSlot[12];

			for (int k = 0; k < 4; ++k) {
				slots[position] = new TriggerSlot(53, 26 + 18 * k, pipe, position);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots[position] = new ActionSlot(107, 26 + 18 * k, pipe, position - 4);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots[position] = new TriggerParameterSlot(71, 26 + 18 * k, pipe, position - 8);
				position++;

			}
		} else if (numSlots == 8) {
			slots = new AdvancedSlot[24];

			for (int k = 0; k < 4; ++k) {
				slots[position] = new TriggerSlot(8, 26 + 18 * k, pipe, position);
				position++;
				slots[position] = new TriggerSlot(98, 26 + 18 * k, pipe, position);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots[position] = new ActionSlot(62, 26 + 18 * k, pipe, position - 8);
				position++;
				slots[position] = new ActionSlot(152, 26 + 18 * k, pipe, position - 8);
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots[position] = new TriggerParameterSlot(26, 26 + 18 * k, pipe, position - 16);
				position++;
				slots[position] = new TriggerParameterSlot(116, 26 + 18 * k, pipe, position - 16);
				position++;
			}
		}

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String name = container.getGateName();

		fontRendererObj.drawString(name, getCenteredOffset(name), 10, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

		container.synchronize();

		ResourceLocation texture = container.getGateGuiFile();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		int triggerTracker = 0;
		boolean allTriggersActive = true;
		for (AdvancedSlot slot : slots) {
			if (slot instanceof TriggerSlot) {
				boolean active = container.triggerState[triggerTracker++];
				if (slot.isDefined() && ((TriggerSlot) slot).getTrigger() != null && !active) {
					allTriggersActive = false;
					break;
				}
			}
		}

		triggerTracker = 0;
		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots[s];

			if (slot instanceof TriggerSlot) {
				ITrigger trigger = ((TriggerSlot) slot).getTrigger();
				boolean halfWidth = pipe.gate.logic == GateDefinition.GateLogic.AND && !allTriggersActive;

				if (pipe.gate.material.hasParameterSlot) {

					if (container.triggerState[triggerTracker++]) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 35, cornerY + slot.y + 6, 176, 18, halfWidth ? 9 : 18, 4);
					}

					if (trigger == null || !trigger.hasParameter()) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y - 1, 176, 0, 18, 18);
					}
				} else if (container.triggerState[triggerTracker++]) {
					mc.renderEngine.bindTexture(texture);

					drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y + 6, 176, 18, halfWidth ? 9 : 18, 4);
				}
			} else if (slot instanceof TriggerParameterSlot) {
				TriggerParameterSlot paramSlot = (TriggerParameterSlot) slot;
				TriggerSlot trigger = (TriggerSlot) slots[s - numSlots * 2];

				if (trigger.isDefined() && trigger.getTrigger().requiresParameter()) {
					if (paramSlot.getItemStack() == null) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x - 1, cornerY + slot.y - 1, 176, 22, 18, 18);
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

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		AdvancedSlot slot = null;

		if (position < 0) {
			return;
		}

		slot = slots[position];

		if (slot instanceof TriggerSlot && container.hasTriggers()) {
			TriggerSlot triggerSlot = (TriggerSlot) slot;

			ITrigger changed = null;
			if (triggerSlot.getTrigger() == null) {

				if (k == 0) {
					changed = container.getFirstTrigger();
				} else {
					changed = container.getLastTrigger();
				}

			} else {
				Iterator<ITrigger> it = container.getTriggerIterator(k != 0);

				for (; it.hasNext();) {
					ITrigger trigger = it.next();

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

			container.setTrigger(position, changed, true);

			if (pipe.gate.material.hasParameterSlot) {
				container.setTriggerParameter(position, null, true);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;

			IAction changed = null;
			if (actionSlot.getAction() == null) {

				if (k == 0) {
					changed = container.getFirstAction();
				} else {
					changed = container.getLastAction();
				}

			} else {
				Iterator<IAction> it = container.getActionIterator(k != 0);

				for (; it.hasNext();) {
					IAction action = it.next();

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

			container.setAction(position - numSlots, changed, true);
		} else if (slot instanceof TriggerParameterSlot) {
			TriggerSlot trigger = (TriggerSlot) slots[position - numSlots * 2];

			if (trigger.isDefined() && trigger.getTrigger().hasParameter()) {
				ITriggerParameter param = trigger.getTrigger().createParameter();

				if (param != null) {
					param.set(mc.thePlayer.inventory.getItemStack());
					container.setTriggerParameter(position - numSlots * 2, param, true);
				}
			}
		}

		container.markDirty();
	}
}
