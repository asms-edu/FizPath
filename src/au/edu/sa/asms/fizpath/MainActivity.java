package au.edu.sa.asms.fizpath;

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
	private SensorManager senSensorManager;	// gives us access to the device's sensors
	private Sensor senAccelerometer;		// the proxy to access the accelerometer sensors

	private int motionStage;	// stage of motion (0=waiting, 1=calibrating, 2=during motion, 3=potentially stopped, 4=stopped)
	private double accelSum;	// container to hold cumulative sum of accelerometer values
	private long accelCount;	// number of accelerometer values in accelSum
	private long RestingYval;	// the value of the Y-axis accelerometer reading at rest found by average during motion stage 1
	DataWord lastTime;	// the previous data word
	DataWord thisTime;	// the current data word
	private long TimeButtonPushed;	// when the start button was pushed (from systemClock.elapsedRealTime)
	private long timeStage3;			// time when stage 3 was most recently triggered
	private static long slowdownDuration=50;	// duration (in ms) that stage 3 must last to confirm motion completed.
	private double currentVelocity;	// current velocity in the ground-based frame of reference
	private double currentDisplacement; // current displacement from location where start button was pushed
	private static int waitTime=150;	// time (in ms) that the system waits after the start button is pushed before calibrating (to allow for the handling vibrations to dissipate)
	private static double startTrigger=0.2;	// acceleration (in m/s/s) above ambient that indicates that motion has begun
	private static double stopTrigger=0.1;	// threshold below which motion may have stopped
	private double initialCal;	// the approximate calibration value from the wait period (used to determine when stage 2 is reached)
	private double mainCal;		// the calibration value calculated over stage 2 and used to correct all acceleration values during motion)
	
	@Override
	public void onSensorChanged(SensorEvent event) {
//		This procedure initiates every time a sensor value change is detected and is the starting
//		point for most of this app's functionality
//		Toast.makeText(getApplicationContext(), "Accel Changed", Toast.LENGTH_SHORT).show();	//temp debugging toast
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

//			temp debugging
//			TextView debugLabel =(TextView)findViewById(R.id.textView3);
//			String LabelString2 = "DeltaT: " + String.valueOf(SystemClock.elapsedRealtime()-thisTime.timestamp);
//			debugLabel.setText(LabelString2);

			
			lastTime.timestamp=thisTime.timestamp;
			thisTime.timestamp = SystemClock.elapsedRealtime();
			thisTime.linear = event.values;	// accelerometer value array ([0] for x, [1] for y and [2] for z)
			
//			colourScreen(values);	// set the screen background colour according to acceleration values (this can be commented out if preferred)

			String LabelString;
			switch (motionStage){
				case 0: if	((SystemClock.elapsedRealtime()-TimeButtonPushed)>waitTime){
							motionStage=1;
							// determine initial calibration value and reset accelSum and accelCount
							initialCal=accelSum/accelCount;
							accelSum=0;
							accelCount=0;
							
//							set the on-screen label to the current motion stage
						    TextView StageLabel =(TextView)findViewById(R.id.textView1);
							LabelString = "Motion Stage: " + String.valueOf(motionStage);
							StageLabel.setText(LabelString);
							
//							set the on-screen label to the current calibration value
							TextView CalLabel =(TextView)findViewById(R.id.textView5);
							LabelString = "Y cal: " + String.valueOf(initialCal);
							CalLabel.setText(LabelString);
						}
						else{ // if still waiting, keep calculating an average acceleration value
							accelSum=accelSum+thisTime.linear[1];
							accelCount++;
						}
						break;
				case 1:	if (Math.abs(thisTime.linear[1]-initialCal)>startTrigger){ // check if calibrated acceleration exceeds start trigger
							motionStage=2;
							mainCal=accelSum/accelCount;
//							set the on-screen label to the current motion stage
						    TextView StageLabel =(TextView)findViewById(R.id.textView1);
							LabelString = "Motion Stage: " + String.valueOf(motionStage);
							StageLabel.setText(LabelString);
							
							// update velocity and displacement values
							currentVelocity = MotionFunctions.updateVelocity(thisTime.linear, mainCal, currentVelocity, (thisTime.timestamp-lastTime.timestamp));
							currentDisplacement = MotionFunctions.updatePosition(currentVelocity, currentDisplacement, (thisTime.timestamp-lastTime.timestamp));
						}
						else{
//							update the data to calculate an average acceleration value
							accelSum=accelSum+thisTime.linear[1];
							accelCount++;
		
//							set the on-screen label to the current calibration value
							TextView CalLabel =(TextView)findViewById(R.id.textView5);
							LabelString = "Y cal: " + String.valueOf(accelSum/accelCount) + "00000000000";
							LabelString=LabelString.substring(0,18);
							CalLabel.setText(LabelString);
						}
						break;
				case 2: // update velocity and displacement values
						currentVelocity = MotionFunctions.updateVelocity(thisTime.linear, mainCal, currentVelocity, (thisTime.timestamp-lastTime.timestamp));
						currentDisplacement = MotionFunctions.updatePosition(currentVelocity, currentDisplacement, (thisTime.timestamp-lastTime.timestamp));
//						update the on-screen label with new velocity and displacement values
						TextView VelLabel =(TextView)findViewById(R.id.textView3);
						LabelString = "Y vel: " + String.valueOf(currentVelocity);
						VelLabel.setText(LabelString);
						TextView DisplacementLabel =(TextView)findViewById(R.id.textView4);
						LabelString = "Y disp: " + String.valueOf(currentDisplacement);
						DisplacementLabel.setText(LabelString);
						
						if (Math.abs(thisTime.linear[1]-mainCal)<stopTrigger){
							motionStage=3;
							timeStage3=thisTime.timestamp;
//							set the on-screen label to the current motion stage
						    TextView StageLabel =(TextView)findViewById(R.id.textView1);
							LabelString = "Motion Stage: " + String.valueOf(motionStage);
							StageLabel.setText(LabelString);
						}
						
						break;
				case 3:// update velocity and displacement values
					currentVelocity = MotionFunctions.updateVelocity(thisTime.linear, mainCal, currentVelocity, (thisTime.timestamp-lastTime.timestamp));
					currentDisplacement = MotionFunctions.updatePosition(currentVelocity, currentDisplacement, (thisTime.timestamp-lastTime.timestamp));
//					update the on-screen label with new velocity and displacement values
					VelLabel =(TextView)findViewById(R.id.textView3);
					LabelString = "Y vel: " + String.valueOf(currentVelocity);
					VelLabel.setText(LabelString);
					DisplacementLabel =(TextView)findViewById(R.id.textView4);
					LabelString = "Y disp: " + String.valueOf(currentDisplacement);
					DisplacementLabel.setText(LabelString);
					
					if (Math.abs(thisTime.linear[1]-mainCal)>stopTrigger){
						motionStage=2;
//						set the on-screen label to the current motion stage
					    TextView StageLabel =(TextView)findViewById(R.id.textView1);
						LabelString = "Motion Stage: " + String.valueOf(motionStage);
						StageLabel.setText(LabelString);
					}
					else if ((thisTime.timestamp-timeStage3)>slowdownDuration){
						motionStage=4;
//						set the on-screen label to the current motion stage
					    TextView StageLabel =(TextView)findViewById(R.id.textView1);
						LabelString = "Motion Stage: " + String.valueOf(motionStage);
						StageLabel.setText(LabelString);
					}
						break;
				case 4:
						//thisTime.Linear=values;
						break;
			}
				//				set the on-screen text label to a new Y-axis acceleration value
			    TextView accel_label =(TextView)findViewById(R.id.textView2);
				String newmessage = String.valueOf(thisTime.linear[1]-initialCal);
				accel_label.setText(newmessage);

		}
	}
	
	// The first method (or subroutine) to run within MainActivity is called Oncreate.
	// This is run when the MainActivity is first created.
	// 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_UI);
		
	    thisTime= new DataWord();
	    lastTime= new DataWord();
	    
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
			motionStage=4;	// set to stopped until start button first pressed by user
