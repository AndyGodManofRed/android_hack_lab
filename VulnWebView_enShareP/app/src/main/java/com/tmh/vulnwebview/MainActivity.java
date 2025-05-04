package com.tmh.vulnwebview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
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

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1337;
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveUser("andy", "1234");  // 建立預設帳號

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
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

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (isValiUser(username, password)) {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    private SharedPreferences getSecurePrefs() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveUser(String username, String password) {
        SharedPreferences securePrefs = getSecurePrefs();
        if (securePrefs == null) return;

        String usersJson = securePrefs.getString("users", "[]");

        try {
            JSONArray usersArray = new JSONArray(usersJson);

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObj = usersArray.getJSONObject(i);
                if (userObj.getString("username").equals(username)) {
                    Log.d("MA", "saveUser: the account name exists");
                    return;
                }
            }

            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("password", password);
            usersArray.put(newUser);

            securePrefs.edit().putString("users", usersArray.toString()).apply();
            Log.d("MA", "saveUser: Added new account: " + username);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isValiUser(String username, String password) {
        SharedPreferences securePrefs = getSecurePrefs();
        if (securePrefs == null) return false;

        String usersStr = securePrefs.getString("users", "[]");

        try {
            Log.d("MA", "【開始比對帳號】使用者輸入 username: [" + username + "]");
            Log.d("MA", "使用者輸入 password: [" + password + "]");
            Log.d("MA", "目前儲存 users 資料: " + usersStr);
            JSONArray users = new JSONArray(usersStr);
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("username").equals(username) &&
                        user.getString("password").equals(password)) {
                    Log.d("MA", "isValiUser: " + username + " is valid");
                    return true;
                }
            }
            Log.d("MA", "isValiUser: " + username + " is not found");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
