package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    Boolean signUpModeActive = true;
    TextView loginTextView,textView;
    EditText passwordEditText,usernameEditText;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Login");

        hideKeyboard(this);

        textView = (TextView) findViewById(R.id.textViewHeader);
        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(this);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        ImageView logoImageView = (ImageView) findViewById(R.id.logoImageView);
        ConstraintLayout backgroundLayout = (ConstraintLayout) findViewById(R.id.backgroundLayout);
        logoImageView.setOnClickListener(this);
        backgroundLayout.setOnClickListener(this);

        passwordEditText.setOnKeyListener(this);

//        if (ParseUser.getCurrentUser()!=null){
//            showUSerList();
//        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.loginTextView) {
            signUpButton = (Button) findViewById(R.id.signUpButton);

            if (signUpModeActive) {
                signUpModeActive = false;
                signUpButton.setText("Login");
                textView.setText("Don't have an account?");
                loginTextView.setText("Sign Up");
            } else {
                signUpModeActive = true;
                signUpButton.setText("Sign Up");
                textView.setText("Have an account?");
                loginTextView.setText("Login");
            }
        } else if (view.getId() == R.id.logoImageView || view.getId() == R.id.backgroundLayout){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            signUpClicked(view);
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        if (key_code== KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(key_code, key_event);
            return true;
        }
        return false;
    }
    public void signUpClicked(View view){

        hideKeyboard(this);
        if (usernameEditText.getText().toString().matches("") | passwordEditText.getText().toString().matches("")){
            Toast.makeText(this,"Username and Password are required",Toast.LENGTH_SHORT).show();
        } else {
            if (signUpModeActive) {
                //Sign Up
                ParseUser user = new ParseUser();
                user.setUsername(usernameEditText.getText().toString());
                user.setPassword(passwordEditText.getText().toString());
                user.setEmail(usernameEditText.getText().toString()+"@expensetracker");
                user.put("phone","");

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("SignUp", "Success");
                            Toast.makeText(getApplicationContext(), "User Created ", Toast.LENGTH_SHORT).show();
                            ParseObject categories = new ParseObject("Categories");
                            categories.put("username",usernameEditText.getText().toString());
                            categories.put("category","Default");
                            categories.saveInBackground();

                            Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,40,stream);
                            final byte[] byteArray = stream.toByteArray();

                            final ParseFile parseFile = new ParseFile("image.png",byteArray);
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("username",usernameEditText.getText().toString());
                            query.getFirstInBackground(new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser object, ParseException e) {
                                    if(object!=null){
                                        object.put("ProfilePic",parseFile);
                                        object.saveInBackground();

                                        finish();
                                        startActivity(getIntent());
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                //Login
                ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null){
                            Toast.makeText(LoginActivity.this,ParseUser.getCurrentUser().getUsername()+" Log in Successful!", Toast.LENGTH_SHORT).show();
                            showUSerList();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
    public void showUSerList() {
        Intent intent = new Intent(getApplicationContext(),UserListActivity.class);//going to dashboard activity
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
    private static void hideKeyboard(Context context) {
        try {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((((Activity) context).getCurrentFocus() != null) && (((Activity) context).getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
