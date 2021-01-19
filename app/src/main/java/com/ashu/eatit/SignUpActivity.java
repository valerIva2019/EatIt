package com.ashu.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.UserModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class SignUpActivity extends AppCompatActivity {

    FirebaseUser user;
    android.app.AlertDialog dialog;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.name_text_input)
    TextInputLayout name_text_input;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.email_text_input)
    TextInputLayout email_text_input;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.address_text_input)
    TextInputLayout address_text_input;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_name)
    TextInputEditText edt_name;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_address)
    TextInputEditText edt_address;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_email)
    TextInputEditText edt_email;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_phone)
    TextInputEditText edt_phone;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_continue)
    LinearLayout btn_continue;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.change_number)
    TextView change_number;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.change_number)
    void changeNumber() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to log out ?")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Common.selectedFood = null;
                    Common.categorySelected = null;
                    Common.currentUser = null;
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog changeDialog = builder.create();
        changeDialog.show();
    }

    private Place placeSelected;
    AutocompleteSupportFragment places_fragment;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

    private DatabaseReference userRef;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_continue)
    void continueClick() {
        if (edt_name.getText().toString().trim().length() < 3)
            name_text_input.setError("Provide a valid name");
        else if (!isValidEmail(edt_email.getText().toString()))
            email_text_input.setError("Provide a valid email address");
        else if (edt_address.getText().toString().length() < 8)
            address_text_input.setError("Provide a valid address");
        else if (getLocationFromAddress(edt_address.getText().toString()) == null)
            address_text_input.setError("Provide a valid address");
        else {
            dialog.show();
            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(user.getPhoneNumber());
            userModel.setEmail(edt_email.getText().toString());
            userModel.setLat(getLocationFromAddress(edt_address.getText().toString()).latitude);
            userModel.setLng(getLocationFromAddress(edt_address.getText().toString()).longitude);

            userRef.child(user.getUid()).setValue(userModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            FirebaseAuth.getInstance().getCurrentUser()
                                    .getIdToken(true)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(SignUpActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    })
                                    .addOnCompleteListener(tokenResultTask -> {

                                        Common.authorizeKey = tokenResultTask.getResult().getToken();
                                        Common.authorizeKey = tokenResultTask.getResult().getToken();

                                        Toast.makeText(SignUpActivity.this, "Registered", Toast.LENGTH_LONG).show();
                                        goToHomeActivity(userModel);


                                    });

                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);
        init();
        textWatchers();


    }

    private void textWatchers() {
        edt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (name_text_input.isErrorEnabled())
                    name_text_input.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (email_text_input.isErrorEnabled())
                    email_text_input.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (address_text_input.isErrorEnabled())
                    address_text_input.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private void init() {

        dialog = new SpotsDialog.Builder().setCancelable(false).setMessage("Please Wait ...").setContext(this).build();

        user = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);

        edt_phone.setText(user.getPhoneNumber());

        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setHint("Search your address");
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                edt_address.setText(place.getAddress());


            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("PLACES API", "onError: " + status.getStatusMessage());
                Toast.makeText(SignUpActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    dialog.dismiss();
                    startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {

            Common.currentUser = userModel;
            Common.updateToken(SignUpActivity.this, task.getResult().getToken());
            dialog.dismiss();
            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
            finish();
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}