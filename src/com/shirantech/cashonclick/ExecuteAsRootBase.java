package com.shirantech.cashonclick;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

public class ExecuteAsRootBase {

	public static boolean canRunRootCommand(){
		boolean retVal = false;
		Process suProcess;
		
		try{
			suProcess = Runtime.getRuntime().exec("su");
			
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			
			if(null != os && null!=osRes){
				os.writeBytes("id\n");
				os.flush();
				String currUid = osRes.readLine();
				boolean exitSu=false;
				if( null == currUid){
					retVal = false;
					exitSu = false;
					Log.d("Root", "Can't get root Access or denied by user");
				}
				else if(true == currUid.contains("uid=0")){
					retVal = true;
					exitSu = true;
					Log.d("Root","Root access granted");
				}
				else {
					retVal = false;
					exitSu = true;
					Log.d("Root","Root Access REJECTED : "+currUid);
				}
				if(exitSu){
					os.writeBytes("exit\n");
					os.flush();
				}
			}
		}
		catch( Exception e){
			//cant get root access
			//probally broken pipe exeception.
			retVal = false;
			Log.d("Root","Root Access reejcted [ "+e.getClass().getName()+"] : "+e.getMessage());
		}
		
		return retVal;
	}
public final boolean execute(ArrayList<String> commands){
		boolean retVal = false;
		
		try{
			if(null != commands && commands.size() >0){
				Process suProcess = Runtime.getRuntime().exec("su");
				
				DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
				//Execute commands that require root access
				for(String currCommand : commands){
					os.writeBytes(currCommand + "\n");
					os.flush();
				}
				os.writeBytes("exit\n");
				os.flush();
				try{
					int suProcessRetVal = suProcess.waitFor();
					if(255 != suProcessRetVal) {
						//Root access granted
						retVal = true;
					} else {
						//Root access denied
						retVal = false;
					}
				}catch(Exception ex){
					Log.e("Root", "Error executing root action", ex);
				}
			}
		} catch (IOException ex){
			Log.w("Root", "Can't get root access",ex);
		} catch(SecurityException ex){
			Log.w("Root", "Can't get root access", ex);
		} catch(Exception ex){
			Log.w("Root", "Error executing internal operation",ex);
		}
		return retVal;
	}
}
