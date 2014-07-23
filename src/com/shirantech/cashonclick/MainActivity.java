package com.shirantech.cashonclick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	TextView tvInfo;
	DBHelper dbHelper;
	SQLiteDatabase database;
	ExecuteAsRootBase rooter;
	SharedPreferences prefFile;
	Button btnAction;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rooter = new ExecuteAsRootBase();

		tvInfo = (TextView) findViewById(R.id.tvInfo);
		btnAction = (Button)findViewById(R.id.btnAction);
		btnAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cashHack();
			}
		});
	}
private void cashHack(){
	ArrayList<String> commands = new ArrayList<String>();
	String msg="";
	commands.add("cp -f /data/data/com.shirantech.cashonad/databases/cashonad_db /data/data/com.shirantech.cashonclick/databases/cashonad_db");
	commands.add("cp -f /data/data/com.shirantech.cashonad/shared_prefs/EarnedPointsSharedPrefs.xml /data/data/com.shirantech.cashonclick/shared_prefs/EarnedPointsSharedPrefs.xml");
	commands.add("chmod 777 /data/data/com.shirantech.cashonclick/databases/cashonad_db");
	commands.add("chmod 777 /data/data/com.shirantech.cashonclick/shared_prefs/EarnedPointsSharedPrefs.xml");
	if(rooter.execute(commands))
		Toast.makeText(this, "db and xml copy successed. :-) ", Toast.LENGTH_SHORT).show();
	
	//File sdCard = Environment.getExternalStorageDirectory();
	database = this.getApplicationContext().openOrCreateDatabase("cashonad_db", SQLiteDatabase.OPEN_READWRITE, null);
	//database = SQLiteDatabase.openDatabase("cashonad_db", null, SQLiteDatabase.OPEN_READWRITE);
	
	Random random = new Random();
	String last_campaign_id = "";
	
	String cur_mobile_number="";	//make it true randmo
	String cur_campaign_id="";		//make it next to last
	String cur_date_time="";		//make it current date
	String cur_sync_status="";		//make it 0
	String cur_video_played_duration="";	//make it rute random
	String cur_call_attended_flag="";		//make it random or true always?
	
	Cursor cur;
	// SELECT campaign_id from campaign_ring_log where call_attended_flag="true" AND video_played_duration NOT IN ("0","1","2","3","4","5","6")  ORDER BY id DESC LIMIT 1;
	cur = database.rawQuery("SELECT campaign_id from campaign_ring_log where call_attended_flag=\"true\" AND video_played_duration NOT IN (\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\")  ORDER BY id DESC LIMIT 1",null);
	cur.moveToFirst();
	try{
		last_campaign_id = cur.getString(0);
	}catch(Exception ex){
		cur = database.rawQuery("SELECT campaign_id from campaign_ring_log ORDER BY id DESC LIMIT 1",null);
		cur.moveToFirst();
		last_campaign_id = cur.getString(0);
	}
	cur.close();
	
	msg += "\n Lat camp_id = "+last_campaign_id;
	
	cur = database.rawQuery("select distinct campaign_id from campaign_details", null);
	ArrayList<String> ids = new ArrayList<String>();
	do{
		cur.moveToNext();
		ids.add(""+cur.getInt(0));//gettingintandconvertingtostring
		//msg += "\n ids :: "+cur.getInt(0);
	}while(!cur.isLast());
	
	for(int i=0;i<ids.size();i++){
		if(last_campaign_id.compareTo(ids.get(i))==0){
			cur_campaign_id=ids.get((i+1)%ids.size());
			break;
		}
	}
	//msg+= "\ncur_camp_id = "+cur_campaign_id;
	cur.close();
	
	SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d H:m:s");
	
	String day = df.format(new Date());
	cur_date_time = day;
	
	cur_sync_status = "0";
	
	cur_mobile_number="";
	prefFile = getSharedPreferences("CONTACT_STORE", 0);
	
	Map<String,?> numbers = prefFile.getAll();
	ArrayList<String> valNum = new ArrayList<String>();
	for( Map.Entry<String,?> entry : numbers.entrySet()){
		String num=entry.getKey();
		if((num.length()==10) || (num.length()==9)){
			valNum.add(num);
		}
	}//return contact with valid phone numbers.
	int ran = random.nextInt(valNum.size());
	cur_mobile_number = valNum.get(ran);
	
	cur_video_played_duration = ""+(6+random.nextInt(30));
	cur_call_attended_flag="true";
	
	//insert the new row;
	String query="";
	query = "INSERT INTO campaign_ring_log values(null, \""+
			cur_mobile_number+"\", \""+
			cur_campaign_id+"\", \""+
			cur_date_time+"\", \""+
			cur_sync_status+"\", \""+
			cur_video_played_duration+"\", \""+
			cur_call_attended_flag+"\""+")";
	msg += "\n query = "+query;
	
	btnAction.setEnabled(false);
	CountDownTimer Count = new CountDownTimer(Integer.parseInt(cur_video_played_duration)*1000,1000){
		@Override
		public void onTick(long mili){
			btnAction.setText((mili/1000)+"");
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			btnAction.setEnabled(true);
			btnAction.setText("Next $0.01");
		}
	};
	Count.start();
	
	try{
	database.execSQL(query);
	}catch(Exception ex){
		msg+="\n"+ex.getMessage();
	}
	//for the played count on campaign_detail database.
	//select campaign_played_count from campaign_details where campaign_id=13 limit 1
	query="select distinct campaign_played_count from campaign_details where campaign_id="+cur_campaign_id;
	cur = database.rawQuery(query,null);
	cur.moveToFirst();
	int played=cur.getInt(0);
	played++;
	query =  "update campaign_details set campaign_played_count="+played+" where campaign_id="+cur_campaign_id;
	try{
		database.execSQL(query);
	}catch(Exception ex){
		msg +="\n"+ex.getMessage();
	}
	
	Toast.makeText(this.getApplicationContext(), "local DB updated", Toast.LENGTH_SHORT).show();
	/*
	 * read campaign_id from campaign_details table.
	 * read last campaign_id with call_attended flag true on campaign_ring_log table.
	 * add new entry on campaign_ring_log table with following entry
	 * insert into campaign_ring_log values(null, rand_mob_no, next_campaing_id, current_date_time, sync_status 0, video_dureation >6,call_attended_flag true);
	 *  	
	 */
	
	float point=0;
	SharedPreferences pref;
	pref = this.getApplicationContext().getSharedPreferences("EarnedPointsSharedPrefs", 0);
	point = pref.getFloat("earnedPointsCounter", 0);
	point = point+(float)0.01;
	SharedPreferences.Editor edit_pref=pref.edit();
	edit_pref.putFloat("earnedPointsCounter", point);
	msg += "\n Points = "+point;
	edit_pref.commit();
	Toast.makeText(this.getApplicationContext(), "local xml updated", Toast.LENGTH_SHORT).show();
	
	commands = new ArrayList<String>();
	commands.add("cp -f /data/data/com.shirantech.cashonclick/databases/cashonad_db /data/data/com.shirantech.cashonad/databases/cashonad_db");
	commands.add("chmod 660 /data/data/com.shirantech.cashonad/databases/cashonad_db");
	commands.add("export owner=`ls -l /data/data/com.shirantech.cashonad/shared_prefs/CONTACT_STORE.xml | awk '{print $3}'`");
	commands.add("chown $owner:$owner /data/data/com.shirantech.cashonad/databases/cashonad_db");
	
	commands.add("cp -f /data/data/com.shirantech.cashonclick/shared_prefs/EarnedPointsSharedPrefs.xml /data/data/com.shirantech.cashonad/shared_prefs/EarnedPointsSharedPrefs.xml");
	commands.add("chmod 660 /data/data/com.shirantech.cashonad/shared_prefs/EarnedPointsSharedPrefs.xml");
	commands.add("chown $owner:$owner /data/data/com.shirantech.cashonad/shared_prefs/EarnedPointsSharedPrefs.xml");
	if(rooter.execute(commands))
		Toast.makeText(this.getApplicationContext(), "Updated file replaced.", Toast.LENGTH_SHORT).show();
	tvInfo.setText(msg);
}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
