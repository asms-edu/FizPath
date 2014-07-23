package au.edu.sa.asms.fizpath;

import android.widget.Toast;

public class MotionFunctions{
	
	public static int TrimmedColour (double LowerBound, double UpperBound, double CurrentValue) {
//		This function maps a provided value as a linear proportion between two provided bounds across the range 0-255.
//		Any result outside 0-255 is "trimmed" to 0 or 255 as appropriate
		long trimmed = Math.round(CurrentValue*(255/(UpperBound-LowerBound)+127));

//		ensure the value is within the legal range of 0-255				
		if (trimmed>255){
			trimmed=255;
			
		}
		if (trimmed<0){
			trimmed=0;
		}	
			
		return (int) (long) trimmed;
		
	}
	
	// calculate a new velocity value given previous velocity, acceleration (and calibration value) and time interval
	public static double[] updateVelocity (double calibration[], DataWord thisTime, DataWord lastTime){
		double [] currentVel;
		currentVel = new double[3];
		currentVel[0] = 0;
		currentVel[1] =	thisTime.linearVelocity[1]+(thisTime.linearAccel[1]-calibration[1])*((thisTime.timestamp-lastTime.timestamp)/1000);
		currentVel[2] = 0;
		return currentVel;
	}
	// calculate a new displacement value given previous displacement, current velocity and time interval	
	public static double [] updatePosition (DataWord thisTime, DataWord lastTime){
		double[] currentDisplacement = lastTime.linearDisplacement;
		currentDisplacement[1]=currentDisplacement[1]+(thisTime.linearVelocity[1]*(thisTime.timestamp-lastTime.timestamp)/1000);
		return currentDisplacement;
	}
}