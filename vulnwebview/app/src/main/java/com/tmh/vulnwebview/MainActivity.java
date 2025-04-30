package com.tmh.vulnwebview;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1337;
    private static final String PREFS_NAME="UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveUser("vuln","webview");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                return;
            }
        }
    }

    public void startRegistration(View v) {
        Intent intent = new Intent(this, RegistrationWebView.class);
        intent.putExtra("is_reg", true);
        startActivity(intent);
    }

    public void doLogin(View v) {
        EditText usernameInput = findViewById(R.id.username);
        EditText passwordInput = findViewById(R.id.password);

        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();


        if (isValiUser(username,password)) {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }
    private void saveUser(String username, String password){
        SharedPreferences sharedPref=getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        String usersJson= sharedPref.getString("users", "[]");

        try{
            //check if account is exist
            JSONArray usersArray=new JSONArray(usersJson);
            //if not exist, add new acount
            for(int i=0;i<usersArray.length();i++){
                JSONObject userObj=usersArray.getJSONObject(i);
                if(userObj.getString("username").equals(username)){
                    Log.d("MA", "saveUser: the account name exist");
                    return;
                }
            }
            //use new account and password to create json object
            JSONObject newUser=new JSONObject();
            newUser.put("username",username);
            newUser.put("password",password);
            usersArray.put(newUser);

            //store json object into sharedpreference
            sharedPref.edit().putString("users",usersArray.toString()).apply();
            Log.d("MA", "saveUser: Add a new account: "+username);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    private boolean isValiUser(String username,String password){
        //get exist account
        SharedPreferences sharePref=getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        String usersStr=sharePref.getString("users","[]");

        try {
            // convert usersStr to JSONArray
            JSONArray users=new JSONArray(usersStr);
            //chech if user input account exist or not
            for(int i=0;i<users.length();i++){
                JSONObject user=users.getJSONObject(i);
                if(user.getString("username").equals(username)&&user.getString("password").equals(password)){
                    Log.d("MA", "isValidate: "+username+" is valid");
                    return true;
                }
            }
            Log.d("MA", "isValidate: "+username+"is not exist");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }
}