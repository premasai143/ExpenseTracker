package com.parse.starter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static com.parse.starter.TodayFragment.amount;
import static com.parse.starter.TodayFragment.category;
import static com.parse.starter.TodayFragment.reason;
import static com.parse.starter.UserListActivity.getCircleBitmap;

public class ShowExpenses extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private int pYear, pMonth, pDay;
    NavigationView navigationView;

    TextView title,allExpenses;
    ExpandableListView seeExpenses;
    CustomAdapter listAdapter;

    ArrayList<GroupInfo> expenses  = new ArrayList<GroupInfo>();
    int sum = 0,id=-1;
    String date = null,date1=null,date2=null;

    ParseQuery<ParseObject> values;
    private LinkedHashMap<String, GroupInfo> categories = new LinkedHashMap<String, GroupInfo>();

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_expenses);

        setTitle("Expense Manager");

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

        drawer = findViewById(R.id.drawerLayoutView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();//this will rotate the hamburger icon together with drawer itself


        title = findViewById(R.id.titleView);
        allExpenses = findViewById(R.id.allExpensesView);
        seeExpenses = findViewById(R.id.seeExpensesView);

        id = getIntent().getIntExtra("reqCode",-1);
        date = getIntent().getStringExtra("date");
        date1 = getIntent().getStringExtra("date1");
        date2 = getIntent().getStringExtra("date2");
        String d = null;
        if (date1!=null&&date2!=null&&date1.compareTo(date2)>0){
            d=date1;
            date1=date2;
            date2=d;
        }


        if (id == 0){
            title.setText(date);
            setTitle("Daily Expenses");
            navigationView.setCheckedItem(R.id.day);
        }
        else if (id == 1){
            title.setText(date1+" to "+date2);
            setTitle("Weekly Expenses");
            navigationView.setCheckedItem(R.id.week);
        } else if (id == 2){
            title.setText(date1+" to "+date2);
            setTitle("Monthly Expenses");
            navigationView.setCheckedItem(R.id.month);
        }

        loadData();

        listAdapter = new CustomAdapter(this,expenses);
        seeExpenses.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
        seeExpenses.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //get the group header
                GroupInfo headerInfo = expenses.get(i);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getProductList().get(i1);
                //display it or do something with it
                Toast.makeText(getApplicationContext(), " Clicked on :: " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        seeExpenses.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //get the group header
                GroupInfo headerInfo = expenses.get(i);
                //display it or do something with it
                Toast.makeText(getApplicationContext(), " Viewing " + headerInfo.getName(),Toast.LENGTH_LONG).show();

                return false;
            }
        });
        registerForContextMenu(seeExpenses);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        Log.e("type",Integer.toString(type));
        menu.setHeaderTitle("Options");
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
            menu.add(0,1,0,"edit");
            menu.add(0,2,0,"delete");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();

        int groupPos = 0, childPos = 0;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        final GroupInfo headerInfo = expenses.get(groupPos);
        ChildInfo detailInfo = headerInfo.getProductList().get(childPos);

        //splitting note and amount
        //splitting note and amount
        String str = detailInfo.getName();
        final String[] splitString = str.split("\n",2);
        String str1 = headerInfo.getName();
//        final String[] categoryName = str1.split("[(]",2);

        values = ParseQuery.getQuery("Expenses");
        values.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        values.whereEqualTo("category",headerInfo.getName());
        values.whereEqualTo("amount",splitString[1].split(" - ")[0]);
        values.whereEqualTo("reason",splitString[1].split(" - ")[1]);

