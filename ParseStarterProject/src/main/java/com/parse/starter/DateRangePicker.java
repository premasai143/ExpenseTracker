package com.parse.starter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangePicker extends AppCompatActivity {

    int i = 0,y=0,x=0;
    String date1=null,date2=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_range_picker);

        setTitle("Select Date Range");

        final Button button = findViewById(R.id.selectButton);
        Date today = new Date();
        date1 = today.toString();
        date2 = today.toString();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.DAY_OF_MONTH, 1);
        Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR,-1);
        final CalendarPickerView datePicker = findViewById(R.id.calendar);
        final Intent intent = new Intent(getApplicationContext(),ShowExpenses.class);

        datePicker.init(lastYear.getTime(), nextYear.getTime(), Locale.getDefault())
                .inMode(CalendarPickerView.SelectionMode.RANGE)
                .withSelectedDate(today);

        datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {

                //String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
                Calendar calSelected = Calendar.getInstance();
                calSelected.setTime(date);
//                String d = String.format("%02d", calSelected.get(Calendar.DAY_OF_MONTH));
//                String m = String.format("%02d", (calSelected.get(Calendar.MONTH) + 1));
//
//                String selectedDate = d
//                        + "-" + m
//                        + "-" + calSelected.get(Calendar.YEAR);

                int year = calSelected.get(Calendar.YEAR);
                int month = calSelected.get(Calendar.MONTH);
                int day = calSelected.get(Calendar.DAY_OF_MONTH);
                calSelected.set(year,month,day);
                SimpleDateFormat format =  new SimpleDateFormat("dd MMMM yyyy");
                String selectedDate = format.format(calSelected.getTime());

                if (i%2==0) {
//                    Log.i("date1", selectedDate+x);
                    date1 = selectedDate;
                    x=1;
                    intent.putExtra("date1",date1);
                } else {
//                    Log.i("date2",selectedDate+y);
                    date2 = selectedDate;
                    y=1;
                    intent.putExtra("date2",date2);
                }
                i++;
//                Toast.makeText(DateRangePicker.this, datePicker.getSelectedDate().toString(), Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onDateUnselected(Date date) {
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                if (!date1.equals("")&&!date2.equals("")&&y==1&&x==1&&date1.compareTo(date2)!=0){
                    intent.putExtra("reqCode",getIntent().getIntExtra("reqCode",-1));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),"Enter the range propoerly",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
