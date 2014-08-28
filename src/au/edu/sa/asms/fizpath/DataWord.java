package au.edu.sa.asms.fizpath;

// custom class to store all the relevant data with the corresponding time stamp. To be expanded as more fields are required
public class DataWord {
	long timestamp;	// time (in ms)
	float [] linear = new float [3];	// linear acceleration values
	
	public DataWord(long timestamp, float[] linear){
		this.timestamp = timestamp;
		this.linear = linear;
	}
	
	public DataWord(){
	}
}
