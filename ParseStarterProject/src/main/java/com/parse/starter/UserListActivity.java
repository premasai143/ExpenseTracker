package com.parse.starter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private int pYear, pMonth, pDay;
    String date;

    TabLayout tabLayout;
    ViewPager viewPager;
    NavigationView navigationView;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        setTitle("Expense Manager");

        navigationView = findViewById(R.id.navView);
        final View header = navigationView.getHeaderView(0);
        final ImageView imageView = header.findViewById(R.id.imageView);

//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.profile);
//        imageView.setImageBitmap(getCircleBitmap(bitmap));
//        imageView.setClickable(true);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        final byte[] byteArray = stream.toByteArray();
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();//this will rotate the hamburger icon together with drawer itself

        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("ADD NEW"));
        tabLayout.addTab(tabLayout.newTab().setText("TODAY"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final MyAdapter myAdapter = new MyAdapter(this,getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(myAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (savedInstanceState == null){
            navigationView.setCheckedItem(R.id.home);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.feedback){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bavanasipremasai@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for the app");
            intent.putExtra(Intent.EXTRA_TEXT, "Your Feedback:-\n");
            startActivity(Intent.createChooser(intent,"Choose Mail App"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
//            super.onBackPressed();
            }
        } else {
            getSupportFragmentManager().popBackStack();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(UserListActivity.this,
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
                finish();
                startActivity(getIntent());
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
//                                Toast.makeText(UserListActivity.this,"Done!!!",Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                       .show();
                videoFragment();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void videoFragment(){
        DemoVideoFragment fragment = new DemoVideoFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.drawerLayout, fragment ).addToBackStack( "tag" );
        fragmentTransaction.commit();
    }

}
