package buildcraft.core.utils;

public class SessionVars {

	@SuppressWarnings("rawtypes")
	private static Class openedLedger;

	@SuppressWarnings("rawtypes")
	public static void setOpenedLedger(Class ledgerClass) {
		openedLedger = ledgerClass;
	}

	@SuppressWarnings("rawtypes")
	public static Class getOpenedLedger() {
		return openedLedger;
	}
}
