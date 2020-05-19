package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class NewCategory extends AppCompatActivity {

    EditText name;
    Button button;
    ParseQuery<ParseObject> categories;
    ParseObject newCategory = new ParseObject("Categories");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        setTitle("New Category");
        hideKeyboard(this);
        name = findViewById(R.id.newName);
        button = findViewById(R.id.saveCategory);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) NewCategory.this.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(NewCategory.this.getCurrentFocus().getWindowToken(),0);
                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                if (name.getText().toString().isEmpty()){
                    button.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    Toast.makeText(getApplicationContext(),"Add a name",Toast.LENGTH_SHORT).show();
                }
                else {
                    categories = ParseQuery.getQuery("Categories");
                    categories.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    categories.whereNotEqualTo("category",name.getText().toString());
                    categories.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e==null && objects.size() > 0){
                                newCategory.put("username",ParseUser.getCurrentUser().getUsername());
                                newCategory.put("category",name.getText().toString());
                                newCategory.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e==null){
                                            Toast.makeText(getApplicationContext(),name.getText().toString()+" added",Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(),ViewCategories.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Specified category already exists",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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
}