package net.minecraft.src.buildcraft;

public class Position {
	// FIXME: double is probably not the way to go here - we may have two 
	// versions of this, for double and for int
	double i, j, k;
	Orientations orientation;
	
	public Position (double ci, double cj, double ck) {
		i = ci;
		j = cj;
		k = ck;
		orientation = Orientations.Unknown;
	}
	
	public Position (double ci, double cj, double ck, Orientations corientation) {
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
		case ZPos:
			i = i - step;
			break;
		case ZNeg:
			i = i + step;    			
			break;
		case XPos:
			k = k + step;
			break;
		case XNeg:
			k = k - step;
			break;
		}
	}
	
	public void moveLeft (double step) {
		moveRight(-step);
	}
	
	public void moveForwards (double step) {
		switch (orientation) {
		case YPos:
			j = j + step;
			break;
		case YNeg:
			j = j - step;
			break;
		case ZPos:
			k = k + step;
			break;
		case ZNeg:
			k = k - step;	
			break;
		case XPos:
			i = i + step;
			break;		
		case XNeg:
			i = i - step;
			break;
		}
	}	
	
	public void moveBackwards (double step) {
		moveForwards(-step);
	}
	
	public void moveUp (double step) {
		switch (orientation) {
		case ZPos: case ZNeg: case XPos: case XNeg:
			j = j + step;
			break;
		}
		
	}
	
	public void moveDown (double step) {
		moveUp (-step);
	}
	
	public void reverseOrientation () {
		switch (orientation) {
		case YPos:
			orientation = Orientations.YNeg;
			break;
		case YNeg:
			orientation = Orientations.YPos;
			break;
		case ZPos:
			orientation = Orientations.ZNeg;
			break;
		case ZNeg:
			orientation = Orientations.ZPos;
			break;
		case XPos:
			orientation = Orientations.XNeg;
			break;
		case XNeg:
			orientation = Orientations.XPos;
			break;
		}
	}
	
}