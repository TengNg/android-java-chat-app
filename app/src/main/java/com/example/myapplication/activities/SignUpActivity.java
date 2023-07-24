package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        handleBackPressed();
        handleSignUp();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void handleSignUp() {
        binding.signUpButton.setOnClickListener(v -> {
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

        db.collection(Constant.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constant.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constant.KEY_NAME, binding.signUpUsernameInput.getText().toString());

                    // TODO: add FriendList field for each user sign up

                    // User:
                    // > docId: 3KzTK74xP440VHn7qRE1
                    // > name: dang tien
                    // > email: dtien@gmail.com

                    // HashMap<String, Boolean> friendId = new HashMap<>();
                    // friendId.put("3KzTK74xP440VHn7qRE1", true);

//                    db.collection(Constant.KEY_COLLECTION_USERS)
//                            .document(documentReference.getId())
//                            .collection(Constant.KEY_FRIENDS);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
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

        return true;
    }

}