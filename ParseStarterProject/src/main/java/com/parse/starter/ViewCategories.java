package com.parse.starter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.parse.starter.UserListActivity.getCircleBitmap;
import static java.util.Arrays.asList;


public class ViewCategories extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    DrawerLayout drawer;
    private int pYear,pMonth,pDay;
    ArrayList<String> options1 = new ArrayList<>(asList("Select Category","Bills","Car","Clothes","Communications","Eating Out","Entertainment","Health","House","Toiletry","Transport"));
    ArrayList<String> options2 = new ArrayList<>();
    ListView listView1,listView2;
    ArrayAdapter<String> arrayAdapter1,arrayAdapter2;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_categories);

        setTitle("Your Categories");
        navigationView = findViewById(R.id.showNavView);
        View header = navigationView.getHeaderView(0);

        final ImageView imageView = header.findViewById(R.id.imageView);

        ParseQuery<ParseUser> profilePic = ParseUser.getQuery();
        profilePic.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        profilePic.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                        for (ParseUser object:objects){
                            final ParseFile parseFile = object.getParseFile("ProfilePic");
                            parseFile.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {
                                    if (e==null&&data!=null){
                                        Bitmap bp = BitmapFactory.decodeByteArray(data,0,data.length);
                                        imageView.setImageBitmap(getCircleBitmap(bp));
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ViewProfile.class);
                startActivity(intent);
            }
        });

        TextView username = header.findViewById(R.id.userName);
        username.setText(ParseUser.getCurrentUser().getUsername());

        TextView userId = header.findViewById(R.id.userId);
        userId.setText(ParseUser.getCurrentUser().getEmail());

        Toolbar toolbar = findViewById(R.id.toolbarView);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ParseQuery<ParseObject> categories = ParseQuery.getQuery("Categories");
        categories.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        categories.whereNotEqualTo("category","Default");
        categories.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null&&objects.size()>0){
                    for (ParseObject update : objects){
                        options2.add(update.getString("category"));
                        arrayAdapter2.notifyDataSetChanged();
                    }
                }
            }
        });
        Collections.sort(options1);
        Collections.sort(options2);

        options1.remove("Select Category");
        listView1 = findViewById(R.id.defaultCategories);
        listView2 = findViewById(R.id.personalCategories);
        arrayAdapter1 = new ArrayAdapter<>(this,R.layout.row,options1);
        arrayAdapter2 = new ArrayAdapter<>(this,R.layout.row,options2);
        listView1.setAdapter(arrayAdapter1);
        listView2.setAdapter(arrayAdapter2);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Default/" + options1.get(i), Toast.LENGTH_SHORT).show();
            }
        });
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Additional/" + options2.get(i), Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fabCategory = findViewById(R.id.fabCategory);
        fabCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewCategory.class);
                startActivity(intent);
            }
        });

    }
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final Intent intent = new Intent(getApplicationContext(),ShowExpenses.class);
        Intent in = new Intent(getApplicationContext(),DateRangePicker.class);
        switch (item.getItemId()){
            case R.id.week:
                in.putExtra("reqCode",1);
                startActivity(in);
                break;
            case R.id.day:
                final Calendar c = Calendar.getInstance();
                pYear = c.get(Calendar.YEAR);
                pMonth = c.get(Calendar.MONTH);
                pDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(ViewCategories.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                                String m =String.format("%02d", month+1);
//                                String d = String.format("%02d", dayOfMonth);
//                                String date=d+" "+(m)+" "+year;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year,month,dayOfMonth);
                                SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                                String date = format.format(calendar.getTime());
                                Log.i("date",date);
                                intent.putExtra("date",date);
                                intent.putExtra("reqCode",0);
                                startActivity(intent);
                            }
                        },pYear,pMonth,pDay);
                datePickerDialog.show();
                break;
            case R.id.month:
                in.putExtra("reqCode",2);
                startActivity(in);
                break;
            case R.id.home:
                startActivity(new Intent(getApplicationContext(),UserListActivity.class));
                break;
            case R.id.logout:
                ParseUser.logOut();
                Intent intent1 = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent1);
                break;
            case R.id.addCategory:
                Intent in1 = new Intent(getApplicationContext(),ViewCategories.class);
                startActivity(in1);
                break;
            case R.id.about:
//                new AlertDialog.Builder(this)
//                        .setIcon(android.R.drawable.ic_menu_info_details)
//                        .setTitle("About")
//                        .setMessage("This app is used to note expenditures of a person under specified category, date & we can view daily, weekly, monthly expenses and we can edit, delete an expense by long click on the item.\nUsing this we can save time and money")
//                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(ViewCategories.this,"Done!!!",Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .show();
                videoFragment();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
    public void videoFragment(){
        DemoVideoFragment fragment = new DemoVideoFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.drawerLayout, fragment ).addToBackStack( "tag" );
        fragmentTransaction.commit();
    }

}
