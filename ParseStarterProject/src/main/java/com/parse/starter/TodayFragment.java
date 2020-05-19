package com.parse.starter;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class TodayFragment extends Fragment{

    TextView totalExpenses;
    ExpandableListView viewExpenses;
    Bundle bundle;

    int sum = 0;
    static String amount = null,category = null, reason = null;

    ArrayList<GroupInfo> expenses  = new ArrayList<GroupInfo>();
    CustomAdapter listAdapter;
    ParseQuery<ParseObject> values;
    private LinkedHashMap<String, GroupInfo> categories = new LinkedHashMap<String, GroupInfo>();


    public TodayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();

        totalExpenses = getActivity().findViewById(R.id.totalExpenses);
        viewExpenses = getActivity().findViewById(R.id.viewExpenses);


        listAdapter = new CustomAdapter(getActivity(),expenses);
        viewExpenses.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
        viewExpenses.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //get the group header
                GroupInfo headerInfo = expenses.get(i);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getProductList().get(i1);
                //display it or do something with it
                Toast.makeText(getActivity().getBaseContext(), " Clicked on :: " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        viewExpenses.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //get the group header
                GroupInfo headerInfo = expenses.get(i);
                //display it or do something with it
                Toast.makeText(getActivity().getBaseContext(), " Viewing " + headerInfo.getName(),Toast.LENGTH_LONG).show();

                return false;
            }
        });
        registerForContextMenu(viewExpenses);
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
        String str = detailInfo.getName();
        final String[] splitString = str.split("\n",2);
        String str1 = headerInfo.getName();
//        final String[] categoryName = str1.split("[(]",2);

        values = ParseQuery.getQuery("Expenses");
        values.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        values.whereEqualTo("category",headerInfo.getName());
        values.whereEqualTo("amount",splitString[1].split(" - ")[0]);
        values.whereEqualTo("reason",splitString[1].split(" - ")[1]);


        if(item.getItemId()==1)
        {
            Log.i("clicked","edit");
            Log.i("amount",splitString[1].split(" - ")[0]);
            Log.i("reason",splitString[1].split(" - ")[1]);
//            Log.i("category",categoryName[0]);
//            Log.i("date",categoryName[1].split("[)]")[0]);

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
                                        totalExpenses.setText("Total Expenses - "+sum+"/-");
//                                        if (viewExpenses !=null){
//                                            viewExpenses.invalidateViews();
//                                        }
                                        listAdapter.notifyDataSetChanged();
                                        getActivity().finish();
                                        startActivity(getActivity().getIntent());
                                    }
                                });
                            }
                        }
                    }
                }
            });
            listAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity().getBaseContext(),headerInfo.getName()+"/"+detailInfo.getName() +" deleted",Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            viewExpenses.expandGroup(i);
        }
    }

    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            viewExpenses.collapseGroup(i);
        }
    }
    private void loadData(){

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        final String currentDateandTime = sdf.format(new Date());

        values = ParseQuery.getQuery("Expenses");
        values.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        values.whereEqualTo("date",currentDateandTime);
        values.orderByDescending("date");
        values.orderByAscending("category");
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
                        totalExpenses.setText("Total Expenses - "+Integer.toString(sum)+"/-");
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
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.todayFragment, fragment ).addToBackStack( "tag" );
        fragmentTransaction.commit();
    }

    static public String sendAmount(){
        return amount;
    }
    static public String sendCategory(){
        return category;
    }
    static public String sendReason(){
        return reason;
    }
}