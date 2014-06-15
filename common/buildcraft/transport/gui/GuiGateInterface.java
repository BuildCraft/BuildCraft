/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.ActionState;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public class GuiGateInterface extends GuiAdvancedInterface {

	IInventory playerInventory;
	private final ContainerGateInterface container;
	private final Pipe<?> pipe;
	private Gate gate;

	private abstract class StatementSlot extends AdvancedSlot {
		public int slot;
		public ArrayList<StatementParameterSlot> parameters = new ArrayList<StatementParameterSlot>();

		public StatementSlot(int x, int y, Pipe<?> pipe, int slot) {
			super(GuiGateInterface.this, x, y);

			this.slot = slot;
		}

		@Override
		public String getDescription() {
			IStatement stmt = getStatement();

			if (stmt != null) {
				return stmt.getDescription();
			} else {
				return "";
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IIcon getIcon() {
			IStatement stmt = getStatement();

			if (stmt != null) {
				return stmt.getIcon();
			} else {
				return null;
			}
		}

		@Override
		public boolean isDefined() {
			return getStatement() != null;
		}

		public abstract IStatement getStatement();
	}

	private class TriggerSlot extends StatementSlot {
		public TriggerSlot(int x, int y, Pipe<?> pipe, int slot) {
			super(x, y, pipe, slot);
		}

		@Override
		public IStatement getStatement() {
			return gate.getTrigger(slot);
		}
	}

	private class ActionSlot extends StatementSlot {
		public ActionSlot(int x, int y, Pipe<?> pipe, int slot) {
			super(x, y, pipe, slot);
		}

		@Override
		public IStatement getStatement() {
			return gate.getAction(slot);
		}
	}

	private abstract class StatementParameterSlot extends AdvancedSlot {

		public Pipe<?> pipe;
		public int slot;
		public StatementSlot statementSlot;

		public StatementParameterSlot(int x, int y, Pipe<?> pipe, int slot, StatementSlot iStatementSlot) {
			super(GuiGateInterface.this, x, y);

			this.pipe = pipe;
			this.slot = slot;
			this.statementSlot = iStatementSlot;
			statementSlot.parameters.add(this);
		}

		@Override
		public boolean isDefined() {
			return getParameter() != null;
		}

		@Override
		public ItemStack getItemStack() {
			IStatementParameter parameter = getParameter();

			if (parameter != null) {
				return parameter.getItemStackToDraw();
			} else {
				return null;
			}
		}

		@Override
		public IIcon getIcon() {
			IStatementParameter parameter = getParameter();

			if (parameter != null) {
				return parameter.getIconToDraw();
			} else {
				return null;
			}
		}

		public abstract IStatementParameter getParameter();

		public boolean isAllowed() {
			return statementSlot.getStatement() != null && slot < statementSlot.getStatement().maxParameters();
		}

		public boolean isRequired() {
			return statementSlot.getStatement() != null && slot < statementSlot.getStatement().minParameters();
		}

		public abstract void setParameter(IStatementParameter param, boolean notifyServer);
	}

	class TriggerParameterSlot extends StatementParameterSlot {
		public TriggerParameterSlot(int x, int y, Pipe<?> pipe, int slot, StatementSlot iStatementSlot) {
			super(x, y, pipe, slot, iStatementSlot);
		}

		@Override
		public IStatementParameter getParameter() {
			return gate.getTriggerParameter(statementSlot.slot, slot);
		}

		@Override
		public void setParameter(IStatementParameter param, boolean notifyServer) {
			container.setTriggerParameter(statementSlot.slot, slot, (ITriggerParameter) param, notifyServer);
		}
	}

	class ActionParameterSlot extends StatementParameterSlot {
		public ActionParameterSlot(int x, int y, Pipe<?> pipe, int slot, StatementSlot iStatementSlot) {
			super(x, y, pipe, slot, iStatementSlot);
		}

		@Override
		public IStatementParameter getParameter() {
			return gate.getActionParameter(statementSlot.slot, slot);
		}

		@Override
		public void setParameter(IStatementParameter param, boolean notifyServer) {
			container.setActionParameter(statementSlot.slot, slot, (IActionParameter) param, notifyServer);
		}
	}

	public GuiGateInterface(IInventory playerInventory, Pipe<?> pipe) {
		super(new ContainerGateInterface(playerInventory, pipe), null, null);

		container = (ContainerGateInterface) this.inventorySlots;
		container.gateCallback = this;
		this.pipe = pipe;
		this.playerInventory = playerInventory;
	}

	public void setGate(Gate gate) {
		this.gate = gate;
		init();
	}

	public void init() {
		if (gate == null) {
			return;
		}
		xSize = 176;
		ySize = gate.material.guiHeight;

		int position = 0;

		if (gate.material == GateMaterial.REDSTONE) {
			slots = new AdvancedSlot[2];

			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new ActionSlot(98, 26, pipe, 0);
		} else if (gate.material == GateMaterial.IRON) {
			slots = new AdvancedSlot[4];

			slots[0] = new TriggerSlot(62, 26, pipe, 0);
			slots[1] = new TriggerSlot(62, 44, pipe, 1);
			slots[2] = new ActionSlot(98, 26, pipe, 0);
			slots[3] = new ActionSlot(98, 44, pipe, 1);
		} else if (gate.material == GateMaterial.GOLD) {
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
				slots[position] = new TriggerParameterSlot(71, 26 + 18 * k, pipe, 0, (TriggerSlot) slots[k]);
				position++;

			}
		} else if (gate.material == GateMaterial.DIAMOND) {
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
				slots[position] = new TriggerParameterSlot(26, 26 + 18 * k, pipe, 0,
						(TriggerSlot) slots[k]);
				position++;
				slots[position] = new TriggerParameterSlot(116, 26 + 18 * k, pipe, 0,
						(TriggerSlot) slots[k + 4]);
				position++;
			}
		} else if (gate.material == GateMaterial.EMERALD) {
			slots = new AdvancedSlot[32];
			int lastPos;

			for (int y = 0; y < 4; ++y) {
				slots[position] = new TriggerSlot(8, 26 + 18 * y, pipe, y);
				lastPos = position;
				position++;

				for (int x = 0; x < 3; ++x) {
					slots[position] = new TriggerParameterSlot(
							8 + 18 * (x + 1),
							26 + 18 * y,
							pipe,
							x,
							(TriggerSlot) slots[lastPos]);

					position++;
				}

				slots[position] = new ActionSlot(98, 26 + 18 * y, pipe, y);
				lastPos = position;
				position++;

				for (int x = 0; x < 3; ++x) {
					slots[position] = new ActionParameterSlot(
							98 + 18 * (x + 1),
							26 + 18 * y,
							pipe,
							x,
							(ActionSlot) slots[lastPos]);
					position++;
				}
			}
		}
		initGui();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (gate == null) {
			return;
		}
		String name = container.getGateName();

		fontRendererObj.drawString(name, getCenteredOffset(name), 10, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		container.synchronize();

		if (gate == null) {
			return;
		}

		ResourceLocation texture = container.getGateGuiFile();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);

		int actionTracker = 0;

		actionTracker = 0;
		for (AdvancedSlot slot : slots) {
			if (slot instanceof TriggerSlot) {
				boolean halfWidth = container.actionsState[actionTracker] == ActionState.Partial;

				if (container.actionsState[actionTracker] != ActionState.Deactivated) {
					mc.renderEngine.bindTexture(texture);

					drawTexturedModalRect(cornerX + slot.x + 17 + 18 * gate.material.numTriggerParameters, cornerY
							+ slot.y + 6, 176, 18, halfWidth ? 9 : 18, 4);
				}

				actionTracker++;
			} else if (slot instanceof StatementParameterSlot) {
				StatementParameterSlot paramSlot = (StatementParameterSlot) slot;
				StatementSlot statement = paramSlot.statementSlot;

				mc.renderEngine.bindTexture(texture);

				if (statement.isDefined()) {
					if (!paramSlot.isAllowed()) {
						drawTexturedModalRect(cornerX + slot.x - 1, cornerY + slot.y - 1, 176, 0, 18, 18);
					} else if (paramSlot.isRequired() && paramSlot.getItemStack() == null) {
						drawTexturedModalRect(cornerX + slot.x - 1, cornerY + slot.y - 1, 176, 22, 18, 18);
					}
				} else {
					drawTexturedModalRect(cornerX + slot.x - 1, cornerY + slot.y - 1, 176, 0, 18, 18);
				}
			}
		}

		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		if (gate == null) {
			return;
		}
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

			if (triggerSlot.getStatement() == null) {

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

					if (trigger == triggerSlot.getStatement()) {
						changed = it.next();
						break;
					}
				}
			}

			if (changed == null) {
				container.setTrigger(triggerSlot.slot, null, true);
			} else {
				container.setTrigger(triggerSlot.slot, changed.getUniqueTag(), true);
			}

			for (StatementParameterSlot p : triggerSlot.parameters) {
				container.setTriggerParameter(triggerSlot.slot, p.slot, null, true);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;

			IAction changed = null;
			if (actionSlot.getStatement() == null) {

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

					if (action == actionSlot.getStatement()) {
						changed = it.next();
						break;
					}
				}
			}

			if (changed == null) {
				container.setAction(actionSlot.slot, null, true);
			} else {
				container.setAction(actionSlot.slot, changed.getUniqueTag(), true);
			}

			for (StatementParameterSlot p : actionSlot.parameters) {
				container.setActionParameter(actionSlot.slot, p.slot, null, true);
			}
		} else if (slot instanceof StatementParameterSlot) {
			StatementParameterSlot paramSlot = (StatementParameterSlot) slot;
			StatementSlot statement = paramSlot.statementSlot;

			if (statement.isDefined() && statement.getStatement().maxParameters() != 0) {
				IStatementParameter param = paramSlot.getParameter();

				if (param == null) {
					param = statement.getStatement().createParameter(paramSlot.slot);
				}

				if (param != null) {
					param.clicked(pipe.container, statement.getStatement(), mc.thePlayer.inventory.getItemStack());
					paramSlot.setParameter(param, true);
				}
			}
		}

		container.markDirty();
	}
}
