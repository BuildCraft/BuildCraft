package net.minecraft.src.buildcraft;

public class Utils {
	public static int get3dOrientationBetweenBlock (Position pos1, Position pos2) {
		return 0;
	}
	
	public static Orientations get2dOrientation (Position pos1, Position pos2) {
		double Dx = pos1.i - pos2.i;
    	double Dz = pos1.k - pos2.k;
    	double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;    	
    	
    	if (angle < 45 || angle > 315) {
    		return Orientations.XPos;
    	} else if (angle < 135) {
    		return Orientations.ZPos;
    	} else if (angle < 225) {
    		return Orientations.XNeg;
    	} else {
    		return Orientations.ZNeg;
    	}    	    	    	
	}	
	
	public static Orientations get3dOrientation (Position pos1, Position pos2) {
		double Dx = pos1.i - pos2.i;
    	double Dy = pos1.j - pos2.j;
    	double angle = Math.atan2(Dy, Dx) / Math.PI * 180 + 180;
    	
    	System.out.println ("3D ANGLE = " + angle);
    	
    	if (angle <= 90) {
    		return Orientations.YPos;
    	} else if (angle >= 270) {
    		return Orientations.YNeg;
    	} else {
    		return get2dOrientation(pos1, pos2);
    	}    	
	}
}
