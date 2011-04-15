package net.minecraft.src.buildcraft;


public class Utils {
	public static int getOrientation (Position pos1, Position pos2) {
		double Dx = pos1.i - pos2.i;
    	double Dz = pos1.k - pos2.k;
    	double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;
    	
    	int orientation = 0;    	
    	
    	if (angle < 45 || angle > 315) {
    		orientation = 4;
    	} else if (angle < 135) {
    		orientation = 2;
    	} else if (angle < 225) {
    		orientation = 5;
    	} else {
    		orientation = 3;
    	}
    	
    	return orientation;    	
	}
}
