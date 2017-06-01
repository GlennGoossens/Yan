package com.glenngoossens.yan;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LOGIN";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText emailField, passwordField;
    private TextView changeSignUpModeTextView;
    private boolean signUpActive;
    private Button signUpButton;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("database");
    private ArrayList<String> emails = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = (EditText) findViewById(R.id.emailEditText);
        passwordField = (EditText) findViewById(R.id.passwordEditText);
        changeSignUpModeTextView = (TextView) findViewById(R.id.logInTextView);
        signUpActive = true;
        signUpButton = (Button) findViewById(R.id.signInButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpOrLogin(v);
            }
        });

        changeSignUpModeTextView.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed in");
                    Toast.makeText(getApplicationContext(),"USER IS ALREADY signed in : " + mAuth.getCurrentUser().getEmail(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),DeviceScanActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener !=null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void createAccount(final String email, String password) {
        myRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (email.equals(dataSnapshot1.getValue().toString())) {
                        //TODO change in informative texview
                        Toast.makeText(getApplicationContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                        signUpActive = false;
                        changeSignUpModeTextView.setText("Sign Up");
                        signUpButton.setText("Log In");
                        return;
                    }
                }
                Log.i(TAG, "onDataChange: " + emails.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO change in informative texview
                Toast.makeText(getApplicationContext(), "Failed to read data", Toast.LENGTH_SHORT).show();
            }
        });


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: create " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            //TODO change in informative texview
                            Toast.makeText(LoginActivity.this, "creation failed", Toast.LENGTH_SHORT).show();
                        } else {
                            //TODO change in informative texview
                            Toast.makeText(LoginActivity.this, "creation of user is successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser userFirebase = mAuth.getCurrentUser();
                            myRef.child("users").push().setValue(email);
                            Log.i(TAG, "CurrentUser creation: " + userFirebase.getEmail());
                        }
                    }
                });
    }


    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: login " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            //TODO change in informative texview
                            Toast.makeText(LoginActivity.this, "login failed", Toast.LENGTH_SHORT).show();
                        } else {
                            //TODO change in informative texview
                            Toast.makeText(LoginActivity.this, "user is logged in", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),DeviceScanActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }


    public void signUpOrLogin(View view) {
        Log.i(TAG, "signUpOrLogin: entering");
        String email = String.valueOf(emailField.getText());
        String pass = String.valueOf(passwordField.getText());
        if (email.equals("") || pass.equals("")) {
            //TODO change in informative texview
            Toast.makeText(LoginActivity.this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
        } else {
            Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
            Matcher m = p.matcher(email);
            boolean matchFound = m.matches();
            if (!matchFound) {
                //TODO change in informative texview
                Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                if (pass.length() < 6) {
                    //TODO change in informative texview
                    Toast.makeText(LoginActivity.this, "Password must have more than 6 digits", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (signUpActive) {
                    createAccount(email, pass);

                } else {
                    signIn(email, pass);
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logInTextView) {
            Log.i(TAG, "onClick: loginText");
            if (signUpActive) {
                signUpActive = false;
                changeSignUpModeTextView.setText("Sign Up");
                signUpButton.setText("Log In");

            } else {
                signUpActive = true;
                changeSignUpModeTextView.setText("Log In");
                signUpButton.setText("Sign Up");
            }
        }
    }
}
