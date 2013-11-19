/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Gate.GateKind;
import buildcraft.transport.Pipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiGateInterface extends GuiAdvancedInterface {

	IInventory playerInventory;
	private final ContainerGateInterface _container;
	private int nbEntries;

	class TriggerSlot extends AdvancedSlot {

		Pipe pipe;
		int slot;

		public TriggerSlot(int x, int y, Pipe pipe, int slot) {
			super(x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription() {
			ITrigger trigger = pipe.gate.getTrigger(slot);
			if (trigger != null)
				return trigger.getDescription();
			else
				return "";
		}

		@SideOnly(Side.CLIENT)
		@Override
		public Icon getIcon() {
			ITrigger trigger = pipe.gate.getTrigger(slot);
			if (trigger != null)
				return trigger.getIcon();
			else
				return null;
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
			super(x, y);

			this.pipe = pipe;
			this.slot = slot;
		}

		@Override
		public String getDescription() {
			IAction action = pipe.gate.getAction(slot);
			if (action != null)
				return action.getDescription();
			else
				return "";
		}

		@SideOnly(Side.CLIENT)
		@Override
		public Icon getIcon() {
			IAction action = pipe.gate.getAction(slot);
			if (action != null)
				return action.getIcon();
			else
				return null;
		}

		@Override
		public ResourceLocation getTexture() {
			IAction action = pipe.gate.getAction(slot);
			if (action instanceof BCAction) {
				BCAction bcAction = (BCAction) action;
				if (bcAction.getTextureMap() == 0)
					return TextureMap.locationBlocksTexture;
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
			super(x, y);

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
			if (parameter != null)
				return parameter.getItemStack();
			else
				return null;
		}

		public ITriggerParameter getTriggerParameter() {
			return pipe.gate.getTriggerParameter(slot);
		}
	}

	public GuiGateInterface(IInventory playerInventory, Pipe pipe) {
		super(new ContainerGateInterface(playerInventory, pipe), null, null);

		_container = (ContainerGateInterface) this.inventorySlots;

		this.playerInventory = playerInventory;
		xSize = 176;
		ySize = pipe.gate.getGuiHeight();

		int position = 0;

		if (pipe.gate.kind == GateKind.Single) {
			nbEntries = 1;

			slots = new AdvancedSlot[2];
			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new ActionSlot(98, 26, pipe, 0);
		} else if (pipe.gate.kind == GateKind.AND_2 || pipe.gate.kind == GateKind.OR_2) {
			nbEntries = 2;

			slots = new AdvancedSlot[4];

			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new TriggerSlot(62, 44, pipe, 1);
			slots[2] = new ActionSlot(98, 26, pipe, 0);
			slots[3] = new ActionSlot(98, 44, pipe, 1);
		} else if (pipe.gate.kind == GateKind.AND_3 || pipe.gate.kind == GateKind.OR_3) {
			nbEntries = 4;

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
		} else if (pipe.gate.kind == GateKind.AND_4 || pipe.gate.kind == GateKind.OR_4) {
			nbEntries = 8;

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
		String name = _container.getGateName();

		fontRenderer.drawString(name, getCenteredOffset(name), 10, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

		_container.synchronize();

		ResourceLocation texture = _container.getGateGuiFile();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		int triggerTracker = 0;
		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots[s];

			if (slot instanceof TriggerSlot) {
				ITrigger trigger = ((TriggerSlot) slot).getTrigger();

				if (_container.getGateOrdinal() >= GateKind.AND_3.ordinal()) {

					if (_container.triggerState[triggerTracker++]) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 35, cornerY + slot.y + 6, 176, 18, 18, 4);
					}

					if (trigger == null || !trigger.hasParameter()) {
						mc.renderEngine.bindTexture(texture);

						drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y - 1, 176, 0, 18, 18);
					}
				} else if (_container.triggerState[triggerTracker++]) {
					mc.renderEngine.bindTexture(texture);

					drawTexturedModalRect(cornerX + slot.x + 17, cornerY + slot.y + 6, 176, 18, 18, 4);
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

		if (position < 0)
			return;

		slot = slots[position];

		if (slot instanceof TriggerSlot && _container.hasTriggers()) {
			TriggerSlot triggerSlot = (TriggerSlot) slot;

			ITrigger changed = null;
			if (triggerSlot.getTrigger() == null) {

				if (k == 0) {
					changed = _container.getFirstTrigger();
				} else {
					changed = _container.getLastTrigger();
				}

			} else {
				Iterator<ITrigger> it = _container.getTriggerIterator(k != 0);

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

			_container.setTrigger(position, changed, true);

			if (_container.getGateOrdinal() >= GateKind.AND_3.ordinal()) {
				_container.setTriggerParameter(position, null, true);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;

			IAction changed = null;
			if (actionSlot.getAction() == null) {

				if (k == 0) {
					changed = _container.getFirstAction();
				} else {
					changed = _container.getLastAction();
				}

			} else {
				Iterator<IAction> it = _container.getActionIterator(k != 0);

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

			_container.setAction(position - nbEntries, changed, true);
		} else if (slot instanceof TriggerParameterSlot) {
			TriggerSlot trigger = (TriggerSlot) slots[position - nbEntries * 2];

			if (trigger.isDefined() && trigger.getTrigger().hasParameter()) {
				ITriggerParameter param = trigger.getTrigger().createParameter();

				if (param != null) {
					param.set(mc.thePlayer.inventory.getItemStack());
					_container.setTriggerParameter(position - nbEntries * 2, param, true);
				}
			}
		}

		_container.markDirty();
	}
}
