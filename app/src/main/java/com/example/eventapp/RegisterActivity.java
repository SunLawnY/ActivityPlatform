package com.example.eventapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.eventapp.database.DatabaseHelper;
import com.example.eventapp.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private SwitchMaterial staffSwitch;
    private MaterialButton registerButton;
    private View loadingView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化视图和数据库
        initViews();
        setupToolbar();
        dbHelper = new DatabaseHelper(this);

        // 设置注册按钮点击事件
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        staffSwitch = findViewById(R.id.staffSwitch);
        registerButton = findViewById(R.id.registerButton);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        // 重置错误提示
        usernameInput.setError(null);
        emailInput.setError(null);
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);

        // 获取输入值
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        boolean isStaff = staffSwitch.isChecked();

        // 验证输入
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("请输入用户名");
            usernameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("请输入邮箱");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("请输入有效的邮箱地址");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("请输入密码");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("密码长度至少为6位");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("两次输入的密码不一致");
            confirmPasswordInput.requestFocus();
            return;
        }

        // 显示加载动画
        showLoading(true);

        // 检查用户名是否已存在
        if (dbHelper.getUser(username) != null) {
            usernameInput.setError("用户名已存在");
            usernameInput.requestFocus();
            showLoading(false);
            return;
        }

        // 检查邮箱是否已存在
        if (dbHelper.getUserByEmail(email) != null) {
            emailInput.setError("邮箱已被注册");
            emailInput.requestFocus();
            showLoading(false);
            return;
        }

        // 创建新用户
        User newUser = new User(username, password, email, isStaff);
        long userId = dbHelper.addUser(newUser);

        if (userId > 0) {
            // 注册成功
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 注册失败
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            showLoading(false);
        }
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
        usernameInput.setEnabled(!show);
        emailInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
        confirmPasswordInput.setEnabled(!show);
        staffSwitch.setEnabled(!show);
    }
} 