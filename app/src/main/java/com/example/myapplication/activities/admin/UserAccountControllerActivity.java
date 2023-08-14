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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
        this.handleOpenUserFriendList();
    }

    private void handleBackPressed() {
        this.binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void initialize() {
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = (User) getIntent().getSerializableExtra(Constant.KEY_USER);
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
            this.deleteAccount();
        });
    }

    private void deleteAccount() {
        CollectionReference usersCollection = db.collection(Constant.KEY_COLLECTION_USERS);
        DocumentReference userRef = usersCollection.document(this.currentUser.id);

        HashMap<String, Object> newData = new HashMap<>();
        newData.put(Constant.KEY_NAME, "Deleted user");
        newData.put(Constant.KEY_EMAIL, FieldValue.delete());
        newData.put(Constant.KEY_PASSWORD, FieldValue.delete());
        newData.put(Constant.KEY_IS_DELETED, true);

        Task<Void> deleteUserTask = userRef.update(newData);
        CollectionReference friendsCollection = userRef.collection(Constant.KEY_COLLECTION_USER_FRIENDS);

        friendsCollection.get().addOnCompleteListener(friendsTask -> {
            if (friendsTask.isSuccessful()) {
                QuerySnapshot friendsSnapshot = friendsTask.getResult();
                for (DocumentSnapshot friendDoc : friendsSnapshot.getDocuments()) {
                    String friendID = friendDoc.getId();
                    DocumentReference friendRef = usersCollection.document(friendID);
                    friendRef.collection(Constant.KEY_COLLECTION_USER_FRIENDS).document(this.currentUser.id).delete();
                }
            } else {
                this.showToast("Failed to delete this account");
            }
        });

        try {
            Tasks.await(deleteUserTask);
            this.showToast("Account deleted");
        } catch (Exception e) {
            this.showToast("Failed to delete this account");
        }
    }

    private void handleOpenUserFriendList() {
        this.binding.showFriendListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UserFriendListActivity.class);
            intent.putExtra(Constant.KEY_USER, this.currentUser);
            startActivity(intent);
        });
    }
}