package net.minecraft.src.buildcraft;

public class Position {
	// FIXME: double is probably not the way to go here - we may have two 
	// versions of this, for double and for int
	double i, j, k;
	int orientation;
	
	public Position (double ci, double cj, double ck, int corientation) {
		i = ci;
		j = cj;
		k = ck;
		orientation = corientation;
	}
	
	public Position (Position p) {
		i = p.i;
		j = p.j;
		k = p.k;
		orientation = p.orientation;
	}
	
	public void moveRight (double step) {
		switch (orientation) {
		case 2:
			i = i - step;
			break;
		case 3:
			i = i + step;    			
			break;
		case 4:
			k = k + step;
			break;
		case 5:
			k = k - step;
			break;
		}
	}
	
	public void moveLeft (double step) {
		moveRight(-step);
	}
	
	public void moveForwards (double step) {
		switch (orientation) {
		case 2:
			k = k + step;
			break;
		case 3:
			k = k - step;	
			break;
		case 4:
			i = i + step;
			break;		
		case 5:
			i = i - step;
			break;
		}
	}	
	
	public void moveBackwards (double step) {
		moveForwards(-step);
	}
	
	public void moveUp (double step) {
		switch (orientation) {
		case 2: case 3: case 4: case 5:
			j = j + step;
			break;
		}
		
	}
	
	public void moveDown (double step) {
		moveUp (-step);
	}
	
	public void reverseOrientation () {
		switch (orientation) {
		case 0:
			orientation = 1;
			break;
		case 1:
			orientation = 0;
			break;
		case 2:
			orientation = 3;
			break;
		case 3:
			orientation = 2;
			break;
		case 4:
			orientation = 5;
			break;
		case 5:
			orientation = 4;
			break;
		}
	}
	
}