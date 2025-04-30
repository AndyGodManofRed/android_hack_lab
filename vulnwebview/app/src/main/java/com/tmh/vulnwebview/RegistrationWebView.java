package com.tmh.vulnwebview;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

public class RegistrationWebView extends AppCompatActivity {
    private WebView webView;
    private static final String PREFS_NAME="UserPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_web_view);
        setTitle("Registration page");

        loadWebView();
    }

    private void loadWebView() {
        WebView webView = findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyApplication", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });

;
        webView.setWebViewClient(new WebViewClient());

        //Allows cross-origin requests from file:// scheme to access content from any origin
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        //Enabling javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 註冊 JavaScript 介面
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (getIntent().getExtras().getBoolean("is_reg", false)) {
            webView.loadUrl("file:///android_asset/registration.html");
        } else {
            webView.loadUrl(getIntent().getStringExtra("reg_url"));
        }
    }
    // JavaScript Interface
// JavaScript Interface
    public class WebAppInterface {
        private Context context;

        public WebAppInterface(Context c) {
            this.context = c;
        }

        @JavascriptInterface
        public void saveUser(String username, String password) {
            SharedPreferences sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String usersJson = sharedPref.getString("users", "[]");

            try {
                // 檢查帳號是否已存在
                JSONArray usersArray = new JSONArray(usersJson);
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject userObj = usersArray.getJSONObject(i);
                    if (userObj.getString("username").equals(username)) {
                        Log.d("WebApp", "註冊失敗，帳號已存在！");
                        showToast("註冊失敗！");
                        return;
                    }
                }

                // 創建新的帳號 JSON 物件
                JSONObject newUser = new JSONObject();
                newUser.put("username", username);
                newUser.put("password", password);
                usersArray.put(newUser);

                // 存入 SharedPreferences
                sharedPref.edit().putString("users", usersArray.toString()).apply();
                Log.d("WebApp", "成功新增帳號：" + username);

                // 顯示成功訊息並跳轉
                showToast("帳號新增成功！");
                navigateToMainActivity();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 顯示 Toast 訊息
        private void showToast(final String message) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }

        // 跳轉到 MainActivity
        private void navigateToMainActivity() {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 需要加這個，否則可能會發生錯誤
            context.startActivity(intent);
        }
    }


}