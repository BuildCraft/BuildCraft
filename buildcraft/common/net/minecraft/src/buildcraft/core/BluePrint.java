package net.minecraft.src.buildcraft.core;

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

import net.minecraft.src.mod_BuildCraftCore;

public class BluePrint {
	
	
	File file;
	BlockContents contents [][][];	
	
	public int anchorX, anchorY, anchorZ;
	public int sizeX, sizeY, sizeZ;
	
	public BluePrint (File file) {
		this.file = file;
	}
	
	public BluePrint (BluePrint src) {
		src.loadIfNeeded();
		
		anchorX = src.anchorX;
		anchorY = src.anchorY;
		anchorZ = src.anchorZ;
		
		sizeX = src.sizeX;
		sizeY = src.sizeY;
		sizeZ = src.sizeZ;
		
		contents = new BlockContents [sizeX][sizeY][sizeZ];
		
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					contents [x][y][z] = src.contents [x][y][z];
				}
			}
		}
	}
	
	public BluePrint (int sizeX, int sizeY, int sizeZ) {
		contents = new BlockContents [sizeX][sizeY][sizeZ];
		
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		anchorX = 0;
		anchorY = 0;
		anchorZ = 0;
	}
	
	public void setBlockId (int x, int y, int z, int blockId) {
		if (contents [x][y][z] == null) {
			contents [x][y][z] = new BlockContents ();
			contents [x][y][z].x = x;
			contents [x][y][z].y = y;
			contents [x][y][z].z = z;
		}
		
		contents [x][y][z].blockId = blockId;
	}
	
	public void rotateLeft () {
		loadIfNeeded();
		BlockContents newContents [][][] = new BlockContents [sizeZ][sizeY][sizeX];
		
		for (int x = 0; x < sizeZ; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeX; ++z) {
					newContents [x][y][z] = contents [z][y][(sizeZ - 1) - x];
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
	}
	
	public void save (int number) {
		loadIfNeeded();
		try {
			File baseDir = CoreProxy.getBuildCraftBase();
			
			baseDir.mkdir();
			
			File file = new File(CoreProxy.getBuildCraftBase(), "blueprints/"
					+ number + ".bpt");
			
			if (!file.exists()) {			
				file.createNewFile();
			}
			
			FileOutputStream output = new FileOutputStream(file);
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					output, "8859_1"));
			
			writer.write("version:" + mod_BuildCraftCore.version());
			writer.newLine();
			writer.write("kind:template");
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
			
			writer.write ("mask:");
			
			boolean first = true;
			
			for (int x = 0; x < sizeX; ++x) {
				for (int y = 0; y < sizeY; ++y) {
					for (int z = 0; z < sizeZ; ++z) {
						if (first) {
							first = false;
						} else {
							writer.write(",");	
						}
						
						writer.write(contents[x][y][z].blockId + "");
					}
				}
			}
			
			writer.newLine();			
			writer.flush();
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadIfNeeded () {
		if (file == null) {
			return;
		}
		
		try {
			FileInputStream input = new FileInputStream(file);
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input, "8859_1"));
			
			while (true) {
				String line = reader.readLine();
				
				if (line == null) {
					break;
				}
				
				String[] cpts = line.split(":");
				String name = cpts [0];
				
				if (name.equals("sizeX")) {
					sizeX = Integer.parseInt(cpts [1]);
				} else if (name.equals("sizeY")) {
					sizeY = Integer.parseInt(cpts [1]);
				} else if (name.equals("sizeZ")) {
					sizeZ = Integer.parseInt(cpts [1]);
				} else if (name.equals("anchorX")) {
					anchorX = Integer.parseInt(cpts [1]);
				} else if (name.equals("anchorY")) {
					anchorY = Integer.parseInt(cpts [1]);
				} else if (name.equals("anchorZ")) {
					anchorZ = Integer.parseInt(cpts [1]);
				} else if (name.equals("mask")) {
					contents = new BlockContents [sizeX][sizeY][sizeZ];
					
					String [] mask = cpts [1].split(",");
					int maskIndex = 0;
					
					for (int x = 0; x < sizeX; ++x) {
						for (int y = 0; y < sizeY; ++y) {
							for (int z = 0; z < sizeZ; ++z) {
								contents[x][y][z] = new BlockContents();
								contents[x][y][z].x = x;
								contents[x][y][z].y = y;
								contents[x][y][z].z = z;
								contents[x][y][z].blockId = Integer
										.parseInt(mask[maskIndex]);
								
								maskIndex++;
							}
						}
					}
				}								
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		file = null;
	}
	
	@Override
	public boolean equals (Object o) {
		loadIfNeeded();
		if (!(o instanceof BluePrint)) {
			return false;
		}
		
		BluePrint bpt = (BluePrint) o;
		bpt.loadIfNeeded();
		
		if (sizeX != bpt.sizeX
				|| sizeY != bpt.sizeY
				|| sizeZ != bpt.sizeZ
				|| anchorX != bpt.anchorX
				|| anchorY != bpt.anchorY
				|| anchorZ != bpt.anchorZ) {
			return false;
		}
		
		for (int x = 0; x < contents.length; ++x) {
			for (int y = 0; y < contents [0].length; ++y) {
				for (int z = 0; z < contents [0][0].length; ++z) {
					if (contents [x][y][z] != null && bpt.contents [x][y][z] == null) {
						return false;
					}
					
					if (contents [x][y][z] == null && bpt.contents [x][y][z] != null) {
						return false;
					}
					
					if (contents [x][y][z].blockId != bpt.contents [x][y][z].blockId) {
						return false;
					}
				}
			}
		}
				
		return true;
	}
}
