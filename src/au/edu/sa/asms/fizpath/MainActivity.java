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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
	
	
	
	// The first method (or subroutine) within MainActivity is called Oncreate.
	// This is run when the MainActivity is first created.
	// 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_UI);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
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
	// This procedure initiates every time a sensor value change is detected and is the starting
	// point for most of this app's functionality
	public void onSensorChanged(SensorEvent event) {
//		Toast.makeText(getApplicationContext(), "Accel Changed", Toast.LENGTH_SHORT).show();	//temp debugging toast
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float[] values = event.values;	// accelerometer value array ([0] for x, [1] for y and [2] for z)

			colourScreen(values);	// set the screen background colour according to acceleration values (this can be commented out if preferred)

				//				set the on-screen text label to a new total acceleration value
			    TextView accel_label =(TextView)findViewById(R.id.textView1);
				String newmessage = String.valueOf(values[2]);
				accel_label.setText(newmessage);

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

		RelativeLayout view = (RelativeLayout)findViewById(R.id.view);	// "view" allows access to screen properties
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
