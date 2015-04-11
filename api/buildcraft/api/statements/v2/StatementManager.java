/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.statements.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.v2.tile.IOverrideStatements;

public final class StatementManager {
	public static Map<String, Statement> statements = new HashMap<String, Statement>();
	public static Map<String, Class<? extends StatementParameter>> parameters = new HashMap<String, Class<? extends StatementParameter>>();
	private static List<ITriggerProvider> triggerProviders = new LinkedList<ITriggerProvider>();
	private static List<IActionProvider> actionProviders = new LinkedList<IActionProvider>();

	/**
	 * Deactivate constructor
	 */
	private StatementManager() {
	}

	public static void registerTriggerProvider(ITriggerProvider provider) {
		if (provider != null && !triggerProviders.contains(provider)) {
			triggerProviders.add(provider);
		}
	}

	public static void registerActionProvider(IActionProvider provider) {
		if (provider != null && !actionProviders.contains(provider)) {
			actionProviders.add(provider);
		}
	}

	public static void registerStatement(Statement statement) {
		statements.put(statement.getUniqueTag(), statement);
	}

	public static void registerParameterClass(Class<? extends StatementParameter> param) {
		parameters.put(createParameter(param).getUniqueTag(), param);
	}

	@Deprecated
	public static void registerParameterClass(String name, Class<? extends StatementParameter> param) {
		parameters.put(name, param);
	}

	public static List<Trigger> getExternalTriggers(ForgeDirection side, TileEntity entity) {
		List<Trigger> result;

		result = new LinkedList<Trigger>();

		for (ITriggerProvider provider : triggerProviders) {
			Collection<Trigger> toAdd = provider.getExternalTriggers(side, entity);

			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		if (entity instanceof IOverrideStatements) {
			((IOverrideStatements) entity).overrideTriggers(result);
		}

		return result;
	}

	public static List<Action> getExternalActions(ForgeDirection side, TileEntity entity) {
		List<Action> result = new LinkedList<Action>();

		for (IActionProvider provider : actionProviders) {
			Collection<Action> toAdd = provider.getExternalActions(side, entity);

			if (toAdd != null) {
				for (Action t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		if (entity instanceof IOverrideStatements) {
			((IOverrideStatements) entity).overrideActions(result);
		}

		return result;
	}

	public static List<Trigger> getInternalTriggers(IStatementContainer container) {
		List<Trigger> result = new LinkedList<Trigger>();

		for (ITriggerProvider provider : triggerProviders) {
			Collection<Trigger> toAdd = provider.getInternalTriggers(container);

			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		if (container.getTile() instanceof IOverrideStatements) {
			((IOverrideStatements) container.getTile()).overrideTriggers(result);
		}

		return result;
	}

	public static List<Action> getInternalActions(IStatementContainer container) {
		List<Action> result = new LinkedList<Action>();

		for (IActionProvider provider : actionProviders) {
			Collection<Action> toAdd = provider.getInternalActions(container);

			if (toAdd != null) {
				for (Action t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		if (container.getTile() instanceof IOverrideStatements) {
			((IOverrideStatements) container.getTile()).overrideActions(result);
		}

		return result;
	}

	public static StatementParameter createParameter(String kind) {
		return createParameter(parameters.get(kind));
	}
	
	private static StatementParameter createParameter(Class<? extends StatementParameter> param) {
		try {
			return param.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Generally, this function should be called by every mod implementing
	 * the Statements API ***as a container*** (that is, adding its own gates)
	 * on the client side from a given Item of choice.
	 */
	@SideOnly(Side.CLIENT)
	public static void registerIcons(IIconRegister register) {
		for (Statement statement : statements.values()) {
			statement.registerIcons(register);
		}

		for (Class<? extends StatementParameter> parameter : parameters.values()) {
			createParameter(parameter).registerIcons(register);
		}
	}
}
