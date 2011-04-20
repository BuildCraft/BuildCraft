package net.minecraft.src.buildcraft;

public class BlockIndex implements Comparable<BlockIndex> {
	int i;
	int j;
	int k;	

	public BlockIndex (int ci, int cj, int ck) {
		i = ci;
		j = cj;
		k = ck;
	}
	
	public int compareTo(BlockIndex o) {
		if (o.i < i) {
			return 1;
		} else if (o.i > i) {
			return -1;
		} else if (o.j < j) {
			return 1;
		} else if (o.j > j) {
			return -1;
		} else if (o.k < k) {
			return 1;
		} else if (o.k > k) {
			return -1;
		} else {
			return 0;
		}
	}
}
