package com.watersurvey.app;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {

    EditText name, phone, rooms;
    RadioGroup taxpayer, house, slips;

    public void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(25,25,25,25);
        scroll.addView(layout);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("NEW WATER CONNECTION SURVEY");
        title.setTextSize(22);
        layout.addView(title);

        name = field(layout,"Resident Name");
        phone = field(layout,"Contact Number");
        taxpayer = options(layout,"Taxpayer?", "Yes","No");
        house = options(layout,"House Type","Ground","G+1");
        rooms = field(layout,"Total Number of Rooms");
        slips = options(layout,"Number of Tax Slips","0","1","2");

        Button save = new Button(this);
        save.setText("Save Record");
        layout.addView(save);

        save.setOnClickListener(v ->
            Toast.makeText(this,
            "Form ready. Database setup is next.",
            Toast.LENGTH_LONG).show()
        );
    }

    EditText field(LinearLayout l,String label) {
        TextView t=new TextView(this);
        t.setText(label);
        l.addView(t);

        EditText e=new EditText(this);
        e.setHint(label);
        l.addView(e);
        return e;
    }

    RadioGroup options(LinearLayout l,String label,String... values) {
        TextView t=new TextView(this);
        t.setText(label);
        l.addView(t);

        RadioGroup g=new RadioGroup(this);
        g.setOrientation(0);

        for(String value:values) {
            RadioButton r=new RadioButton(this);
            r.setText(value);
            g.addView(r);
        }
        l.addView(g);
        return g;
    }
}
