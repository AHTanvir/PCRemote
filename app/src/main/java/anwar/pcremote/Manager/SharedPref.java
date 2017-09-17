package anwar.pcremote.Manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by anwar on 9/14/2017.
 */

public class SharedPref {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "PcRemote";
    private static final String PREFS_KEY= "user_name";
    public SharedPref(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); //1
        editor = pref.edit();
    }
    public void clearSharedPreference() {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    public void setUserName(String userName){
        editor = pref.edit();
        editor.putString(PREFS_KEY,userName);
        editor.commit();
    }
    public String getUserName(){
       return pref.getString(PREFS_KEY, null);
    }
}
