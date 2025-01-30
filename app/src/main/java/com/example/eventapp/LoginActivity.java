package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapp.database.DatabaseHelper;
import com.example.eventapp.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private SignInButton googleSignInButton;
    private ProgressBar loadingView;
    private DatabaseHelper dbHelper;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        initViews();
        setupButtons();
        
        // 初始化数据库
        dbHelper = new DatabaseHelper(this);
        
        // 配置Google登录
        setupGoogleSignIn();
        
        // 检查是否已登录
        checkLoginStatus();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupGoogleSignIn() {
        // 配置Google登录选项
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // 从 google-services.json 获取
                .requestEmail()
                .build();

        // 创建Google登录客户端
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // 设置Google登录结果处理
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                });

        // 设置Google登录按钮点击事件
        googleSignInButton.setOnClickListener(v -> startGoogleSignIn());
    }

    private void startGoogleSignIn() {
        showLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // 获取用户信息
            String email = account.getEmail();
            String username = account.getDisplayName();
            String idToken = account.getIdToken();  // 获取 ID token
            
            Log.d(TAG, "Google login successful: " + email);
            
            // 检查用户是否已存在
            User existingUser = dbHelper.getUserByEmail(email);
            if (existingUser == null) {
                // 创建新用户
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setPassword(""); // Google登录用户无密码
                newUser.setStaff(false);
                
                long userId = dbHelper.addUser(newUser);
                if (userId != -1) {
                    saveLoginStatus(username, userId, false);
                    startMainActivity();
                } else {
                    Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Google登录用户统一设置为普通用户
                saveLoginStatus(existingUser.getUsername(), existingUser.getId(), false);
                startMainActivity();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google login failed: " + e.getStatusCode() + ", " + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()));
            Toast.makeText(this, "Google login failed: " + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()), Toast.LENGTH_SHORT).show();
        } finally {
            showLoading(false);
        }
    }

    private void saveLoginStatus(String username, long userId, boolean isStaff) {
        SharedPreferences prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        prefs.edit()
                .putString("username", username)
                .putLong("userId", userId)
                .putBoolean("isLoggedIn", true)
                .putBoolean("isStaff", isStaff)
                .apply();
    }

    private void setupButtons() {
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        new Thread(() -> {
            User user = dbHelper.getUser(username);
            runOnUiThread(() -> {
                if (user != null && user.getPassword().equals(password)) {
                    saveLoginStatus(username, user.getId(), user.isStaff());
                    startMainActivity();
                } else {
                    Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
                showLoading(false);
            });
        }).start();
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            startMainActivity();
            finish();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        googleSignInButton.setEnabled(!show);
    }
} 