package buildcraft.builders.json;

public class JSONValidationException extends Exception {
	public JSONValidationException(BuilderSupportEntry e, String s) {
		super(e == null ? s : ("[" + e.listPos + ": " + (e.names != null ? e.names.get(0) : e.name) + "] " + s));
	}
}
