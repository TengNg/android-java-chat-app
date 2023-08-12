package com.example.myapplication.activities.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCreateUserBinding;
import com.example.myapplication.utilities.Constant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class CreateUserActivity extends AppCompatActivity {
    private ActivityCreateUserBinding binding;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityCreateUserBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
        this.handlePickingProfileImage();
        this.handleCreateNewUser();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            this.binding.profileNewUserImage.setImageBitmap(bitmap);
                            this.binding.addNewUserImageTextView.setVisibility(View.GONE);
                            this.encodedImage = this.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    private void handlePickingProfileImage() {
        this.binding.addNewUserImageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.pickImage.launch(intent);
        });
    }

    private void handleCreateNewUser() {
        this.binding.createNewUserButton.setOnClickListener(v -> {
            if (this.isValidInput()) {
                createNewUser();
            }
        });
    }

    private void createNewUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constant.KEY_NAME, binding.createNewUsernameInput.getText().toString());
        user.put(Constant.KEY_EMAIL, binding.createNewUserEmailInput.getText().toString());
        user.put(Constant.KEY_PASSWORD, binding.signUpPasswordInput.getText().toString());
        user.put(Constant.KEY_IMAGE, this.encodedImage);

        if (this.binding.maleUserRadioButton.isChecked()) {
            user.put(Constant.KEY_GENDER, this.binding.maleUserRadioButton.getText().toString());
        } else if (this.binding.femaleUserRadioButton.isChecked()) {
            user.put(Constant.KEY_GENDER, this.binding.femaleUserRadioButton.getText().toString());
        }

        if (this.encodedImage == null) {
            Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_user_image);
            this.encodedImage = this.encodeImage(icon);
        }

        user.put(Constant.KEY_IMAGE, this.encodedImage);

        db.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, this.binding.createNewUserEmailInput.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().isEmpty();
                        if (emailExists) {
                            this.showToast("Email exists");
                        } else {
                            DocumentReference newUserRef = db.collection(Constant.KEY_COLLECTION_USERS).document();
                            newUserRef.set(user);
                            this.showToast("New user is created");
                            onBackPressed();
                        }
                    } else {
                        this.showToast("Error");
                    }
                })
                .addOnFailureListener(ex -> {
                    showToast(ex.getMessage());
                });
    }

    private void handleBackPressed() {
        binding.backButtonAd.setOnClickListener(v -> onBackPressed());
    }

    private boolean isValidInput() {
        if (binding.createNewUsernameInput.getText().toString().isEmpty()) {
            showToast("Please enter your username");
            return false;
        }

        if (binding.createNewUserEmailInput.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        }

        if (binding.signUpPasswordInput.getText().toString().isEmpty()) {
            showToast("Please enter your password");
            return false;
        }

        if (this.binding.genderUserRadioGroup.getCheckedRadioButtonId() == -1) {
            showToast("Select your gender");
            return false;
        }

        return true;
    }


}