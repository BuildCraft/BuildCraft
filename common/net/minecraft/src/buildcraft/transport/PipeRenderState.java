package net.minecraft.src.buildcraft.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.transport.utils.ConnectionMatrix;
import net.minecraft.src.buildcraft.transport.utils.TextureMatrix;
import net.minecraft.src.buildcraft.transport.utils.WireMatrix;

public class PipeRenderState {

	private String textureFile = DefaultProps.TEXTURE_BLOCKS;
	private boolean hasGate = false;
	private int gateTextureIndex = 0;
	
	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final WireMatrix wireMatrix = new WireMatrix();
	
	private boolean dirty = false;
	
	
	/**This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will
	 * then return the the WorldRenderer  
     */
	public int currentTextureIndex;
		
	
	public void setTextureFile(String textureFile){
		if (this.textureFile != textureFile){
			this.textureFile = textureFile;
			this.dirty = true;
		}
	}
	
	public String getTextureFile(){
		return this.textureFile;
	}
	
	public void setHasGate(boolean value){
		if (hasGate != value){
			hasGate = value;
			dirty = true;
		}
	}
	
	public boolean hasGate(){
		return hasGate;
	}
	
	public void setGateTexture(int value){
		if (gateTextureIndex != value){
			gateTextureIndex = value;
			dirty = true;
		}
	}
	
	public int getGateTextureIndex(){
		return gateTextureIndex;
	}

	public void clean(){
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		wireMatrix.clean();
	}

	public boolean isDirty(){
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || wireMatrix.isDirty();
	}

	public void writeData(DataOutputStream data) throws IOException {
		data.writeUTF(textureFile);
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		wireMatrix.writeData(data);
	}

	public void readData(DataInputStream data) throws IOException {
		textureFile = data.readUTF();
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		wireMatrix.readData(data);
	}
	
}
