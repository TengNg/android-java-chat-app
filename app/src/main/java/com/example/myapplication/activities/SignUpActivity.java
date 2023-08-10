package com.example.myapplication.activities;

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

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.example.myapplication.utilities.Validator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        handleBackPressed();
        handleSignUp();
        handlePickingProfileImage();
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
                            this.binding.profileImage.setImageBitmap(bitmap);
                            this.binding.addImageTextView.setVisibility(View.GONE);
                            this.encodedImage = this.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    private void handlePickingProfileImage() {
        this.binding.addImageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.pickImage.launch(intent);
        });
    }

    private void handleSignUp() {
        this.binding.signUpButton.setOnClickListener(v -> {
            if (isValidInput()) {
                signUp();
            }
        });
    }

    private void signUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constant.KEY_NAME, binding.signUpUsernameInput.getText().toString());
        user.put(Constant.KEY_EMAIL, binding.signUpEmailInput.getText().toString());
        user.put(Constant.KEY_PASSWORD, binding.signUpPasswordInput.getText().toString());
        user.put(Constant.KEY_IS_AVAILABLE, true);
        user.put(Constant.KEY_IMAGE, this.encodedImage);

        if (this.binding.maleRadioButton.isChecked()) {
            user.put(Constant.KEY_GENDER, this.binding.maleRadioButton.getText().toString());
        } else if (this.binding.femaleRadioButton.isChecked()) {
            user.put(Constant.KEY_GENDER, this.binding.femaleRadioButton.getText().toString());
        }

        user.put(Constant.KEY_IMAGE, this.encodedImage);

        db.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, this.binding.signUpEmailInput.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().isEmpty();
                        if (emailExists) {
                            this.showToast("Email exists");
                        } else {
                            this.showToast("Loading...");
                            DocumentReference newUserRef = db.collection(Constant.KEY_COLLECTION_USERS).document();
                            newUserRef.set(user);
                            this.preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                            this.preferenceManager.putString(Constant.KEY_USER_ID, newUserRef.getId());
                            this.preferenceManager.putString(Constant.KEY_NAME, binding.signUpUsernameInput.getText().toString());
                            this.preferenceManager.putString(Constant.KEY_EMAIL, binding.signUpEmailInput.getText().toString());
                            this.preferenceManager.putString(Constant.KEY_IMAGE, this.encodedImage);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
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
        binding.signInTextView.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private boolean isValidInput() {
        if (encodedImage == null) {
            showToast("Please select your profile image");
            return false;
        }

        if (binding.signUpUsernameInput.getText().toString().isEmpty()) {
            showToast("Please enter your username");
            return false;
        }

        if (binding.signUpEmailInput.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        }

        if (binding.signUpPasswordInput.getText().toString().isEmpty()) {
            showToast("Please enter your password");
            return false;
        }

        if (binding.signUpConfirmPasswordInput.getText().toString().isEmpty()) {
            showToast("Confirm your password");
            return false;
        }

        if (!binding.signUpConfirmPasswordInput.getText().toString().equals(binding.signUpPasswordInput.getText().toString())) {
            showToast("Password is not matched");
            return false;
        }

        if (this.binding.genderRadioGroup.getCheckedRadioButtonId() == -1) {
            showToast("Select your gender");
            return false;
        }

        if (Validator.isValidPassword(this.binding.signUpPasswordInput.getText().toString())) {
            showToast("Password must contain lowercase/uppercase characters and digits");
            return false;
        }

        return true;
    }

}