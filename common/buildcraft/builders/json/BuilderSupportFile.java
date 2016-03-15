package buildcraft.builders.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.state.IBlockState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderSupportFile {
	private static final Gson gson;
	private static final Type entryType = new TypeToken<List<BuilderSupportEntry>>(){}.getType();

	public List<BuilderSupportEntry> entries;

	static {
		gson = new Gson();
	}

	private void doInclude(Object dst, Object include) {
		try {
			Object emptyInstance = dst.getClass().newInstance();

			for (Field f : dst.getClass().getDeclaredFields()) {
				if (f.getAnnotation(IncludeIgnore.class) != null) {
					continue;
				} else {
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}

					if (f.getAnnotation(IncludeRecurse.class) != null) {
						if (f.get(include) != null) {
							if (f.get(dst) != null) {
								doInclude(f.get(dst), f.get(include));
							} else {
								f.set(dst, f.get(include));
							}
						}
					} else {
						if (List.class.isAssignableFrom(f.getType())) {
							List includeList = (List) f.get(include);

							if (includeList != null) {
								List dstList = (List) f.get(dst);

								if (dstList == null) {
									f.set(dst, includeList);
								} else {
									for (Object o : includeList) {
										if (!dstList.contains(o)) {
											dstList.add(o);
										}
									}
								}
							}
						} else if (f.get(dst) == f.get(emptyInstance)) {
							f.set(dst, f.get(include));
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

	public BuilderSupportFile(String filename, ClassLoader classLoader, String blockName) throws JSONValidationException, FileNotFoundException {
		InputStream stream = classLoader.getResourceAsStream(filename);
		if (stream == null) {
			throw new FileNotFoundException(filename);
		}

		entries = gson.fromJson(new InputStreamReader(stream), entryType);

		if (entries == null) {
			throw new JSONValidationException(null, "Invalid JSON file!");
		} else if (entries.size() == 0) {
			throw new JSONValidationException(null, "JSON file has no valid entries!");
		}

		Iterator<BuilderSupportEntry> entryIterator = entries.iterator();

		while (entryIterator.hasNext()) {
			BuilderSupportEntry e = entryIterator.next();

			if (e.includes != null) {
				while (e.includes.size() > 0) {
					List<String> includesCopy = new ArrayList<String>();
					includesCopy.addAll(e.includes);
					e.includes.clear();

					for (String includeName : includesCopy) {
						System.out.println("TEST: " + getRelativePath(filename, includeName));
						InputStream includeStream = classLoader.getResourceAsStream(getRelativePath(filename, includeName));
						if (includeStream == null) {
							throw new FileNotFoundException(getRelativePath(filename, includeName));
						}

						List<BuilderSupportEntry> includedEntryList = gson.fromJson(new InputStreamReader(includeStream), entryType);
						if (includedEntryList == null) {
							throw new JSONValidationException(e, "Invalid include file!");
						} else if (includedEntryList.size() != 1) {
							throw new JSONValidationException(e, "Include file must only have one entry!");
						}

						BuilderSupportEntry include = includedEntryList.get(0);
						doInclude(e, include);
					}
				}
			}

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

	private String getRelativePath(String filename, String includeName) {
		if (includeName.startsWith("assets/")) {
			return includeName;
		}

		File f = new File(filename.substring(0, filename.lastIndexOf("/")), includeName);
		String path = f.getPath();
		return path.startsWith("/") ? path.substring(1) : path;
	}

	public boolean isValidForState(IBlockState state) {
		for (BuilderSupportEntry e : entries) {
			if (e.isValidForState(state)) {
				return true;
			}
		}

		return false;
	}

	public BuilderSupportEntry getEntryForSchematic(SchematicJSON s) {
		IBlockState state = s.state;
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

			if (e.isValidForState(state) && e.isValidForTile(tileId)) {
				return e;
			}
		}

		return null;
	}
}
