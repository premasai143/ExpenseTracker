package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static java.util.Arrays.asList;


public class AddNewFragment extends Fragment implements View.OnClickListener {

    static Spinner spinner;
    EditText amountEditText,noteEditText;
    TextView textViewHeader;
    Button addExpenseButton;
    ArrayList<String> options = new ArrayList<>(asList("Select Category","Bills","Car","Clothes","Communications","Eating Out","Entertainment","Health","House","Toiletry","Transport"));
    ArrayAdapter<String> spinnerArrayAdapter;

    public AddNewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        hideKeyboard(getActivity());
        textViewHeader = getActivity().findViewById(R.id.textViewHeader);
        spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        amountEditText = getActivity().findViewById(R.id.amountEditText);
        noteEditText = getActivity().findViewById(R.id.noteEditText);

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

        addExpenseButton = (Button) getActivity().findViewById(R.id.addButton);

        addExpenseButton.setOnClickListener(this);

        spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.spinner_item,options){
            @Override
            public boolean isEnabled(int position){
                if(position==0)
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

        addExpenseButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        final String currentDateandTime = sdf.format(new Date());
        final ParseUser user = ParseUser.getCurrentUser();
        final ParseObject expense = new ParseObject("Expenses");
        ParseQuery<ParseObject> check = ParseQuery.getQuery("Expenses");
        check.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        check.whereEqualTo("reason",noteEditText.getText().toString());

        check.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null && objects.size()==0){
                    expense.put("username",user.getUsername())  ;
                    expense.put("category",spinner.getSelectedItem());
                    expense.put("amount",amountEditText.getText().toString());
                    expense.put("reason",noteEditText.getText().toString());
                    expense.put("date",currentDateandTime);
                    Log.i("date",currentDateandTime);
                    if (!spinner.getSelectedItem().equals("Select Category") && !amountEditText.getText().toString().equals("") && !noteEditText.getText().toString().equals("")){
                        expense.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e==null){
                                    Toast.makeText(getActivity().getBaseContext(),"Expenditure Added",Toast.LENGTH_LONG).show();
                                    Intent intent = getActivity().getIntent();
                                    getActivity().finish();
                                    startActivity(intent);
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else{
                        addExpenseButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                        Toast.makeText(getActivity(),"Add all details",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    addExpenseButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                    Toast.makeText(getActivity(),"Reason already existed for some category, modify the reason",Toast.LENGTH_LONG).show();
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

    public static void showKeyboard(Context context) {
        ((InputMethodManager) (context).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
