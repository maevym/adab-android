package com.ambinusian.adab.ui.splash;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ambinusian.adab.R;
import com.ambinusian.adab.manager.APIManager;
import com.ambinusian.adab.manager.NetworkHelper;
import com.ambinusian.adab.preferences.UserPreferences;
import com.ambinusian.adab.room.ClassDatabase;
import com.ambinusian.adab.room.ClassEntity;
import com.ambinusian.adab.ui.lecturer.MainActivity;
import com.ambinusian.adab.ui.login.ActivityLogin;

import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private ClassDatabase db;
    private final UserPreferences userPreferences = new UserPreferences(this);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if high contrast turn on, force app to dark mode
        if(userPreferences.getHighContrast()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        db = ClassDatabase.getDatabase(this);

        if (userPreferences.getUserLoggedIn()) {
            Boolean connected = null;
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                //we are connected to a network
                connected = true;
            }
            else
                connected = false;

            if(connected){
                APIManager apiManager = new APIManager(SplashActivity.this);
                apiManager.getUserClasses(userPreferences.getUserToken(), new NetworkHelper.getUserClasses() {
                    @Override
                    public void onResponse(Boolean success, Map<String, Object>[] userClasses) {
                        Log.d("ngak bisa",success+"");
                        if(success){
                            Log.d("HORE", "CELAN");
                            int len = userClasses.length;
                            for (int i=0;i<len;i++) {
                                // untuk masuk ke live streaming pake session_id
                                int sessionId = (int) userClasses[i].get("session_id");
                                // course_id -> EEEK2222
                                String courseId = userClasses[i].get("course_id").toString();
                                // course_name -> Introduction to Art of Manipulation
                                String courseName = (String) userClasses[i].get("course_name");
                                // session_th -> session ke berapa
                                int sessionTh = (int) userClasses[i].get("session_th");
                                // session_mode -> LEC, CL, LAB, blablabla
                                String sessionMode = (String) userClasses[i].get("session_mode");
                                // class_name -> LA88
                                String className = (String) userClasses[i].get("class_name");
                                // topic_title -> How to manipulate someone's mind with ease
                                String topicTitle = (String) userClasses[i].get("topic_title");
                                // topic_description -> deskripsi singkat tentang topik yang bersangkutan
                                String topicDescription = (String) userClasses[i].get("topic_description");
                                // session_campus -> Anggrek, Kijang, Syahdan, blablabla
                                String sessionCampus = (String) userClasses[i].get("session_campus");
                                // session_room -> 400
                                String sessionRoom = (String) userClasses[i].get("session_room");
                                // lecturer_id -> D8888
                                String lecturerId = userClasses[i].get("lecturer_id").toString();
                                // lectuer_name -> Felik Orange
                                String lecturerName = (String) userClasses[i].get("lecturer_name");
                                // session_startdate -> waktu mulai
                                String sessionStartDate = userClasses[i].get("session_startdate").toString();
                                // session_enddate -> waktu selesai
                                String sessionEndDate = userClasses[i].get("session_enddate").toString();

                                insertData(new ClassEntity(sessionId,courseId,courseName,sessionTh,sessionMode,className,topicTitle,topicDescription,sessionCampus,sessionRoom,lecturerId,lecturerName,sessionStartDate,sessionEndDate));
                            }
                        }
                    }

                    @Override
                    public void onError(int errorCode, String errorReason) {
                    }
                });
            }
            else{
                if(userPreferences.getUserPrivilege() == 1){
                    Intent intent = new Intent(SplashActivity.this, com.ambinusian.adab.ui.lecturer.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, com.ambinusian.adab.ui.student.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }


        } else {
            startActivity(new Intent(SplashActivity.this, ActivityLogin.class));
            finish();
        }
    }

    private void insertData(final ClassEntity classList){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                // melakukan proses insert data
                db.classDAO().insertClass(classList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(userPreferences.getUserPrivilege() == 1){
                    Intent intent = new Intent(SplashActivity.this, com.ambinusian.adab.ui.lecturer.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, com.ambinusian.adab.ui.student.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                super.onPostExecute(aVoid);
            }
        }.execute();
    }
}
