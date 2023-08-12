package com.example.myapplication.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityUserAccountControllerBinding;
import com.example.myapplication.models.User;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UserAccountControllerActivity extends AppCompatActivity {
    ActivityUserAccountControllerBinding binding;
    PreferenceManager preferenceManager;
    FirebaseFirestore db;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserAccountControllerBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.handleBackPressed();
        this.initialize();
        this.listenUserData();
        this.handleOpenUserProfileEditor();
        this.handleSetAdminRole();
        this.handleDeleteAccount();
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
        Log.d("AdminController > CurrentUserProfile", this.currentUser.toString());
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void getUserData() {
        this.binding.profileImageReceiverView.setImageBitmap(this.getUserImage(this.currentUser.image));
        this.binding.usernameTextView.setText(this.currentUser.name);
        this.binding.userEmailTextView.setText(this.currentUser.email);
        this.binding.userGenderTextView.setText(this.currentUser.gender);
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void listenUserData() {
        DocumentReference docRef = db.collection(Constant.KEY_COLLECTION_USERS).document(this.currentUser.id);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Error listening for document changes.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString(Constant.KEY_NAME);
                String email = snapshot.getString(Constant.KEY_EMAIL);
                String gender = snapshot.getString(Constant.KEY_GENDER);
                String image = snapshot.getString(Constant.KEY_IMAGE);
                boolean isAdmin = Boolean.TRUE.equals(snapshot.getBoolean(Constant.KEY_IS_ADMIN_ROLE));

                this.binding.userEmailTextView.setText(email);
                this.binding.usernameTextView.setText(name);
                this.binding.profileImageReceiverView.setImageBitmap(this.getUserImage(image));
                this.binding.userGenderTextView.setText(gender);

                if (isAdmin) {
                    this.binding.adminRoleTextView.setVisibility(View.VISIBLE);
                    this.binding.requestAdminRoleButton.setVisibility(View.GONE);
                } else {
                    this.binding.adminRoleTextView.setVisibility(View.INVISIBLE);
                }

                Log.d("EditUser > NewData", gender);
            } else {
                Log.d("Firestore", "Document does not exist.");
            }
        });
    }


    private void handleOpenUserProfileEditor() {
        this.binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserInformationActivity.class);
            intent.putExtra(Constant.KEY_USER, this.currentUser);
            startActivity(intent);
        });
    }

    private void handleSetAdminRole() {
        this.binding.requestAdminRoleButton.setOnClickListener(v -> {
            DocumentReference dr = db.collection(Constant.KEY_COLLECTION_USERS)
                    .document(this.currentUser.id);

            HashMap<String, Object> newData = new HashMap<>();
            newData.put(Constant.KEY_IS_ADMIN_ROLE, true);
            dr.update(newData)
                    .addOnSuccessListener(unused -> {
                        this.showToast("This account is set to admin role");
                    })
                    .addOnFailureListener(e -> {
                        this.showToast("Unable to set this account to admin role");
                    });
        });
    }

    private void handleDeleteAccount() {
        this.binding.deleteAccountButton.setOnClickListener(v -> {
            this.db.collection(Constant.KEY_COLLECTION_USERS).document(this.currentUser.id)
                    .delete()
                    .addOnSuccessListener(task -> {
                        this.showToast("Account deleted");
                        Intent intent = new Intent(getApplicationContext(), AdminControllerActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        this.showToast("Failed to delete this account");
                    });
        });
    }
}