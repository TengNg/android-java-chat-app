package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignInBinding;
import com.example.myapplication.utilities.Constant;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        preferenceManager.clear();
        if (preferenceManager.getBoolean(Constant.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handleSignIn();
        handleGoToSignUpPage();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void handleGoToSignUpPage() {
        binding.createNewAccountTextView.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
    }

    public void handleSignIn() {
        binding.loginButton.setOnClickListener(v -> {
            if (isValidInput()) {
                signIn();
            }
        });
    }

    public void signIn() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.KEY_COLLECTION_USERS)
                .whereEqualTo(Constant.KEY_EMAIL, binding.loginEmailInput.getText().toString())
                .whereEqualTo(Constant.KEY_PASSWORD, binding.loginPasswordInput.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        this.preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN, true);
                        this.preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                        this.preferenceManager.putString(Constant.KEY_NAME, documentSnapshot.getString(Constant.KEY_NAME));

                        db.collection(Constant.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constant.KEY_USER_ID))
                                .update(Constant.KEY_IS_AVAILABLE, true);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        showToast("Unable to Login");
                    }
                });
    }

    public boolean isValidInput() {
        if (binding.loginEmailInput.getText().toString().isEmpty()) {
            showToast("Please enter your email");
            return false;
        }

        if (binding.loginPasswordInput.getText().toString().isEmpty()) {
            showToast("Please enter your password");
            return false;
        }

        return true;
    }

}