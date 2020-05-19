package com.parse.starter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ViewProfile extends AppCompatActivity {

    ImageView imageView;
    EditText nameEditText,phoneEditText,emailEditTest;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        setTitle("Your Profile");
        hideKeyboard(this);

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEdiText);
        emailEditTest = findViewById(R.id.emailEditText);
        uploadProfile();
        save = findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                final ParseQuery<ParseUser> profile = ParseUser.getQuery();
                if (!nameEditText.getText().toString().equals("")&&!phoneEditText.getText().toString().equals("")&&!emailEditTest.getText().toString().equals("")) {
                    profile.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                    profile.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser object, ParseException e) {
                            if(object!=null){
                                object.put("username",nameEditText.getText().toString());
                                object.put("phone",phoneEditText.getText().toString());
                                object.put("email",emailEditTest.getText().toString());
                                object.saveInBackground();
                                Toast.makeText(getApplicationContext(),"Profile Updated",Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(getIntent());
                            }
                        }
                    });
                } else {
                    save.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    Toast.makeText(getApplicationContext(),"Fill all details",Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfile();
            }
        });
//        Bundle extras = getIntent().getExtras();
//        byte[] byteArray = extras.getByteArray("picture");
//        final Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView = (ImageView) findViewById(R.id.profilePic);
//        imageView.setImageBitmap(getCircleBitmap(bitmap));
//
//        imageView.setClickable(true);

        FloatingActionButton fabCategory = findViewById(R.id.photoUpload);
        fabCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    } else {
                        getPhoto();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);

                Log.i("Image Selected","Good Work!");
                //uploading our code to parse
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,40,stream);

                byte[] byteArray = stream.toByteArray();
                final ParseFile parseFile = new ParseFile("image.png",byteArray);

                final ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if(object!=null){
                            object.put("ProfilePic",parseFile);
                            object.saveInBackground();
                            Toast.makeText(getApplicationContext(),"Profile Picture Uploaded",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {


        //crop to circle
        Bitmap output;
        //check if its a rectangular image
        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(output);

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());


        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    public void getPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }
    public void uploadProfile(){
        ParseQuery<ParseUser> user = ParseUser.getQuery();
        user.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        user.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0) {
                        for (ParseUser object:objects){
                            nameEditText.setText(object.getUsername());
                            phoneEditText.setText(object.get("phone").toString());
                            emailEditTest.setText(object.getEmail());
                            ParseFile profilePic = object.getParseFile("ProfilePic");
                            profilePic.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {
                                    if (e==null && data != null) {
                                        Bitmap bp = BitmapFactory.decodeByteArray(data,0,data.length);
                                        imageView.setImageBitmap(getCircleBitmap(bp));
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Upload compressed image",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),"Upload a profile Pic",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    e.getMessage();
                }
            }
        });
    }
    public static void hideKeyboard(Context context) {
        try {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((((Activity) context).getCurrentFocus() != null) && (((Activity) context).getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
        if (key_code== KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(key_code, key_event);
            return true;
        }
        return false;
    }
}
