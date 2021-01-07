package com.ashu.eatit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.IpSecManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.BrainTreeToken;
import com.ashu.eatit.Model.UserModel;
import com.ashu.eatit.Remote.ICloudFunctions;
import com.ashu.eatit.Remote.RetrofitICloudClient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;
    private ICloudFunctions cloudFunctions;

    public static final String TAG = MainActivity.class.getSimpleName();

    private Place placeSelected;
    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

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
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build());

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
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
                                phoneLogin();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "You must give all permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();

    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance().
                        createSignInIntentBuilder()
                        .setLogo(R.drawable.logo)
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Failed to sign in!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //already exists user
                    FirebaseAuth.getInstance().getCurrentUser()
                            .getIdToken(true)
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show())
                            .addOnCompleteListener(tokenResultTask -> {

                                Common.authorizeKey = tokenResultTask.getResult().getToken();

                                dialog.dismiss();
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                goToHomeActivity(userModel);

                            });


                } else {
                    showRegisterDialog(user);
                    dialog.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Log.e(TAG, "onCancelled: " + error);
            }
        });
    }

    private void showRegisterDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout phone_input_layout = itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        TextView txt_address_detail = itemView.findViewById(R.id.txt_address_detail);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address_detail.setText(place.getAddress());


            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("PLACES API", "onError: " + status.getStatusMessage());
                Toast.makeText(MainActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        //set Data
        if (user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }
        else
            edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("REGISTER", (dialogInterface, i) -> {

            if (placeSelected != null) {

                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                UserModel userModel = new UserModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(txt_address_detail.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());
                userModel.setLat(placeSelected.getLatLng().latitude);
                userModel.setLng(placeSelected.getLatLng().longitude);

                userRef.child(user.getUid()).setValue(userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                FirebaseAuth.getInstance().getCurrentUser()
                                        .getIdToken(true)
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show())
                                        .addOnCompleteListener(tokenResultTask -> {

                                            Common.authorizeKey = tokenResultTask.getResult().getToken();
                                            Common.authorizeKey = tokenResultTask.getResult().getToken();

                                                        dialogInterface.dismiss();
                                                        Toast.makeText(MainActivity.this, "Registered", Toast.LENGTH_LONG).show();
                                                        goToHomeActivity(userModel);


                                        });

                            }
                        });
            } else {
                Toast.makeText(this, "Please select a address", Toast.LENGTH_SHORT).show();
            }

        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {

            Common.currentUser = userModel;
            Common.updateToken(MainActivity.this, task.getResult().getToken());
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        });


    }
}
