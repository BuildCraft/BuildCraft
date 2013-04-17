package buildcraft.transport.utils;

import java.util.BitSet;

public class BitSetCodec {
	public byte encode(BitSet set){
		byte result = 0;
		for (byte i = 0; i < set.length() && i < 8; i++ ){
			if (set.get(i)){
				result |= 0x1 << i;	
			}
		}
		return result;
	}
	
	public void decode(byte data, BitSet target){
		target.clear();
		for (byte i = 0; i < target.length() && i < 8; i++){
			target.set(i, (data & 0x1 >>> i) > 0);
		}
	}
}
