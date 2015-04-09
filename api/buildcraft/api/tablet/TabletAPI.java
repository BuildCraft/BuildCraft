package buildcraft.api.tablet;

import java.util.HashMap;
import java.util.Map;

public class TabletAPI {
	private TabletAPI() {

	}

	private static final Map<String, TabletProgramFactory> programs = new HashMap<String, TabletProgramFactory>();

	public static void registerProgram(TabletProgramFactory factory) {
		programs.put(factory.getName(), factory);
	}

	public static TabletProgramFactory getProgram(String name) {
		return programs.get(name);
	}
}
