package com.mukeshmaurya91.mausam;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {
	private RelativeLayout rl;
	private TextView temp, type, humi, min, max, press, wind, city,updatetime;
	private TextView icon;
	private Info info= new Info();
	private ProgressDialog pd;
	private String shareText=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		temp = (TextView) findViewById(R.id.temp);
		type = (TextView) findViewById(R.id.type);
		humi = (TextView) findViewById(R.id.humidity);
		min = (TextView) findViewById(R.id.min);
		max = (TextView) findViewById(R.id.max);
		press = (TextView) findViewById(R.id.pressure);
		wind = (TextView) findViewById(R.id.wind);
		city = (TextView) findViewById(R.id.city);
		updatetime =(TextView)findViewById(R.id.updated);
		icon =(TextView)findViewById(R.id.icon);
		Typeface font = Typeface.createFromAsset(getAssets(), "weathericons-regular-webfont.ttf");
		icon.setTypeface(font);
		temp.setTypeface(font);
		min.setTypeface(font);
		max.setTypeface(font);
		humi.setTypeface(font);
		rl = (RelativeLayout) findViewById(R.id.rl);
		if (isNight()) {
			rl.setBackgroundResource(R.raw.night_2);
		}
		else{
			icon.setTextColor(Color.WHITE);
		}
	}
   
	@Override
    protected void onStart() {
		/*SharedPreferences msharePref = getSharedPreferences("mPref", Activity.MODE_PRIVATE);
		if(msharePref.getBoolean("isFirst", true))
		{
			startActivity(new Intent(this, SettingsActivity.class));
		}*/
    	updateWeatherFromPref();
    	super.onStart();
    }
    private void saveSharedPref(){
    	SharedPreferences msharePref = getSharedPreferences("mPref", Activity.MODE_PRIVATE);
    	SharedPreferences.Editor edit=msharePref.edit();
    	edit.putString("city", info.getCityName());
    	edit.putString("country",info.getCountry());
    	edit.putString("temp",info.getTemp());
    	edit.putString("temp_min",info.getTempMin());
    	edit.putString("temp_max",info.getTempMax());
    	edit.putString("type",info.getType());
    	edit.putString("humidity",info.getHumidity());
    	edit.putString("pressure",info.getPressure());
    	edit.putString("windSpeed",info.getWindSpeed());
    	edit.putString("updated",info.getUpdateTime());
    	edit.putString("icon", info.getIcon());
    	edit.putBoolean("isFirst",false);
    	edit.commit();
    }
	private void updateWeatherFromPref() {
		SharedPreferences msharePref = getSharedPreferences("mPref", Activity.MODE_PRIVATE);
		info.setCityName(msharePref.getString("city", "Enter city name"));
		info.setCountry(msharePref.getString("country", " in settings"));
		info.setTemp(msharePref.getString("temp", "00"));
		info.setTempMin(msharePref.getString("temp_min", "00"));
		info.setTempMax(msharePref.getString("temp_max", "99"));
		info.setType(msharePref.getString("type", "xxxx"));
		info.setHumidity(msharePref.getString("humidity", "00"));
		info.setPressure(msharePref.getString("pressure", "00"));
		info.setWindSpeed(msharePref.getString("windSpeed", "00"));
		info.setUpdateTime(msharePref.getString("updated", "refresh"));
		info.setIcon(msharePref.getString("icon", "na"));
		setTexts();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_refresh) {
			updateWeather();
			return true;
		}
		if(id==R.id.action_share){
			setShareText();
            if(shareText!=null)
            startActivity(Intent.createChooser(getDefaultShareIntent(),"Share with"));
            else
                Toast.makeText(this,"Current weather is unavailable.",Toast.LENGTH_LONG).show();
        }
		if (id == R.id.action_about) {
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
			ad.setTitle("About Me");
			ad.setMessage("Mukesh Kumar Maurya\nCSE\nmukeshmaurya91@gmail.com");
			ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			ad.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void setShareText(){
		shareText=info.getCityName()+", "+info.getCountry()+"\n Temperature: "+
    	                   info.getTemp()+"*c\n"+
    	                   info.getType()+"\nHumidity: "+
    	                  info.getHumidity()+"%\nPressure: "+
    	                  info.getPressure()+"hpa\nWind Speed : "+
    	                  info.getWindSpeed()+"m/s\nUpdated On: "+
    	                  info.getUpdateTime();
	}
	private Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Current Weather Report");
        intent.putExtra(Intent.EXTRA_TEXT,"\n"+shareText);
        return intent;
    }

	private void updateWeather() {
		SharedPreferences settingPref = PreferenceManager.getDefaultSharedPreferences(this);
		String city=settingPref.getString("CITY_PREF", "Ghaziabad");
		//if(city.equals(null))
		try {
			if (isConnected()) {
				new AccessData()
						.execute("http://api.openweathermap.org/data/2.5/weather?q="+city+"&units=metric");
			} else {
				//lon.setText("Please connect to Wi-Fi or Mobile Internet.");	
			   Toast.makeText(this, "No Internet connection!", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			Toast.makeText(this, "Error in your Internet."+e.toString(), Toast.LENGTH_LONG).show();
		}

	}
	private void setTexts(){
		city.setText(info.getCityName()+", "+info.getCountry());
		temp.setText(info.getTemp()+getResources().getString(R.string.celsius));
		min.setText(info.getTempMin()+getResources().getString(R.string.celsius));
		max.setText(info.getTempMax()+getResources().getString(R.string.celsius));
		type.setText(info.getType());
		humi.setText("Humidity \n"+info.getHumidity()+getResources().getString(R.string.humidity));
		press.setText("Pressure \n"+info.getPressure()+" hpa");
		wind.setText("Wind speed \n"+info.getWindSpeed()+" m/s");
		updatetime.setText("Updated on\n"+info.getUpdateTime());
		icon.setText(setIcon(info.getIcon()));
	}
	private String setIcon(String ico) {
		if(ico.equals("01d"))
		 return getResources().getString(R.string.day_clear_sky);
		else if(ico.equals("02d"))
		 return getResources().getString(R.string.day_few_clouds);
		else if(ico.equals("03d"))
			 return getResources().getString(R.string.day_scattered_clouds);
		else if(ico.equals("04d"))
			 return getResources().getString(R.string.day_broken_clouds);
		else if(ico.equals("09d"))
			 return getResources().getString(R.string.day_shower_rain);
		else if(ico.equals("10d"))
			 return getResources().getString(R.string.day_rain);
		else if(ico.equals("11d"))
			 return getResources().getString(R.string.day_thunderstrom);
		else if(ico.equals("13d"))
			 return getResources().getString(R.string.day_snow);
		else if(ico.equals("50d"))
			 return getResources().getString(R.string.day_mist);
		else if(ico.equals("01n"))
			 return getResources().getString(R.string.ni_clear_sky);
		else if(ico.equals("02n"))
			 return getResources().getString(R.string.ni_few_clouds);
		else if(ico.equals("03n"))
			 return getResources().getString(R.string.ni_scattered_clouds);
		else if(ico.equals("04n"))
			 return getResources().getString(R.string.ni_broken_clouds);
		else if(ico.equals("09n"))
			 return getResources().getString(R.string.ni_shower_rain);
		else if(ico.equals("10n"))
			 return getResources().getString(R.string.ni_rain);
		else if(ico.equals("11n"))
			 return getResources().getString(R.string.ni_thunderstrom);
		else if(ico.equals("13n"))
			 return getResources().getString(R.string.ni_snow);
		else if(ico.equals("50n"))
			 return getResources().getString(R.string.ni_mist);
		else
			return getResources().getString(R.string.na);
	}
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean connection = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		return connection;
	}

	private boolean isNight() {
		Calendar calender = Calendar.getInstance();
		int hour = calender.get(Calendar.HOUR_OF_DAY);
		return hour < 6 || hour > 18;
	}
  private String currentTime(){
	  Calendar c = Calendar.getInstance();
	  SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aaa dd-MMM-yyyy",Locale.getDefault());
	  return sdf.format(c.getTime());
  }
	class AccessData extends AsyncTask<String, Void, String> {
    @Override
    protected void onPreExecute() {
    	pd =new ProgressDialog(DetailActivity.this);
    	pd.setTitle("Loading");
    	pd.setMessage("Please wait ...");
    	pd.setCancelable(false);
    	pd.show();
    	super.onPreExecute();
    }
		@Override
	protected String doInBackground(String... params) {
			 StringBuilder  sb = new StringBuilder();
			 HttpURLConnection urlConnection = null;
		
		try { 
			URL url = new URL(params[0]);
			urlConnection = (HttpURLConnection) url.openConnection();
		     InputStream is= new BufferedInputStream(urlConnection.getInputStream());
		     BufferedReader reader =new BufferedReader(new InputStreamReader(is, "UTF-8"));
		     String line;
			  while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
		   }catch(Exception e){
			   Log.e("Connection error", e.toString());
		   }
		     urlConnection.disconnect();
		     return sb.toString();
		 }
		

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			parseJsonResult(result);
			super.onPostExecute(result);
		}

	}

	public void parseJsonResult(String result) {
		
		try{
			JSONObject obj = new JSONObject(result);
			JSONArray weather = obj.getJSONArray("weather");
			for(int i=0;i<weather.length();i++){
				JSONObject json = weather.getJSONObject(i);
				//ty = json.getString("main");
				info.setType(json.getString("description"));
				info.setIcon(json.getString("icon"));
			}
			JSONObject main = obj.getJSONObject("main");
			info.setTemp(main.getString("temp"));
			info.setTempMin(main.getString("temp_min"));
			info.setTempMax(main.getString("temp_max"));
			info.setPressure(main.getString("pressure"));
			info.setHumidity(main.getString("humidity"));
			JSONObject wind=obj.getJSONObject("wind");
			info.setWindSpeed(wind.getString("speed"));
			JSONObject sys=obj.getJSONObject("sys");
			info.setCountry(sys.getString("country"));
			info.setCityName(obj.getString("name"));
			info.setUpdateTime(currentTime());
			saveSharedPref();
			setTexts();
		}catch(Exception e){
			Toast.makeText(DetailActivity.this, "Error in JSON parsing "+e.toString(), Toast.LENGTH_LONG).show();
		}
		
	}
}
