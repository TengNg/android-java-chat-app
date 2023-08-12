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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityEditUserInformationBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class EditUserInformationActivity extends AppCompatActivity {
    private ActivityEditUserInformationBinding binding;
    private User currentUser;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityEditUserInformationBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.initialize();
        this.handleBackPressed();
        this.getUserCurrentData();

        this.handlePickingProfileImage();
        this.handleCommitChanges();
    }

    private void initialize() {
        this.currentUser = ((User) getIntent().getSerializableExtra(Constant.KEY_USER));
        this.db = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void handleBackPressed() {
        this.binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void getUserCurrentData() {
        this.binding.editEmailInput.setText(this.currentUser.email);
        this.binding.editUsernameInput.setText(this.currentUser.name);

        if (this.currentUser.gender.equals("Male")) {
            this.binding.editMaleRadioButton.setChecked(true);
        } else {
            this.binding.editFemaleRadioButton.setChecked(true);
        }

        this.binding.editProfileImage.setImageBitmap(this.getUserImage(this.currentUser.image));
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

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
                            this.binding.editProfileImage.setImageBitmap(bitmap);
                            this.binding.editImageTextView.setVisibility(View.GONE);
                            this.currentUser.image = this.encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    private void handlePickingProfileImage() {
        this.binding.editProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.pickImage.launch(intent);
        });
    }

    private void handleCommitChanges() {
        this.binding.commitChangesButton.setOnClickListener(v -> {
            if (this.isValidInput()) {
                this.commitChanges();
            }
        });
    }

    private void commitChanges() {
        HashMap<String, Object> newData = new HashMap<>();
        newData.put(Constant.KEY_NAME, binding.editUsernameInput.getText().toString());

        if (!this.currentUser.email.equals(binding.editEmailInput.getText().toString())) {
            newData.put(Constant.KEY_EMAIL, this.currentUser.email);
        }

        newData.put(Constant.KEY_IMAGE, this.currentUser.image);

        if (this.binding.editMaleRadioButton.isChecked()) {
            newData.put(Constant.KEY_GENDER, this.binding.editMaleRadioButton.getText().toString());
        } else if (this.binding.editFemaleRadioButton.isChecked()) {
            newData.put(Constant.KEY_GENDER, this.binding.editFemaleRadioButton.getText().toString());
        }

        db.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, this.binding.editEmailInput.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().isEmpty();
                        if (emailExists) {
                            if (this.currentUser.email.equals(binding.editEmailInput.getText().toString())) {
                                this.db.collection(Constant.KEY_COLLECTION_USERS).document(this.currentUser.id).update(newData);
                                this.showToast("User data updated");
                                this.binding.editEmailInput.clearFocus();
                                this.binding.editUsernameInput.clearFocus();

                            } else {
                                this.showToast("Email exists");
                            }
                        } else {
                            this.db.collection(Constant.KEY_COLLECTION_USERS).document(this.currentUser.id).update(newData);
                            this.showToast("User data updated");
                            this.binding.editEmailInput.clearFocus();
                            this.binding.editUsernameInput.clearFocus();
                        }
                    } else {
                        this.showToast("Error");
                    }
                })
                .addOnFailureListener(ex -> {
                    showToast(ex.getMessage());
                });
    }

    private boolean isValidInput() {
        if (binding.editUsernameInput.getText().toString().isEmpty()) {
            showToast("Please enter your username");
            return false;
        }

        if (binding.editEmailInput.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        }

        if (this.binding.editGenderRadioGroup.getCheckedRadioButtonId() == -1) {
            showToast("Select your gender");
            return false;
        }

        return true;
    }

}