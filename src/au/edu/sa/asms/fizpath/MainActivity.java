package au.edu.sa.asms.fizpath;

// Any imports are listed here. In Eclipse, press ctl-shift-O to update this list automatically (cmd-shift-O for Mac)
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//Declare the main class (this is where the App starts running)
//
//The "implements SensorEventListener" part of this declaration is required
//for us to see the accelerometer.
//
public class MainActivity extends Activity implements SensorEventListener{
//	Declare variables for both the accelerometer sensor and a manager for the accelerometer
//	The variable declaration syntax here is:
//		- variable scope. Private indicates that the variable is only visible within the containing class (MainActivity)
//		- variable name. By convention multiple words are connected with underscores or by capitalising each word
//		- variable class (usually imported but can be a private class you write yourself)
//	
	public String debugtemp;	// temporary debugging variable
	
	private SensorManager senSensorManager;	// gives us access to the device's sensors
	private Sensor senAccelerometer;		// the proxy to access the accelerometer sensors
	private int motionStage;	// stage of motion (0=waiting, 1=calibrating, 2=during motion, 3=potentially stopped, 4=stopped)
	DataWord lastTime;	// the previous data word
	DataWord thisTime;	// the current data word
	private long TimeButtonPushed;	// when the start button was pushed (from systemClock.elapsedRealTime)
	private long timeStage3;			// time when stage 3 was most recently triggered
	private static long slowdownDuration=50;	// duration (in ms) that stage 3 must last to confirm motion completed.
	private static int waitTime=150;	// time (in ms) that the system waits after the start button is pushed before calibrating (to allow for the handling vibrations to dissipate)
	private static double startTrigger=0.2;	// acceleration (in m/s/s) above ambient that indicates that motion has begun
	private static double stopTrigger=0.1;	// threshold below which motion may have stopped
	private double [] accelSum;	// container to hold cumulative sum of accelerometer values for each axis (0=x, 1=y, 2=z)
	private long accelCount;	// number of accelerometer values in accelSum
	private double [] calibration;	// the acceleration correction for each axis (0=x, 1=y, 2=z)
	
