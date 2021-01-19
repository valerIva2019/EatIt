package com.ashu.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashu.eatit.Common.Common;
import com.ashu.eatit.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {
    private static int LOGIN_STATUS = 0;
    private FirebaseAuth mAuth;
    String mVerificationId;
    private DatabaseReference userRef;
    private android.app.AlertDialog dialog;

    private long timeCountInMilliSeconds = 60000;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    //Add this on top where other variables are declared
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.parent)
    RelativeLayout parent;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_info)
    TextView login_info;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.otp_text_input)
    TextInputLayout otp_text_input;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.phone_text_input)
    TextInputLayout phone_text_input;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_phone_number)
    EditText edt_phone_number;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_otp)
    EditText edt_otp;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.change_number)
    TextView change_number;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_login)
    TextView txt_login;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.otp_layout)
    LinearLayout otp_layout;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_login)
    LinearLayout btn_login;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.resendOtp_linear_layout)
    LinearLayout resendOTP;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.timer_layout)
    RelativeLayout timerLayout;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.textViewTime)
    TextView textViewTime;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view)
    View view;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_login)
    void sendOTP() {

            if (LOGIN_STATUS == 0) {
                if (edt_phone_number.getText().toString().length() < 10) {
                    phone_text_input.setError("Provide a valid number");
                } else {
                    dialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91" + edt_phone_number.getText().toString(),        // Phone number to verify
                            1,                 // Timeout duration
                            TimeUnit.MINUTES,   // Unit of timeout
                            this,               // Activity (for callback binding)
                            mCallbacks);
                }
                       // OnVerificationStateChangedCallbacks
            } else {
                if (edt_otp.getText().toString().length() < 6) {
                    otp_text_input.setError("Provide a valid OTP");
                } else {
                    dialog.show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, edt_otp.getText().toString());
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkUserFromFirebase(task.getResult().getUser());
                                } else {
                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        otp_text_input.setError("Invalid OTP");
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
            }
        }
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.resendOtp_linear_layout)
    void onResendOtp() {
        if (timerStatus == TimerStatus.STOPPED) {
            dialog.dismiss();
            startCountDownTimer();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91" + edt_phone_number.getText().toString(),        // Phone number to verify
                    1,               // Timeout duration
                    TimeUnit.MINUTES,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks,         // OnVerificationStateChangedCallbacks
                    mResendToken);             // Force Resending Token from callbacks
        } else {
            Toast.makeText(getApplicationContext(), "Please wait ...", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.change_number)
    void changeNumber() {
        dialog.show();
        if (otp_text_input.isErrorEnabled()) {
            otp_text_input.setErrorEnabled(false);
        }

        LOGIN_STATUS = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change number")
                .setMessage("Do you really want to change the number?")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialog.show();
                    Common.selectedFood = null;
                    Common.categorySelected = null;
                    Common.currentUser = null;
                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
                        FirebaseAuth.getInstance().signOut();

                    otp_layout.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                    txt_login.setText("Send Otp");
                    login_info.setText("Please provide your phone number");
                    edt_phone_number.setText("");
                    edt_otp.setText("");
                    edt_phone_number.setEnabled(true);
                    dialogInterface.dismiss();
                    dialog.dismiss();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog changeDialog = builder.create();
        changeDialog.show();

        dialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        init();
        initFireBaseCallbacks();
        textWatchers();
    }

    private void textWatchers() {
        edt_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (phone_text_input.isErrorEnabled()) {
                    phone_text_input.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp_text_input.isErrorEnabled()) {
                    otp_text_input.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setMessage("Please Wait ...").setContext(this).build();
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        otp_layout.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
        txt_login.setText("Send Otp");
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private void initFireBaseCallbacks() {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserFromFirebase(task.getResult().getUser());
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                otp_text_input.setError("Invalid OTP");
                                dialog.dismiss();
                            }
                        }
                    }
                });
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onCodeSent(@NotNull String verificationId,
                                   @NotNull PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(LoginActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();
                LOGIN_STATUS = 1;
                edt_phone_number.setEnabled(false);
                login_info.setText("Please provide OTP sent to +91" + edt_phone_number.getText().toString().trim());
                txt_login.setText("Continue");
                otp_layout.setVisibility(View.VISIBLE);

                view.setVisibility(View.GONE);


                startCountDownTimer();
                mVerificationId = verificationId; //Add this line to save //verification Id
                mResendToken = token; //Add this line to save the resend token
                dialog.dismiss();

            }
        };
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //already exists user
                    FirebaseAuth.getInstance().getCurrentUser()
                            .getIdToken(true)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                            })
                            .addOnCompleteListener(tokenResultTask -> {

                                Common.authorizeKey = tokenResultTask.getResult().getToken();
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                goToHomeActivity(userModel);


                            });


                } else {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void goToHomeActivity(UserModel userModel) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    dialog.dismiss();
                    finish();
                }).addOnCompleteListener(task -> {

            Common.currentUser = userModel;
            Common.updateToken(LoginActivity.this, task.getResult().getToken());
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            dialog.dismiss();
            finish();
        });
    }

    private void startCountDownTimer() {
        resendOTP.setVisibility(View.VISIBLE);
        timerStatus = TimerStatus.STARTED;
        timerLayout.setVisibility(View.VISIBLE);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        CountDownTimer countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String hms1 = String.format(Locale.ENGLISH, "%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
                textViewTime.setText(hms1);

            }

            @Override
            public void onFinish() {
                timerStatus = TimerStatus.STOPPED;
                timerLayout.setVisibility(View.GONE);

            }

        }.start();
        countDownTimer.start();
    }
}