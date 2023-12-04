package com.example.sspgcek;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sspgcek.databinding.ActivitySigninBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    ActivitySigninBinding binding;
    ProgressDialog dialog;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    EditText editTextemail;
    TextView closeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        AnimationDrawable animationDrawable = (AnimationDrawable)binding.signinlayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        closeTextView=null;
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                            if (signInAccountTask.isSuccessful()) {
                                Toast.makeText(SigninActivity.this,"Sign in successful",Toast.LENGTH_SHORT).show();
                                try {
                                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                                    if (googleSignInAccount != null) {
                                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(SigninActivity.this, task -> {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(SigninActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                                                Toast.makeText(SigninActivity.this,"Firebase authentication successful", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(SigninActivity.this,"Authentication Failed :" + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        final String[] passwordResetEmail = new String[1];
        final View[] view1 = {null};
        String default_web_client_id="";
        auth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(SigninActivity.this);
        dialog.setTitle("Login");
        dialog.setMessage("Login to your account");
//        try {
//            ApplicationInfo applicationInfo=getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
//            Object value = applicationInfo.metaData.get("defaultwebclientidValue");
//            if (value != null) {
//                default_web_client_id = value.toString();
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Toast.makeText(SigninActivity.this, e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
//        }
        GoogleSignInOptions googleSignInOptions =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(SigninActivity.this, googleSignInOptions);
        binding.gin.setOnClickListener((View.OnClickListener) view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 74);
//            activityResultLauncher.launch(intent);
        });
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(SigninActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }

        binding.newacc.setOnClickListener(v -> {
            startActivity(new Intent(SigninActivity.this, SignupActivity.class));
            finish();
        });

        binding.signinbtn.setOnClickListener(v -> {
            if(Objects.requireNonNull(binding.gmailfield.getText()).toString().isEmpty()){
                binding.gmailfield.setError("Please fill out this field.");
                return;
            }
            if(Objects.requireNonNull(binding.signinpass.getText()).toString().isEmpty()){
                binding.signinpass.setError("Please fill out this field.");
                return;
            }
            dialog.show();
            auth.signInWithEmailAndPassword(binding.gmailfield.getText().toString(),binding.signinpass.getText().toString())
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            startActivity(new Intent(SigninActivity.this,MainActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(SigninActivity.this, Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        binding.forgotpasssword.setOnClickListener(v -> {
            if (binding.viewStub.getParent()!=null) {
                view1[0] =binding.viewStub.inflate();
                editTextemail= (EditText) view1[0].findViewById(R.id.mailfieldpasswordreset);
                closeTextView= (TextView) view1[0].findViewById(R.id.backbtn);
                passwordReset(passwordResetEmail);
            } else {
                binding.viewStub.setVisibility(View.VISIBLE);
                editTextemail= (EditText) view1[0].findViewById(R.id.mailfieldpasswordreset);
                passwordReset(passwordResetEmail);
            }
        });

        if (closeTextView!=null) {
            binding.viewStub.setVisibility(View.GONE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.viewStub.getVisibility()==View.VISIBLE) {
                    binding.viewStub.setVisibility(View.GONE);
                    finish();
                } else {
                    finish();
                }
            }
        });
    }

    private void passwordReset(String[] passwordResetEmail){
        passwordResetEmail[0] =editTextemail.getText().toString().trim();
        if (TextUtils.isEmpty(passwordResetEmail[0])) {
            Toast.makeText(getApplication(), "Please fill out this field.", Toast.LENGTH_SHORT).show();
        } else {
            auth.sendPasswordResetEmail(passwordResetEmail[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplication(), "Email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 74) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                Toast.makeText(SigninActivity.this,"Sign in successful",Toast.LENGTH_SHORT).show();
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(SigninActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                                Toast.makeText(SigninActivity.this,"Firebase authentication successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SigninActivity.this,"Authentication Failed :" + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}