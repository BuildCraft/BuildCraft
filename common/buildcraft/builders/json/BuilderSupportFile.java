package buildcraft.builders.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicTile;

public class BuilderSupportFile {
	private static final Gson gson;
	private static final Type entryType = new TypeToken<List<BuilderSupportEntry>>(){}.getType();

	public List<BuilderSupportEntry> entries;

	static {
		gson = new Gson();
	}

	public BuilderSupportFile(InputStream stream, String blockName) throws JSONValidationException {
		entries = gson.fromJson(new InputStreamReader(stream), entryType);

		if (entries == null) {
			throw new JSONValidationException(null, "Invalid JSON file!");
		} else if (entries.size() == 0) {
			throw new JSONValidationException(null, "JSON file has no valid entries!");
		}

		Iterator<BuilderSupportEntry> entryIterator = entries.iterator();

		while (entryIterator.hasNext()) {
			BuilderSupportEntry e = entryIterator.next();

			if (blockName != null) {
				if (e.name != null && !e.name.equals(blockName)) {
					throw new JSONValidationException(e, "JSON file is specific to block " + blockName + ", but contains different block " + e.name + "!");
				} else if (e.names == null && e.name == null) {
					e.name = blockName;
				}
			}
			e.listPos = entries.indexOf(e);
			try {
				e.validate(e);
			} catch (Exception ee) {
				ee.printStackTrace();
				entryIterator.remove();
			}
		}
	}

	public boolean isValidForMeta(int metadata) {
		for (BuilderSupportEntry e : entries) {
			if (e.isValidForMeta(metadata)) {
				return true;
			}
		}

		return false;
	}

	public BuilderSupportEntry getEntryForSchematic(SchematicJSON s) {
		int metadata = s.meta;
		String tileId = null;

		if (s.tileNBT != null && s.tileNBT.hasKey("id", 8)) {
			tileId = s.tileNBT.getString("id");
		}

		for (BuilderSupportEntry e : entries) {
			if (s.entryName != null) {
				if (e.names != null) {
					if (!e.names.contains(s.entryName)) {
						continue;
					}
				} else if (!e.name.equals(s.entryName)) {
					continue;
				}
			}

			if (e.isValidForMeta(metadata) && e.isValidForTile(tileId)) {
				return e;
			}
		}

		return null;
	}
}
