package au.edu.sa.asms.fizpath;

public class DataWord {
	long timestamp;
	float [] linear = new float [3];

	DataWord(){
		timestamp=0;
		linear[0] = (float) 0.0;
		linear[1] = (float) 0.0;
		linear[2] = (float) 0.0;
	}
}
