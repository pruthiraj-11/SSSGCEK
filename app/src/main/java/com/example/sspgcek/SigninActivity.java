package com.example.sspgcek;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sspgcek.databinding.ActivitySigninBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;
import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    ActivitySigninBinding binding;
    private ActivityResultLauncher<Intent> settingsLauncher;
    ProgressDialog dialog;
    FirebaseAuth auth;
    GoogleSignInClient gsic;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        AnimationDrawable animationDrawable = (AnimationDrawable)binding.signinlayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        requestPermissions();
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                                String idToken=googleSignInAccount.getIdToken();
                                AuthCredential authCredential=GoogleAuthProvider.getCredential(idToken,null);
                                FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                            finish();
                                        } else {
                                            Toast.makeText(SigninActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SigninActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (ApiException e) {
                                Log.e("fb", Objects.requireNonNull(e.getLocalizedMessage()));
                                e.printStackTrace();
                            }
                        }
                    }
                });

        settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                    }
                });
        auth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(SigninActivity.this);
        dialog.setTitle("Login");
        dialog.setMessage("Login to your account");
        GoogleSignInOptions googleSignInOptions =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();
        gsic = GoogleSignIn.getClient(SigninActivity.this, googleSignInOptions);
        binding.gin.setOnClickListener(view -> {
            Intent intent = gsic.getSignInIntent();
            startActivityForResult(intent, 74);
//            activityResultLauncher.launch(intent);
        });
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(SigninActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
            startActivity(new Intent(SigninActivity.this,ForgotPasswordActivity.class));
        });

        binding.guestlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signInAnonymously().addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    startActivity(new Intent(SigninActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                } else {
                                    Toast.makeText(SigninActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 74) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    String idToken=googleSignInAccount.getIdToken();
                    AuthCredential authCredential=GoogleAuthProvider.getCredential(idToken,null);
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            } else {
                                Toast.makeText(SigninActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SigninActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ApiException e) {
                    Log.e("fb", Objects.requireNonNull(e.getLocalizedMessage()));
                    e.printStackTrace();
                }
        }
    }

    private void requestPermissions() {
        Dexter.withContext(getApplicationContext())
                .withPermissions(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(SigninActivity.this, "All the permissions are granted.", Toast.LENGTH_SHORT).show();
                        }
                        else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);
        builder.setCancelable(false);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("Goto Settings", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            settingsLauncher.launch(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
//        new MaterialAlertDialogBuilder(getApplicationContext())
//                .setTitle("Need Permissions")
//                .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
//                .setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        settingsLauncher.launch(intent);
//                    }
//                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(), "Permission required to make app function properly.", Toast.LENGTH_SHORT).show();
//                    }
//                }).setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
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