//        if (id == 0) {
//            values.whereEqualTo("date",date);
//        } else if (id == 1) {
//        } else if (id == 2) {
//
//        }

        if(item.getItemId()==1)
        {
            Log.i("clicked","edit");

            values.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null&&objects.size()>0){
                        for (ParseObject update : objects){
                            amount = update.getString("amount");
                            category = update.getString("category");
                            reason = update.getString("reason");
                            Log.i("details",amount+"-"+category+"-"+reason);
                            fragmentTransition();
                        }
                    }
                }
            });

        } else if (item.getItemId() == 2){
            Log.i("clicked","delete");

            final int finalChildPos = childPos;
            final int finalGroupPos = groupPos;
            values.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (final ParseObject object : objects) {
                                Log.i("category", object.getString("category"));
                                Log.i("amount", object.getString("amount"));
                                object.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        expenses.get(finalGroupPos).getProductList().remove(finalChildPos);
                                        if (expenses.get(finalGroupPos).getProductList().size()==0)
                                            expenses.remove(finalGroupPos);
                                        sum = sum - Integer.parseInt(splitString[1].split(" - ")[0]);
                                        allExpenses.setText("Total Expenses - "+sum+"/-");
                                        listAdapter.notifyDataSetChanged();
                                        seeExpenses.setAdapter(listAdapter);
                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                            }
                        }
                    }
                }
            });
            listAdapter.notifyDataSetChanged();
            Toast.makeText(this,headerInfo.getName()+"/"+detailInfo.getName() +" deleted",Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            seeExpenses.expandGroup(i);
        }
    }

    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            seeExpenses.collapseGroup(i);
        }
    }
    private void loadData(){

        values = ParseQuery.getQuery("Expenses");
        values.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        values.orderByDescending("date");
        values.orderByAscending("category");
        if (id == 0) {
            values.whereEqualTo("date",date);
        } else if (id == 1) {
            Log.i("date1",date1);
            Log.i("date2",date2);
            navigationView.setCheckedItem(R.id.week);
            values.whereGreaterThanOrEqualTo("date",date1);
            values.whereLessThanOrEqualTo("date",date2);
        } else if (id == 2) {
            navigationView.setCheckedItem(R.id.month);
            values.whereGreaterThanOrEqualTo("date",date1);
            values.whereLessThanOrEqualTo("date",date2);
        }
//        values.setLimit(1);
        values.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                        sum = 0;
                        for (ParseObject object : objects){
                            Log.i("category",object.getString("category"));
                            Log.i("amount",object.getString("amount"));
                            addProduct(object.getString("category"),object.getString("date")+"\n"+object.getString("amount")+" - "+object.getString("reason"));
                            sum = sum + Integer.parseInt(object.getString("amount"));
                        }
                        allExpenses.setText("Total Expenses - "+Integer.toString(sum)+"/-");
                    }
                }
            }
        });
    }

    //here we maintain our products in various departments
    private int addProduct(String department, String product){

        int groupPosition = 0;

        //check the hash map if the group already exists
        GroupInfo headerInfo = categories.get(department);
        //add the group if doesn't exists
        if(headerInfo == null){
            headerInfo = new GroupInfo();
            headerInfo.setName(department);
            categories.put(department, headerInfo);
            expenses.add(headerInfo);
        }

        //get the children for the group
        ArrayList<ChildInfo> productList = headerInfo.getProductList();
        //size of the children list
        int listSize = productList.size();
        //add to the counter
        listSize++;

        //create a new child and add that to the group
        ChildInfo detailInfo = new ChildInfo();
        detailInfo.setSequence(String.valueOf(listSize));
        detailInfo.setName(product);
        productList.add(detailInfo);
        headerInfo.setProductList(productList);

        //find the group position inside the list
        groupPosition = expenses.indexOf(headerInfo);
        listAdapter.notifyDataSetChanged();
        return groupPosition;
    }

    public void fragmentTransition(){
        UpdateFragment fragment = new UpdateFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.weekAndMonthView, fragment ).addToBackStack( "tag" );
        fragmentTransaction.commit();
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
        //super.onBackPressed();
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
            case R.id.month:
                in.putExtra("reqCode",2);
                startActivity(in);
                break;
            case R.id.home:
                Intent intent2 = new Intent(getApplicationContext(),UserListActivity.class);
                startActivity(intent2);
                break;
            case R.id.day:
                final Calendar c = Calendar.getInstance();
                pYear = c.get(Calendar.YEAR);
                pMonth = c.get(Calendar.MONTH);
                pDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(ShowExpenses.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                                String m =String.format("%02d", month+1);
//                                String d = String.format("%02d", dayOfMonth);
//                                String date=d+" "+(month)+" "+year;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year,month,dayOfMonth);
                                SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                                String date = format.format(calendar.getTime());
                                intent.putExtra("date",date);
                                intent.putExtra("reqCode",0);
                                startActivity(intent);
                                Log.i("date",date);
                            }
                        },pYear,pMonth,pDay);
                datePickerDialog.show();
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
//                        .setTitle("Expense Tracker")
//                        .setMessage("About")
//                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(ShowExpenses.this,"Done!!!",Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .setNegativeButton("No",null).show();
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
        fragmentTransaction.replace( R.id.drawerLayoutView, fragment ).addToBackStack( "tag" );
        fragmentTransaction.commit();
    }

}