//			thisTime.timestamp=0;
//			lastTime.timestamp=0;
		}
	}
	
	public void StartSequence(View view) {
		// when the start button is pushed
		motionStage=0;
		TimeButtonPushed=SystemClock.elapsedRealtime();
		thisTime.timestamp=SystemClock.elapsedRealtime();
		currentVelocity=0;
		currentDisplacement=0;
		accelSum=0;
		accelCount=0;
		
//		set the on-screen labels to the current values
	    TextView StageLabel =(TextView)findViewById(R.id.textView1);
		String LabelString = "Motion Stage: " + String.valueOf(motionStage);
		StageLabel.setText(LabelString);
		TextView VelLabel =(TextView)findViewById(R.id.textView3);
		LabelString = "Y vel: " + String.valueOf(currentVelocity);
		VelLabel.setText(LabelString);
		TextView DisplacementLabel =(TextView)findViewById(R.id.textView4);
		LabelString = "Y disp: " + String.valueOf(currentDisplacement);
		DisplacementLabel.setText(LabelString);
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
	    super.onResume();
	    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	private void colourScreen(float AccelValues[]){

		LinearLayout view = (LinearLayout)findViewById(R.id.view);	// "view" allows access to screen properties
		int[] ARGB = new int[4];	// an array of integers to store alpha (transparency), red, blue and green values

		ARGB[0]= 127;	// set alpha to fully-opaque
		
//		calculate RGB values from corresponding x,y and z acceleration values
		for (int x=1; x<4; x=x+1){
			ARGB[x] = MotionFunctions.TrimmedColour(-5.5, 5.5, AccelValues[(x-1)]);
		}

//		set background colour to the channel values in ARGB
		view.setBackgroundColor(Color.argb(ARGB[0], ARGB[1], ARGB[2], ARGB[3]));
	}
}
