package com.parse.starter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.parse.starter.TodayFragment.sendAmount;
import static com.parse.starter.TodayFragment.sendCategory;
import static com.parse.starter.TodayFragment.sendReason;
import static java.util.Arrays.asList;


public class UpdateFragment extends Fragment implements View.OnClickListener {

    Spinner spinner;
    EditText amountEditText,noteEditText;
    TextView textViewHeader;
    Button updateButton;
    String amount = null,category = null,reason = null;
    ArrayList<String> options = new ArrayList<>(asList("Select Category","Bills","Car","Clothes","Communications","Eating Out","Entertainment","Health","House","Toiletry","Transport"));

    public UpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        amount = sendAmount();
        category = sendCategory();
        reason = sendReason();
        return inflater.inflate(R.layout.fragment_update, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("Update an Expense");

        textViewHeader = getActivity().findViewById(R.id.textViewHeader);
        spinner = (Spinner) getActivity().findViewById(R.id.spinnerView);
        amountEditText = getActivity().findViewById(R.id.amount);
        noteEditText = getActivity().findViewById(R.id.note);

        ParseQuery<ParseObject> categories = ParseQuery.getQuery("Categories");
        categories.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        categories.whereNotEqualTo("category","Default");
        categories.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null&&objects.size()>0){
                    for (ParseObject update : objects){
                        options.add(update.getString("category"));
                    }
                }
            }
        });

        amountEditText.setText(amount);
        noteEditText.setText(reason);

        updateButton = (Button) getActivity().findViewById(R.id.updateButton);

        updateButton.setOnClickListener(this);
//        List<String> questions = Arrays.asList(getResources().getStringArray(R.array.categories));

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.spinner_item,options){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(spinnerArrayAdapter);

        if (category!=null){
            int spinnerPosition = spinnerArrayAdapter.getPosition(category);
            spinner.setSelection(spinnerPosition);
            spinnerArrayAdapter.notifyDataSetChanged();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    Toast.makeText(getContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);

        updateButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        final String currentDateandTime = sdf.format(new Date());
        final ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> check = ParseQuery.getQuery("Expenses");
        check.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        check.whereEqualTo("amount",amount);
        check.whereEqualTo("category",category);
        check.whereEqualTo("reason",reason);

        check.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null && objects.size()>0){
                    for (ParseObject expense:objects) {
                        expense.put("username",user.getUsername())  ;
                        expense.put("category",spinner.getSelectedItem());
                        expense.put("amount",amountEditText.getText().toString());
                        expense.put("reason",noteEditText.getText().toString());
//                        expense.put("date",currentDateandTime);
                        if (!spinner.getSelectedItem().equals("Select Category") && !amountEditText.getText().toString().equals("") && !noteEditText.getText().toString().equals("")){
                            expense.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e==null){
                                        Toast.makeText(getActivity(),"Expenditure Updated",Toast.LENGTH_LONG).show();
                                        Intent intent = getActivity().getIntent();
                                        getActivity().finish();
                                        startActivity(intent);
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else{
                            updateButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                            Toast.makeText(getActivity(),"Add all details",Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else {
                    updateButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    Toast.makeText(getActivity(),"Modify the details"+objects.size(),Toast.LENGTH_LONG).show();
                }
            }
        });
//        if (!spinner.getSelectedItem().equals("Select Category") && !amountEditText.getText().toString().equals("") && !noteEditText.getText().toString().equals("")){
//            expense.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e==null){
//                        Toast.makeText(getActivity(),"Expenditure Added",Toast.LENGTH_LONG).show();
//                        Intent intent = getActivity().getIntent();
//                        getActivity().finish();
//                        startActivity(intent);
//                    } else {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        } else{
//            Toast.makeText(getActivity(),"Add all details",Toast.LENGTH_LONG).show();
//        }
        //alternate checking for 2 values
//        if (spinner.getSelectedItem().equals("Select Category") && amountEditText.getText().toString().equals("")){
//            Toast.makeText(getActivity(),"Enter Amount & Category",Toast.LENGTH_LONG).show();
//        }
//        else if (spinner.getSelectedItem().equals("Select Category")) {
//            Toast.makeText(getActivity(),"Enter Category",Toast.LENGTH_LONG).show();
//        } else if (amountEditText.getText().toString().equals("")){
//            Toast.makeText(getActivity(),"Enter Amount",Toast.LENGTH_LONG).show();
//        } else {
//            expense.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e==null){
//                        Toast.makeText(getActivity(),"Expenditure Added",Toast.LENGTH_LONG).show();
//                    } else {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//        getActivity().getActionBar().setSelectedNavigationItem(1);
    }
}
