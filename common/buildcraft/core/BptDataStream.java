/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class BptDataStream implements DataInput, DataOutput {

	Writer writer;
	Reader reader;

	public BptDataStream(Writer writer) {
		this.writer = writer;
	}

	public BptDataStream(Reader reader) {
		this.reader = reader;
	}

	public boolean isFirst = true;

	@Override
	public void readFully(byte[] b) throws IOException {
		char[] c = new char[b.length];

		reader.read(c);

		for (int i = 0; i < b.length; ++i)
			b[i] = (byte) c[i];
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int skipBytes(int n) throws IOException {
		return reader.read(new char[n]);
	}

	@Override
	public boolean readBoolean() throws IOException {
		String s = readUTF();

		return "T".equals(s);
	}

	@Override
	public byte readByte() throws IOException {
		return (byte) readLong();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return (short) readLong();
	}

	@Override
	public short readShort() throws IOException {
		return (short) readLong();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return (int) readLong();
	}

	@Override
	public char readChar() throws IOException {
		return readUTF().charAt(0);
	}

	@Override
	public int readInt() throws IOException {
		return (int) readLong();
	}

	@Override
	public long readLong() throws IOException {
		StringBuilder builder = new StringBuilder();

		boolean exit = false;

		while (!exit) {
			int i = reader.read();
			if (i < 0)
				break;

			char c = (char) i;

			switch (c) {
			case ',':
				exit = true;
				break;
			default:
				builder.append(c);
			}
		}

		return Long.parseLong(builder.toString());
	}

	@Override
	public float readFloat() throws IOException {
		return (float) readDouble();
	}

	@Override
	public double readDouble() throws IOException {
		StringBuilder builder = new StringBuilder();

		boolean exit = false;

		while (!exit) {
			int i = reader.read();
			if (i < 0)
				break;

			char c = (char) i;

			switch (c) {
			case ',':
				exit = true;
				break;
			default:
				builder.append(c);
			}
		}

		return Double.parseDouble(builder.toString());
	}

	@Override
	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readUTF() throws IOException {
		StringBuilder builder = new StringBuilder();

		boolean exit = false;

		char c = (char) reader.read();

		if (c != '\"')
			throw new IOException("String does not start with '\"' character");

		while (reader.ready() && !exit) {
			c = (char) reader.read();

			switch (c) {
			case '\\':
				c = (char) reader.read();

				switch (c) {
				case 'n':
					builder.append('\n');
				case 'r':
					builder.append('\r');
				case '\\':
					builder.append('\\');
				case '\"':
					builder.append('\"');
				}
			case '"':
				exit = true;
				break;
			default:
				builder.append(c);
			}
		}

		int i = reader.read();
		c = (char) i;

		if (c != ',' && i > 0)
			throw new IOException("Missing ',' at end of attribute");

		return builder.toString();
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		if (v)
			writeUTF("T");
		else
			writeUTF("F");
	}

	@Override
	public void writeByte(int v) throws IOException {
		writeLong(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		writeLong(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		writeUTF(((char) v) + "");

	}

	@Override
	public void writeInt(int v) throws IOException {
		writeLong(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		handleWriteComma();

		writer.append(Long.toString(v));
	}

	@Override
	public void writeFloat(float v) throws IOException {
		writeDouble(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		handleWriteComma();

		writer.append(Double.toString(v));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		writeUTF(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		writeUTF(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		handleWriteComma();

		writer.write("\"");

		for (char c : s.toCharArray())
			switch (c) {
			case '\n':
				writer.write("\\n");
			case '\r':
				writer.write("\\r");
			case '\"':
				writer.write("\\\"");
			case '\\':
				writer.write("\\\\");
			default:
				writer.write(c);
			}

		writer.write("\"");
	}

	private void handleWriteComma() throws IOException {
		if (!isFirst)
			writer.append(",");

		isFirst = false;
	}

}
