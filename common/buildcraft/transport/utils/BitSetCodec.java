package buildcraft.transport.utils;

import java.util.BitSet;

public class BitSetCodec {
	public byte encode(BitSet set){
		byte result = 0;
		for (byte i = 0; i < 8; i++ ){
			result <<= 1; 
			result |= set.get(i) ? 1 : 0;
		}
		return result;
	}
	
	public void decode(byte data, BitSet target){
		target.clear();
		for (byte i = 0; i < 8; i++){
			target.set(7-i, (data & 1) != 0); 
			data >>= 1;
		}
	}
}
