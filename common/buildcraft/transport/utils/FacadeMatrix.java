package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class FacadeMatrix extends ConnectionMatrix {
	private Icon[] _textureIcons = new Icon[ForgeDirection.VALID_DIRECTIONS.length];

	private boolean dirty = false;

	public FacadeMatrix() {
	}


	public Icon getTextureIcon(ForgeDirection direction) {
		return _textureIcons[direction.ordinal()];
	}

	public void setTextureIcon(ForgeDirection direction, Icon value) {
		if (_textureIcons[direction.ordinal()] != value) {
			_textureIcons[direction.ordinal()] = value;
			dirty = true;
		}
	}

	@Override
	public boolean isDirty() {
		return dirty || super.isDirty();
	}

	@Override
	public void clean() {
		super.clean();
		dirty = false;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_textureIcons[i] = data.readInt();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeUTF(_textureFiles[i]);
			data.writeInt(_textureIcons[i]);
		}
	}
}
