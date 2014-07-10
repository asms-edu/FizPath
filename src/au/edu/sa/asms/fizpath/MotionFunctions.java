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
	
}