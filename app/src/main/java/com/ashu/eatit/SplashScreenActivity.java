package com.ashu.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private DatabaseReference userRef;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_logo)
    ImageView img_logo;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        firebaseAuth = FirebaseAuth.getInstance();
       init();

    }

    private void init() {
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        permissions();
    }

    private void permissions() {
        listener = firebaseAuth -> Dexter.withActivity(this)
                .withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                //Already Login
                                checkUserFromFirebase(user);

                            } else {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                }, 900);
                            }
                        } else {

                            AlertDialog alert = new AlertDialog.Builder(SplashScreenActivity.this)
                                    .setTitle("Custom Alert")
                                    .setMessage("You must give all the permissions to use the app")
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        permissions();
                                        dialog.dismiss();
                                    }).create();
                            alert.show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                            finish();
                        }
                    }, 800);
                }).addOnCompleteListener(task -> {

            Common.currentUser = userModel;
            Common.updateToken(SplashScreenActivity.this, task.getResult().getToken());
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                    finish();
                }
            }, 800);

        });

    }

    private void checkUserFromFirebase(FirebaseUser user) {
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //already exists user
                    FirebaseAuth.getInstance().getCurrentUser()
                            .getIdToken(true)
                            .addOnFailureListener(e -> Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show())
                            .addOnCompleteListener(tokenResultTask -> {

                                Common.authorizeKey = tokenResultTask.getResult().getToken();
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                goToHomeActivity(userModel);

                            });


                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });
    }


}