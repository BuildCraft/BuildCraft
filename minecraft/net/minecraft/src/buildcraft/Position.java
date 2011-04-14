package net.minecraft.src.buildcraft;

public class Position {
	int i, j, k;
	int orientation;
	
	public Position (int ci, int cj, int ck, int corientation) {
		i = ci;
		j = cj;
		k = ck;
		orientation = corientation;
	}
	
	public void moveRight (int step) {
		switch (orientation) {
		case 3:
			i = i + step;    			
			break;
		case 4:
			k = k + step;
			break;
		case 2:
			i = i - step;
			break;
		case 5:
			k = k - step;
			break;
		}
	}
	
	public void moveLeft (int step) {
		moveRight(-step);
	}
	
	public void moveForwards (int step) {
		switch (orientation) {
		case 3:
			k = k - step;	
			break;
		case 4:
			i = i + step;
			break;
		case 2:
			k = k + step;
			break;
		case 5:
			i = i - step;
			break;
		}
	}
	
	public void moveBackwards (int step) {
		moveForwards(-step);
	}
}