package au.edu.sa.asms.fizpath;

// custom class to store all the relevant data with the corresponding time stamp. To be expanded as more fields are required
public class DataWord {
	long timestamp;	// time (in ms)
	float [] linearAccel = new float [3];			// linear acceleration values (m/s/s)
	double [] linearVelocity = new double [3];		// linear velocity values (m/s)
	double [] linearDisplacement = new double [3];	// linear displacement (m)
}
