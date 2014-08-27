package au.edu.sa.asms.fizpath.dataio;

import java.util.ArrayList;

import au.edu.sa.asms.fizpath.DataWord;

public class DataManager {
	
	public static ArrayList<DataWord> MAINDATA = new ArrayList<DataWord>();
	
	public static void addMAINDATA(DataWord dw){
		MAINDATA.add(dw);
	}

}
