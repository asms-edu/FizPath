package au.edu.sa.asms.fizpath;

public class MotionFunctions{
	
	public static int TrimmedColour (double LowerBound, double UpperBound, double CurrentValue) {

//		comment here later
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
	
	public static double updateVelocity (float accelValues[], double mainCal, double currentVel, long deltaT){
		currentVel=currentVel+((accelValues[1]-mainCal)*deltaT/1000);
		return currentVel;
	}
	
	public static double updatePosition (double currentVel, double currentDisplacement, long deltaT){
		currentDisplacement=currentDisplacement+(currentVel*deltaT/1000);
		return currentDisplacement;
	}
}