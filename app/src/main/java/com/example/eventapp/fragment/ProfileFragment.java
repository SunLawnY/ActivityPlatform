package com.example.eventapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.LoginActivity;
import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentProfileBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private TextView usernameText;
    private TextView userTypeText;
    private MaterialButton logoutButton;
    private SharedPreferences prefs;
    private FragmentProfileBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri photoUri;
    private String currentPhotoPath;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri imageUri = result.getData() != null ? result.getData().getData() : null;
                    if (imageUri != null) {
                        startImageCrop(imageUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoUri != null) {
                        startImageCrop(photoUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri croppedImageUri = result.getData() != null ? result.getData().getData() : null;
                    if (croppedImageUri != null) {
                        updateAvatar(croppedImageUri);
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在这里初始化SharedPreferences
        prefs = requireContext().getSharedPreferences("EventApp", Activity.MODE_PRIVATE);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // 设置头像点击事件
        binding.avatarImage.setOnClickListener(v -> checkPermissionAndPickImage());

        // 加载当前头像
        loadCurrentAvatar();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        usernameText = view.findViewById(R.id.usernameText);
        userTypeText = view.findViewById(R.id.userTypeText);
        logoutButton = view.findViewById(R.id.logoutButton);

        // 显示用户信息
        String username = prefs.getString("username", "");
        boolean isStaff = prefs.getBoolean("isStaff", false);
        usernameText.setText("用户名：" + username);
        userTypeText.setText("用户类型：" + (isStaff ? "工作人员" : "普通用户"));

        // 设置退出登录按钮
        logoutButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除所有用户数据
                    prefs.edit().clear().apply();
                    
                    // 跳转到登录界面
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }

    private void checkPermissionAndPickImage() {
        String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        };
        
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
        } else {
            showImagePickerDialog();
        }
    }

    private void showImagePickerDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择图片来源")
                .setItems(new String[]{"从相册选择", "拍照"}, (dialog, which) -> {
                    if (which == 0) {
                        pickImageFromGallery();
                    } else {
                        takePhoto();
                    }
                })
                .show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.eventapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePhotoLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir     /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void startImageCrop(Uri sourceUri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(sourceUri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", false);

            // 创建裁剪后的图片保存路径
            File cropFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "crop_" + System.currentTimeMillis() + ".jpg");
            Uri destinationUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.eventapp.fileprovider",
                    cropFile);

            // 设置输出URI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, destinationUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);

            // 授予URI权限
            List<ResolveInfo> resInfoList = requireContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                requireContext().grantUriPermission(packageName, sourceUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                requireContext().grantUriPermission(packageName, destinationUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            cropImageLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "裁剪功能不可用，直接使用原图: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateAvatar(sourceUri);
        }
    }

    private void updateAvatar(Uri imageUri) {
        try {
            // 使用应用的内部存储目录
            File privateDir = new File(requireContext().getFilesDir(), "avatars");
            if (!privateDir.exists()) {
                privateDir.mkdirs();
            }

            // 使用用户名作为文件名的一部分，确保不同用户的头像不会冲突
            String username = prefs.getString("username", "default");
            File destFile = new File(privateDir, "avatar_" + username + ".jpg");
            
            InputStream in = requireContext().getContentResolver().openInputStream(imageUri);
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();

            // 使用新的URI
            Uri savedUri = Uri.fromFile(destFile);

            // 使用Glide加载并显示头像
            Glide.with(this)
                    .load(savedUri)
                    .circleCrop()
                    .into(binding.avatarImage);
            
            // 保存头像路径到SharedPreferences
            prefs.edit().putString("avatar_path", destFile.getAbsolutePath()).apply();
            
            Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
            
            // TODO: 上传头像到服务器
            uploadAvatarToServer(savedUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "保存头像失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentAvatar() {
        // 从SharedPreferences加载保存的头像路径
        String savedAvatarPath = prefs.getString("avatar_path", null);
        if (savedAvatarPath != null) {
            File avatarFile = new File(savedAvatarPath);
            if (avatarFile.exists()) {
                Glide.with(this)
                        .load(avatarFile)
                        .circleCrop()
                        .error(R.drawable.default_avatar)
                        .into(binding.avatarImage);
            } else {
                // 如果文件不存在，加载默认头像
                loadDefaultAvatar();
            }
        } else {
            loadDefaultAvatar();
        }
    }

    private void loadDefaultAvatar() {
        Glide.with(this)
                .load(R.drawable.default_avatar)
                .circleCrop()
                .into(binding.avatarImage);
    }

    private void uploadAvatarToServer(Uri imageUri) {
        // TODO: 实现头像上传到服务器的逻辑
        Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                showImagePickerDialog();
            } else {
                Toast.makeText(requireContext(), "需要相机和存储权限才能更换头像", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 