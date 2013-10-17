package com.example.luxmetre;

import org.apache.commons.math3.fraction.Fraction;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.R.fraction;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{
	
	TextView lightValue;
	EditText shutterValue;
	EditText apertureValue;
	EditText sensitivityValue;
	
	TextView resultValue;
	
	Button lightPauseButton;
	Button resetButton;
	Button validateButton;
	
	SensorManager manager;
	Sensor lum;
	private int sensorState = 1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		manager = (SensorManager)getSystemService(SENSOR_SERVICE);
		lum = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
		manager.registerListener(this, lum, SensorManager.SENSOR_DELAY_UI);
		
		lightValue = (TextView)findViewById(R.id.light_value);
		resultValue = (TextView)findViewById(R.id.result_label);
		shutterValue = (EditText)findViewById(R.id.shutter_value);
		apertureValue = (EditText)findViewById(R.id.aperture_value);
		sensitivityValue = (EditText)findViewById(R.id.sensitivity_value);
		
		lightPauseButton = (Button)findViewById(R.id.light_button);
		resetButton = (Button)findViewById(R.id.reset_button);
		validateButton = (Button)findViewById(R.id.validate_button);
		
		
		buttonsListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.sensor.getType() == Sensor.TYPE_LIGHT) {
			Log.d("Light", ""+arg0.values[0]);
			lightValue.setText(""+arg0.values[0]);
		}
	}
	
	public void buttonsListeners() {
		
		lightPauseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(sensorState == 1) {
					//notify --> unregister listener
					deactivateSensor();
				}
				else {
					//notify --> register listener
					activateSensor();
				}
			}
		});
		
		resetButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				resultValue.setText("");
				shutterValue.setText("");
				apertureValue.setText("");
				sensitivityValue.setText("");
			}
		});
		
		validateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(sensitivityValue.getText().toString().equals("")){
					//Error
					resultValue.setText("Missing Values");
				}
				else if(shutterValue.getText().toString().equals("")) {
					
					Log.d("calc", "shutter");
					double shutterSpeed = getShutter(Double.parseDouble(apertureValue.getText().toString()),
								Integer.parseInt(sensitivityValue.getText().toString()),
								Double.parseDouble((String) lightValue.getText()));
					if(shutterSpeed < 1) {
						Fraction f = new Fraction(shutterSpeed);
						if(f.getNumerator() != 1) {
							int newDenominator = f.getDenominator()/f.getNumerator();
							Fraction newFraction = new Fraction(1, newDenominator);
							resultValue.setText("Shutter Speed : "+newFraction+" s");
						}
						else {
							resultValue.setText("Shutter Speed : "+f+" s");
						}
					}
					else {
						resultValue.setText("Shutter Speed : "+shutterSpeed+" s");
					}
					
				}
				//apertureValue.getText() = ""
				else {
					Log.d("calc", "aperture");
					double aperture = getAperture(Double.parseDouble(shutterValue.getText().toString()),
							Integer.parseInt(sensitivityValue.getText().toString()),
							Double.parseDouble((String) lightValue.getText()));
					
					//Fraction f = new Fraction(aperture);
					resultValue.setText("Aperture : f "+aperture);
					
				}
			}
		});
	}
	
	/*
	 * N²/t = ES/C
	 * N : aperture (f) - double
	 * t : exposition time, shutter speed (s) - double
	 * E : Illuminance (lux) - double
	 * S : sensibility (ISO) - int
	 * C : incident-light meter calibration constant (~=33.127 / sunny day outside ~= 5000 lux)
	 * 
	 * N = sqrt(tES/C)
	 * t = CN²/ES
	 */
	
	public double getAperture(double t, int S, double E) {
		return Math.sqrt((t*E*S)/33.127);
	}
	
	public double getShutter(double N, int S, double E) {
		return (33.127*N*N)/(E*S);
	}
	
	public void activateSensor() {
		manager.registerListener(this, lum, SensorManager.SENSOR_DELAY_UI);
		sensorState = 1;
	}
	
	public void deactivateSensor() {
		manager.unregisterListener(this, lum);
		sensorState = 0;
	}

}
