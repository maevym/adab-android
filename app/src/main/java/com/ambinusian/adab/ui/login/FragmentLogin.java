package com.ambinusian.adab.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.ambinusian.adab.R;
import com.ambinusian.adab.manager.APIManager;
import com.ambinusian.adab.manager.NetworkHelper;
import com.ambinusian.adab.preferences.UserPreferences;
import com.ambinusian.adab.room.ClassDatabase;
import com.ambinusian.adab.room.ClassEntity;
import com.ambinusian.adab.ui.student.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ambinusian.adab.ui.splash.SplashActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentLogin extends Fragment {

    private TextInputEditText inputNim;
    private TextInputEditText inputPassword;
    private TextView loginMessage;
    private MaterialButton btnLogin;
    private APIManager apiManager;
    private UserPreferences userPreferences;
    private ClassDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        btnLogin = view.findViewById(R.id.btn_login);
        inputNim = view.findViewById(R.id.input_nim);
        inputPassword = view.findViewById(R.id.input_password);
        loginMessage = view.findViewById(R.id.tv_login_message);
        userPreferences = new UserPreferences(getContext());
        apiManager = new APIManager(getContext());
        db = ClassDatabase.getDatabase(getContext());

        //set text attributes
        setTextSize();
        setTextTypeface();

        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(view1 -> Objects.requireNonNull(getActivity()).onBackPressed());

        btnLogin.setOnClickListener(view12 -> doLogin());
    }


    private void doLogin() {
        boolean valid = true;

        if (inputPassword.length() < 1) {
            valid = false;
            inputPassword.setError(getString(R.string.error_password_empty));
            inputPassword.requestFocus();
        }

        if (valid) {
            apiManager.authenticateUser(Objects.requireNonNull(inputNim.getText()).toString(), Objects.requireNonNull(inputPassword.getText()).toString(), new NetworkHelper.authenticateUser() {
                @Override
                public void onResponse(Boolean success, Map<String, Object> userProfile) {
                    if (success) {
                        userPreferences.setUserLoggedIn(true);
                        userPreferences.setUserToken((String) userProfile.get("session_id"));
                        Log.d("HORE", "CHAU");
                        getUserProfile();
//                        startActivity(new Intent(context, SplashActivity.class));

                    }
                }

                @Override
                public void onError(int errorCode, String errorReason) {
                    if (errorCode == 401) {
                        inputNim.setError(getString(R.string.username_or_password_invalid));
                        inputPassword.setError(getString(R.string.username_or_password_invalid));
                        inputNim.requestFocus();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void getUserProfile(){
        apiManager.getUserProfile(userPreferences.getUserToken(), new NetworkHelper.getUserProfile() {
            @Override
            public void onResponse(Boolean success, Map<String, Object> userProfile) {
                if (success) {
                    Log.d("HORE", "CE");
                    int privilege = (int) userProfile.get("is_staff");
                    String username = userProfile.get("user_id").toString();
                    String name = (String) userProfile.get("user_name");

                    userPreferences.setUserUsername(username);
                    userPreferences.setUserName(name);
                    userPreferences.setUserPrivilege(privilege);
                    userPreferences.setUserDepartement((String) userProfile.get("department"));
                    userPreferences.setUserPrivilege((int) userProfile.get("is_staff"));

                    getUserClasses();

                    startActivity(new Intent(getContext(), SplashActivity.class));
                    Objects.requireNonNull(getActivity()).finish();
                }
            }

            @Override
            public void onError(int errorCode, String errorReason) {
                Log.d("HORE", errorReason);
            }
        });
    }

    public void getUserClasses(){
        apiManager.getUserClasses(userPreferences.getUserToken(), new NetworkHelper.getUserClasses() {
                @Override
            public void onResponse(Boolean success, Map<String, Object>[] userClasses) {
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
                        String lecturerId = (String) userClasses[i].get("lecturer_id");
                        // lectuer_name -> Felik Orange
                        String lecturerName = (String) userClasses[i].get("lecturer_name");
                        // session_startdate -> waktu mulai
                        String sessionStartDate = (String) userClasses[i].get("session_startdate");
                        // session_enddate -> waktu selesai
                        String sessionEndDate = (String) userClasses[i].get("session_enddate");

                        insertData(new ClassEntity(sessionId,courseId,courseName,sessionTh,sessionMode,className,topicTitle,topicDescription,sessionCampus,sessionRoom,lecturerId,lecturerName,sessionStartDate,sessionEndDate));
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorReason) {

            }
        });
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
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void setTextSize(){
        //multiple of text size
        float textSize = userPreferences.getTextSize();
        //set text size for each text view
        loginMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX,loginMessage.getTextSize()*textSize);
        inputNim.setTextSize(TypedValue.COMPLEX_UNIT_PX,inputNim.getTextSize()*textSize);
        inputPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX,inputPassword.getTextSize()*textSize);
        btnLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX,btnLogin.getTextSize()*textSize);
    }

    private void setTextTypeface(){
        //get font type
        Typeface textTypeface = userPreferences.getTextTypeface();
        //set font type for each text view
        loginMessage.setTypeface(textTypeface, loginMessage.getTypeface().getStyle());
        inputNim.setTypeface(textTypeface, inputNim.getTypeface().getStyle());
        inputPassword.setTypeface(textTypeface, inputPassword.getTypeface().getStyle());
        btnLogin.setTypeface(textTypeface, btnLogin.getTypeface().getStyle());
    }
}