	@Override
	public void onSensorChanged(SensorEvent event) {
//		This procedure initiates every time a sensor value change is detected and is the starting
//		point for most of this app's functionality
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			lastTime=thisTime;	// pass data from this time to last time
			thisTime=new DataWord();
			thisTime.timestamp = SystemClock.elapsedRealtime(); //update time stamp
			thisTime.linearAccel = event.values;	// accelerometer value array ([0] for x, [1] for y and [2] for z)
			
			debugtemp = String.valueOf((thisTime.linearAccel[1]-calibration[1]));

			//			colourScreen(values);	// set the screen background colour according to acceleration values (this can be commented out if preferred)
			UpdateScreenLabels();

			switch (motionStage){
				case 0: if	((SystemClock.elapsedRealtime()-TimeButtonPushed)>waitTime){	// test if enough time has elapsed to move to stage 2
							motionStage=1;
							// determine initial calibration value and reset accelSum and accelCount
							calibration[1]=accelSum[1]/accelCount;
							accelSum[1]=0;
							accelCount=0;
						}
						else{ // if still waiting, keep calculating an average acceleration value
							accelSum[1]=accelSum[1]+thisTime.linearAccel[1];
							accelCount++;	// this is Java shorthand for "add one to accelCount"
							calibration[1]=accelSum[1]/accelCount;
						}
						break;
				case 1:	if (Math.abs(thisTime.linearAccel[1] - calibration[1])>startTrigger){ // check if calibrated acceleration exceeds start trigger
							motionStage=2;
							// update velocity and displacement values (required as soon as Stage 2 is triggered)
							thisTime.linearVelocity = MotionFunctions.updateVelocity(calibration, thisTime, lastTime);
							thisTime.linearDisplacement = MotionFunctions.updatePosition(thisTime, lastTime);
						}
						else{	// still in stage 1 so keep calculating calibration values
							accelSum[1]=accelSum[1]+thisTime.linearAccel[1];
							accelCount++;
						}
						calibration[1]=accelSum[1]/accelCount;	// calculate calibration value for Y-axis that will be used during motion calculations
						break;
				case 2: // update velocity and displacement values
						thisTime.linearVelocity = MotionFunctions.updateVelocity(calibration, thisTime, lastTime);
						thisTime.linearDisplacement = MotionFunctions.updatePosition(thisTime, lastTime);
//						Toast.makeText(getApplicationContext(), String.valueOf(((thisTime.timestamp-lastTime.timestamp)/1000)), Toast.LENGTH_SHORT).show();
						
						if (Math.abs(thisTime.linearAccel[1]-calibration[1])<stopTrigger){	// check if acceleration value has fallen below threshold for stage 3
							motionStage=3;
							timeStage3=thisTime.timestamp;	// this time is used to determine if the low acceleration is transient or actually the end of motion.
						}
						break;
				case 3:// update velocity and displacement values
					thisTime.linearVelocity = MotionFunctions.updateVelocity(calibration, thisTime, lastTime);
					thisTime.linearDisplacement = MotionFunctions.updatePosition(thisTime, lastTime);
						
						if (Math.abs(thisTime.linearAccel[1]-calibration[1])>stopTrigger){	//if acceleration rises again, revert to stage 2
							motionStage=2;
						}
						else if ((thisTime.timestamp-timeStage3)>slowdownDuration){	//if acceleration has remained low long enough, move to stage 4
							motionStage=4;
						}
						break;
				case 4:
						// stopped - do nothing
						break;
			}


		}
	}
	
	// The first method (or subroutine) to run within MainActivity is called Oncreate.
	// This is run when the MainActivity is first created.
	// 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	// set up screen
		
	// set up accelerometer
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_UI);
		// instantiate the custom class variables and arrays previously declared
	    thisTime= new DataWord();
	    lastTime= new DataWord();
	    calibration = new double [3];
	    accelSum = new double[3];
	    
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
			motionStage=4;	// set to stopped until start button first pressed by user
		}
	}
	
	public void StartSequence(View view) {
		// when the start button is pushed
		motionStage=0;
		TimeButtonPushed=SystemClock.elapsedRealtime();	// used to determine the transition to stage 1
		thisTime=new DataWord();
		lastTime=new DataWord();
		thisTime.timestamp=SystemClock.elapsedRealtime();	// set up a thisTime.timestamp value to pass to lastTime when onSensorChanged is first called
		thisTime.linearVelocity[1]=0;	// reset velocity
		thisTime.linearDisplacement[1]=0;	// reset displacement
		accelSum[1]=0;	// reset calibration values
		accelCount=0;	// reset calibration values
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	A placeholder fragment containing a simple view.
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Not used in this app but included as a compulsory requirement of having
		// SensorEventListener implemented in the main procedure
	}

	protected void onPause() {
//		Called every time that the app pauses for any reason (eg. phone screen locked, app sent to background, etc.)
	    super.onPause();
	    // unregistering the sensors saves a lot of battery power
	    senSensorManager.unregisterListener(this);
	}
		
	protected void onResume() {
		// Called when the app is resumed from pause
	    super.onResume();
	    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	private void UpdateScreenLabels (){
//		this routine updates the screen labels to reflect current values
		
//		initialise variables
		String labelString;	// string to hold next label to be updated
	    TextView StageLabel =(TextView)findViewById(R.id.textView1);
	    TextView AccelLabel =(TextView)findViewById(R.id.textView2);
	    TextView VelLabel =(TextView)findViewById(R.id.textView3);
	    TextView DispLabel =(TextView)findViewById(R.id.textView4);
	    TextView CalLabel =(TextView)findViewById(R.id.textView5);
	    labelString="Stage: ";
	    
		switch (motionStage) {	// set motion stage label according to value (with explanatory word)
		case 0: labelString = "Stage: 0 (waiting)";
		break;
		case 1: labelString = "Stage: 1 (calibrating)";
		break;
		case 2: labelString = "Stage: 2 (moving)";
		break;
		case 3: labelString = "Stage: 3 (slowing)";
		break;
		case 4: labelString = "Stage: 4 (stopped)";
		break;
		}
		StageLabel.setText(labelString);
		labelString="Y-accel: " + String.valueOf(thisTime.linearAccel[1])+"000000000000000000";
		AccelLabel.setText(labelString.substring(0,18));
		labelString="Y-vel: " + String.valueOf(thisTime.linearVelocity[1])+"000000000000000000";
		VelLabel.setText(labelString.substring(0,18));
		labelString="Y-disp: " + String.valueOf(thisTime.linearDisplacement[1])+"00000000000000";
		DispLabel.setText(labelString.substring(0,18));
		
//		switch (motionStage) {	// select the appropriate calibration variable depending on motion stage
//		case 0: labelString = "Y-Cal: " + String.valueOf(calibration[1])+"00000000000000";
//		labelString=labelString.substring(0,18);
//		break;
//		case 1: labelString = "Y-Cal: " + String.valueOf(calibration[1])+"00000000000000";
//		labelString=labelString.substring(0,18);
//		break;
//		case 2: labelString = "Y-Cal: " + String.valueOf(calibration[1])+"00000000000000";
//		labelString=labelString.substring(0,18);
//		break;
//		case 3: labelString = "Y-Cal: " + String.valueOf(calibration[1])+"00000000000000";
//		labelString=labelString.substring(0,18);
//		break;
//		case 4: labelString = "Y-Cal: N/A";
//		break;
//		}
		labelString=debugtemp;
		CalLabel.setText(labelString);
	}
	
	private void colourScreen(float AccelValues[]){	// makes a background colour from 3-axis accelerometer values

		LinearLayout view = (LinearLayout)findViewById(R.id.view);	// "view" allows access to screen properties
		int[] ARGB = new int[4];	// an array of integers to store alpha (transparency), red, blue and green values

		ARGB[0]= 255;	// set alpha to fully-opaque
		
//		calculate RGB values from corresponding x,y and z acceleration values
		for (int x=1; x<4; x=x+1){
			ARGB[x] = MotionFunctions.TrimmedColour(-5.5, 5.5, AccelValues[(x-1)]);
		}

//		set background colour to the channel values in ARGB
		view.setBackgroundColor(Color.argb(ARGB[0], ARGB[1], ARGB[2], ARGB[3]));
	}
}
