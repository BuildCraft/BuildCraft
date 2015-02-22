/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.statements;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public final class StatementManager {

	public static Map<String, IStatement> statements = new HashMap<String, IStatement>();
	public static Map<String, Class<? extends IStatementParameter>> parameters = new HashMap<String, Class<? extends IStatementParameter>>();
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

	public static void registerStatement(IStatement statement) {
		statements.put(statement.getUniqueTag(), statement);
	}

	public static void registerParameterClass(Class<? extends IStatementParameter> param) {
		parameters.put(createParameter(param).getUniqueTag(), param);
	}
	
	@Deprecated
	public static void registerParameterClass(String name, Class<? extends IStatementParameter> param) {
		parameters.put(name, param);
	}

	public static List<ITriggerExternal> getExternalTriggers(EnumFacing side, TileEntity entity) {
		List<ITriggerExternal> result;

		if (entity instanceof IOverrideDefaultStatements) {
			result = ((IOverrideDefaultStatements) entity).overrideTriggers();
			if (result != null) {
				return result;
			}
		}
		
		result = new LinkedList<ITriggerExternal>();
		
		for (ITriggerProvider provider : triggerProviders) {
			Collection<ITriggerExternal> toAdd = provider.getExternalTriggers(side, entity);

			if (toAdd != null) {
				for (ITriggerExternal t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		return result;
	}

	public static List<IActionExternal> getExternalActions(EnumFacing side, TileEntity entity) {
		List<IActionExternal> result = new LinkedList<IActionExternal>();

		if (entity instanceof IOverrideDefaultStatements) {
			result = ((IOverrideDefaultStatements) entity).overrideActions();
			if (result != null) {
				return result;
			} else {
				result = new LinkedList<IActionExternal>();
			}
		}
		
		for (IActionProvider provider : actionProviders) {
			Collection<IActionExternal> toAdd = provider.getExternalActions(side, entity);

			if (toAdd != null) {
				for (IActionExternal t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		return result;
	}

	public static List<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		List<ITriggerInternal> result = new LinkedList<ITriggerInternal>();

		for (ITriggerProvider provider : triggerProviders) {
			Collection<ITriggerInternal> toAdd = provider.getInternalTriggers(container);

			if (toAdd != null) {
				for (ITriggerInternal t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		return result;
	}

	public static List<IActionInternal> getInternalActions(IStatementContainer container) {
		List<IActionInternal> result = new LinkedList<IActionInternal>();

		for (IActionProvider provider : actionProviders) {
			Collection<IActionInternal> toAdd = provider.getInternalActions(container);

			if (toAdd != null) {
				for (IActionInternal t : toAdd) {
					if (!result.contains(t)) {
						result.add(t);
					}
				}
			}
		}

		return result;
	}

	public static IStatementParameter createParameter(String kind) {
		return createParameter(parameters.get(kind));
	}
	
	private static IStatementParameter createParameter(Class<? extends IStatementParameter> param) {
		try {
			return param.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}
}
