/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.blueprints;

import buildcraft.BuildCraftCore;
import buildcraft.core.Box;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BCLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public abstract class BptBase {

	BptSlot contents[][][];

	public int position;

	public int anchorX, anchorY, anchorZ;
	public int sizeX, sizeY, sizeZ;

	protected String name;

	public String author;

	public File file;

	protected String version = "";

	public BptBase() {
	}

	public BptBase(int sizeX, int sizeY, int sizeZ) {
		contents = new BptSlot[sizeX][sizeY][sizeZ];

		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;

		anchorX = 0;
		anchorY = 0;
		anchorZ = 0;
	}

	public void setBlockId(int x, int y, int z, int blockId) {
		if (contents[x][y][z] == null) {
			contents[x][y][z] = new BptSlot();
			contents[x][y][z].x = x;
			contents[x][y][z].y = y;
			contents[x][y][z].z = z;
		}

		contents[x][y][z].blockId = blockId;
	}

	public void rotateLeft(BptContext context) {
		BptSlot newContents[][][] = new BptSlot[sizeZ][sizeY][sizeX];

		for (int x = 0; x < sizeZ; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeX; ++z) {
					newContents[x][y][z] = contents[z][y][(sizeZ - 1) - x];

					if (newContents[x][y][z] != null) {
						try {
							newContents[x][y][z].rotateLeft(context);
						} catch (Throwable t) {
							// Defensive code against errors in implementers
							t.printStackTrace();
							BCLog.logger.throwing("BptBase", "rotateLeft", t);
						}
					}
				}
			}
		}

		int newAnchorX, newAnchorY, newAnchorZ;

		newAnchorX = (sizeZ - 1) - anchorZ;
		newAnchorY = anchorY;
		newAnchorZ = anchorX;

		contents = newContents;
		int tmp = sizeX;
		sizeX = sizeZ;
		sizeZ = tmp;

		anchorX = newAnchorX;
		anchorY = newAnchorY;
		anchorZ = newAnchorZ;

		context.rotateLeft();
	}

	public File save() {
		try {
			File baseDir = new File("./");

			baseDir.mkdir();

			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream output = new FileOutputStream(file);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "8859_1"));

			writer.write("version:" + Version.VERSION);
			writer.newLine();

			if (this instanceof BptTemplate) {
				writer.write("kind:template");
			} else {
				writer.write("kind:blueprint");
			}
			writer.newLine();

			writer.write("sizeX:" + sizeX);
			writer.newLine();
			writer.write("sizeY:" + sizeY);
			writer.newLine();
			writer.write("sizeZ:" + sizeZ);
			writer.newLine();
			writer.write("anchorX:" + anchorX);
			writer.newLine();
			writer.write("anchorY:" + anchorY);
			writer.newLine();
			writer.write("anchorZ:" + anchorZ);
			writer.newLine();
			writer.write("name:" + name);
			writer.newLine();

			if (author != null) {
				writer.write("author:" + author);
				writer.newLine();
			}

			saveAttributes(writer);

			writer.newLine();
			writer.flush();
			output.close();

			return file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o.getClass() != getClass()))
			return false;

		BptBase bpt = (BptBase) o;

		if (sizeX != bpt.sizeX || sizeY != bpt.sizeY || sizeZ != bpt.sizeZ || anchorX != bpt.anchorX || anchorY != bpt.anchorY || anchorZ != bpt.anchorZ)
			return false;

		for (int x = 0; x < contents.length; ++x) {
			for (int y = 0; y < contents[0].length; ++y) {
				for (int z = 0; z < contents[0][0].length; ++z) {
					if (contents[x][y][z] != null && bpt.contents[x][y][z] == null)
						return false;

					if (contents[x][y][z] == null && bpt.contents[x][y][z] != null)
						return false;

					if (contents[x][y][z] == null && bpt.contents[x][y][z] == null) {
						continue;
					}

					if (contents[x][y][z].blockId != bpt.contents[x][y][z].blockId)
						return false;
				}
			}
		}

		return true;
	}

	public static BptBase loadBluePrint(File file, int position) {
		BptBase result = null;

		String version = null;

		try {
			FileInputStream input = new FileInputStream(file);

			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "8859_1"));

			while (true) {
				String line = reader.readLine();

				if (line == null) {
					break;
				}

				String[] cpts = line.split(":");
				String attr = cpts[0];

				if (attr.equals("kind")) {
					if (cpts[1].equals("template")) {
						result = new BptTemplate();
					} else if (cpts[1].equals("blueprint")) {
						result = new BptBlueprint();
					}

					result.position = position;
					result.version = version;
					result.file = file;
				} else if (attr.equals("sizeX")) {
					result.sizeX = Integer.parseInt(cpts[1]);
				} else if (attr.equals("sizeY")) {
					result.sizeY = Integer.parseInt(cpts[1]);
				} else if (attr.equals("sizeZ")) {
					result.sizeZ = Integer.parseInt(cpts[1]);
				} else if (attr.equals("anchorX")) {
					result.anchorX = Integer.parseInt(cpts[1]);
				} else if (attr.equals("anchorY")) {
					result.anchorY = Integer.parseInt(cpts[1]);
				} else if (attr.equals("anchorZ")) {
					result.anchorZ = Integer.parseInt(cpts[1]);
				} else if (attr.equals("name")) {
					result.name = cpts[1];
				} else if (attr.equals("version")) {
					if (result != null) {
						result.version = version;
					} else {
						version = cpts[1];
					}
				} else if (attr.equals("author")) {
					result.author = cpts[1];
				} else if (result != null)
					if (cpts.length >= 2) {
						result.loadAttribute(reader, attr, cpts[1]);
					} else {
						result.loadAttribute(reader, attr, "");
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		if (name == null)
			return "#" + position;
		else
			return name;
	}

	@Override
	public final BptBase clone() {
		BptBase res = null;

		try {
			res = getClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();

			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();

			return null;
		}

		res.anchorX = anchorX;
		res.anchorY = anchorY;
		res.anchorZ = anchorZ;

		res.sizeX = sizeX;
		res.sizeY = sizeY;
		res.sizeZ = sizeZ;

		res.position = position;
		res.name = name;
		res.author = author;

		res.contents = new BptSlot[sizeX][sizeY][sizeZ];

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z)
					if (contents[x][y][z] != null) {
						res.contents[x][y][z] = contents[x][y][z].clone();
					}
			}
		}

		copyTo(res);

		return res;
	}

	protected void copyTo(BptBase base) {

	}

	public Box getBoxForPos(int x, int y, int z) {
		int xMin = x - anchorX;
		int yMin = y - anchorY;
		int zMin = z - anchorZ;
		int xMax = x + sizeX - anchorX - 1;
		int yMax = y + sizeY - anchorY - 1;
		int zMax = z + sizeZ - anchorZ - 1;

		Box res = new Box();
		res.initialize(xMin, yMin, zMin, xMax, yMax, zMax);
		res.reorder();

		return res;
	}

	public abstract void loadAttribute(BufferedReader reader, String attr, String val) throws IOException, BptError;

	public abstract void saveAttributes(BufferedWriter writer) throws IOException;
}
