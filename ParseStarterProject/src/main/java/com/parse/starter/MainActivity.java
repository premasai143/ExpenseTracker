/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;


public class MainActivity extends AppCompatActivity {

  TextView textView,textView1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setTitle("Expense Tracker");

    textView = findViewById(R.id.textView);
    textView1 = findViewById(R.id.textView1);
    textView.setTranslationX(-1000);textView1.setTranslationX(1000);
    textView.animate().translationXBy(1000).rotationBy(3600).setDuration(3000);
    textView1.animate().translationXBy(-1000).rotationBy(3600).setDuration(3000);

    Button button = (Button) findViewById(R.id.button);
    button.setTranslationY(-1000);
    button.animate().translationYBy(1000).rotationBy(3600).setDuration(3000);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
      }
    });

    ParseUser currentUser = ParseUser.getCurrentUser();
    if (currentUser != null) {
      Intent intent = new Intent(this,UserListActivity.class);

      startActivity(intent);
    }
    
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

  @Override
  public void onBackPressed() {
//    super.onBackPressed();
  }
}