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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.ActionActiveState;
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
		public String getDescription() {
			IStatementParameter parameter = getParameter();

			if (parameter != null) {
				return parameter.getDescription() != null ? parameter.getDescription() : "";
			} else {
				return null;
			}
		}
		
		@Override
		public ItemStack getItemStack() {
			IStatementParameter parameter = getParameter();

			if (parameter != null) {
				return parameter.getItemStack();
			} else {
				return null;
			}
		}

		@Override
		public IIcon getIcon() {
			IStatementParameter parameter = getParameter();

			if (parameter != null) {
				return parameter.getIcon();
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
			container.setTriggerParameter(statementSlot.slot, slot, (IStatementParameter) param, notifyServer);
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
			container.setActionParameter(statementSlot.slot, slot, (IStatementParameter) param, notifyServer);
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

		slots.clear();
		
		if (gate.material == GateMaterial.REDSTONE) {
			slots.add(new TriggerSlot(62, 26, pipe, 0));
			slots.add(new ActionSlot(98, 26, pipe, 0));
		} else if (gate.material == GateMaterial.IRON) {
			slots.add(new TriggerSlot(62, 26, pipe, 0));
			slots.add(new TriggerSlot(62, 44, pipe, 1));
			slots.add(new ActionSlot(98, 26, pipe, 0));
			slots.add(new ActionSlot(98, 44, pipe, 1));
		} else if (gate.material == GateMaterial.GOLD) {
			for (int k = 0; k < 4; ++k) {
				slots.add(new TriggerSlot(53, 26 + 18 * k, pipe, position));
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots.add(new ActionSlot(107, 26 + 18 * k, pipe, position - 4));
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots.add(new TriggerParameterSlot(71, 26 + 18 * k, pipe, 0, (TriggerSlot) slots.get(k)));
				position++;

			}
		} else if (gate.material == GateMaterial.DIAMOND) {
			for (int k = 0; k < 4; ++k) {
				slots.add(new TriggerSlot(8, 26 + 18 * k, pipe, position));
				position++;
				slots.add(new TriggerSlot(98, 26 + 18 * k, pipe, position));
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots.add(new ActionSlot(62, 26 + 18 * k, pipe, position - 8));
				position++;
				slots.add(new ActionSlot(152, 26 + 18 * k, pipe, position - 8));
				position++;
			}

			for (int k = 0; k < 4; ++k) {
				slots.add(new TriggerParameterSlot(26, 26 + 18 * k, pipe, 0,
						(TriggerSlot) slots.get(position - 16)));
				position++;
				slots.add(new TriggerParameterSlot(116, 26 + 18 * k, pipe, 0,
						(TriggerSlot) slots.get(position - 16)));
				position++;
			}
		} else if (gate.material == GateMaterial.EMERALD) {
			int lastPos;

			for (int y = 0; y < 4; ++y) {
				slots.add(new TriggerSlot(8, 26 + 18 * y, pipe, y));
				lastPos = position;
				position++;

				for (int x = 0; x < 3; ++x) {
					slots.add(new TriggerParameterSlot(
							8 + 18 * (x + 1),
							26 + 18 * y,
							pipe,
							x,
							(TriggerSlot) slots.get(lastPos)));

					position++;
				}

				slots.add(new ActionSlot(98, 26 + 18 * y, pipe, y));
				lastPos = position;
				position++;

				for (int x = 0; x < 3; ++x) {
					slots.add(new ActionParameterSlot(
							98 + 18 * (x + 1),
							26 + 18 * y,
							pipe,
							x,
							(ActionSlot) slots.get(lastPos)));
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

		drawTooltipForSlotAt(par1, par2);
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

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for (AdvancedSlot slot : slots) {
			if (slot instanceof TriggerSlot) {
				boolean halfWidth = container.actionsState[((TriggerSlot) slot).slot] == ActionActiveState.Partial;

				if (container.actionsState[((TriggerSlot) slot).slot] != ActionActiveState.Deactivated) {
					mc.renderEngine.bindTexture(texture);

					drawTexturedModalRect(guiLeft + slot.x + 17 + 18 * gate.material.numTriggerParameters, guiTop
							+ slot.y + 6, 176, 18, halfWidth ? 9 : 18, 4);
				}
			} else if (slot instanceof StatementParameterSlot) {
				StatementParameterSlot paramSlot = (StatementParameterSlot) slot;
				StatementSlot statement = paramSlot.statementSlot;

				mc.renderEngine.bindTexture(texture);

				if (statement.isDefined()) {
					if (!paramSlot.isAllowed()) {
						drawTexturedModalRect(guiLeft + slot.x - 1, guiTop + slot.y - 1, 176, 0, 18, 18);
					} else if (paramSlot.isRequired() && paramSlot.getItemStack() == null) {
						drawTexturedModalRect(guiLeft + slot.x - 1, guiTop + slot.y - 1, 176, 22, 18, 18);
					}
				} else {
					drawTexturedModalRect(guiLeft + slot.x - 1, guiTop + slot.y - 1, 176, 0, 18, 18);
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

		AdvancedSlot slot = getSlotAtLocation(i, j);

		if (slot instanceof TriggerSlot && container.hasTriggers()) {
			TriggerSlot triggerSlot = (TriggerSlot) slot;

			IStatement changed = null;

			if (triggerSlot.getStatement() == null) {

				if (k == 0) {
					changed = container.getFirstTrigger();
				} else {
					changed = container.getLastTrigger();
				}

			} else {
				Iterator<IStatement> it = container.getTriggerIterator(k != 0);

				for (; it.hasNext();) {
					IStatement trigger = it.next();

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
				IStatementParameter parameter = null;
				if (changed != null && p.slot < changed.minParameters()) {
					parameter = changed.createParameter(p.slot);
				}
				container.setTriggerParameter(triggerSlot.slot, p.slot, parameter, true);
			}
		} else if (slot instanceof ActionSlot) {
			ActionSlot actionSlot = (ActionSlot) slot;

			IStatement changed = null;
			if (actionSlot.getStatement() == null) {

				if (k == 0) {
					changed = container.getFirstAction();
				} else {
					changed = container.getLastAction();
				}

			} else {
				Iterator<IStatement> it = container.getActionIterator(k != 0);

				for (; it.hasNext();) {
					IStatement action = it.next();

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
				IStatementParameter parameter = null;
				if (changed != null && p.slot < changed.minParameters()) {
					parameter = changed.createParameter(p.slot);
				}
				container.setActionParameter(actionSlot.slot, p.slot, parameter, true);
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
					param.onClick(gate, statement.getStatement(), mc.thePlayer.inventory.getItemStack(),
							new StatementMouseClick(k, isShiftKeyDown()));
					paramSlot.setParameter(param, true);
				}
			}
		}

		container.markDirty();
	}
}
