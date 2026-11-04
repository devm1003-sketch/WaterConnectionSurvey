package com.watersurvey.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    EditText name, phone, rooms, remarks;
    RadioGroup taxpayerGroup, houseGroup, slipsGroup;
    LinearLayout root, recordsBox;
    DB db;
    static final int CREATE_CSV = 41;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DB(this);
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(1); root.setPadding(28,24,28,28);
        root.setBackgroundColor(0xFFF6F8FB);
        scroll.addView(root); setContentView(scroll);
        TextView title = new TextView(this); title.setText("New Water Connection Survey");
        title.setTextSize(23); title.setTypeface(null, Typeface.BOLD); title.setTextColor(0xFF15324B);
        root.addView(title);
        TextView sub = new TextView(this); sub.setText("House-wise survey • One entry per house");
        sub.setTextSize(14); sub.setTextColor(0xFF526575); sub.setPadding(0,6,0,22); root.addView(sub);

        name = edit("Resident / House Owner Name", "Enter full name");
        phone = edit("Contact Number", "Enter mobile number");
        taxpayerGroup = choices("Taxpayer?", new String[]{"Yes","No"});
        houseGroup = choices("House Type", new String[]{"Ground","G+1"});
        rooms = edit("Total Number of Rooms", "Enter room count");
        rooms.setInputType(2);
        slipsGroup = choices("Number of Tax Slips", new String[]{"0","1","2"});
        remarks = edit("Remarks (Optional)", "Any additional information");

        Button save = button("Save House Record"); root.addView(save);
        save.setOnClickListener(v -> saveRecord());
        Button export = button("Export Survey to CSV"); root.addView(export);
        export.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("text/csv");
            i.putExtra(Intent.EXTRA_TITLE, "water_connection_survey.csv");
            startActivityForResult(i, CREATE_CSV);
        });
        TextView recordsTitle = new TextView(this); recordsTitle.setText("Saved Records");
        recordsTitle.setTextSize(19); recordsTitle.setTypeface(null, Typeface.BOLD);
        recordsTitle.setTextColor(0xFF15324B); recordsTitle.setPadding(0,24,0,8); root.addView(recordsTitle);
        recordsBox = new LinearLayout(this); recordsBox.setOrientation(1); root.addView(recordsBox);
        refreshRecords();
    }

    EditText edit(String label, String hint) {
        TextView l = new TextView(this); l.setText(label); l.setTextSize(14);
        l.setTypeface(null, Typeface.BOLD); l.setTextColor(0xFF263746); l.setPadding(0,12,0,5); root.addView(l);
        EditText e = new EditText(this); e.setSingleLine(label.equals("Contact Number") || label.equals("Total Number of Rooms"));
        e.setHint(hint); e.setTextSize(16); e.setPadding(14,8,14,8);
        e.setBackgroundColor(0xFFFFFFFF); root.addView(e); return e;
    }
    RadioGroup choices(String label, String[] opts) {
        TextView l = new TextView(this); l.setText(label); l.setTextSize(14);
        l.setTypeface(null, Typeface.BOLD); l.setTextColor(0xFF263746); l.setPadding(0,16,0,5); root.addView(l);
        RadioGroup g = new RadioGroup(this); g.setOrientation(RadioGroup.HORIZONTAL);
        for (String s: opts) { RadioButton r = new RadioButton(this); r.setText(s); r.setTextSize(15); g.addView(r); }
        root.addView(g); return g;
    }
    Button button(String text) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false);
        b.setTextSize(15); b.setPadding(8,8,8,8); return b;
    }
    String selected(RadioGroup g) {
        int id = g.getCheckedRadioButtonId();
        if (id == -1) return "";
        return ((RadioButton)findViewById(id)).getText().toString();
    }
    void saveRecord() {
        String n=name.getText().toString().trim(), p=phone.getText().toString().trim();
        String t=selected(taxpayerGroup), h=selected(houseGroup), r=rooms.getText().toString().trim(), s=selected(slipsGroup);
        if(n.isEmpty() || p.isEmpty() || t.isEmpty() || h.isEmpty() || r.isEmpty() || s.isEmpty()) {
            Toast.makeText(this,"Please complete all required fields.",Toast.LENGTH_LONG).show(); return;
        }
        if(!p.matches("[0-9+() -]{7,16}")) { Toast.makeText(this,"Please enter a valid contact number.",Toast.LENGTH_LONG).show(); return; }
        try { if(Integer.parseInt(r)<1) throw new Exception(); }
        catch(Exception ex) { Toast.makeText(this,"Enter a valid room count.",Toast.LENGTH_LONG).show(); return; }
        ContentValues cv=new ContentValues(); cv.put("name",n); cv.put("phone",p); cv.put("taxpayer",t);
        cv.put("house_type",h); cv.put("rooms",r); cv.put("slips",s); cv.put("remarks",remarks.getText().toString().trim());
        cv.put("created",new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date()));
        db.getWritableDatabase().insert("houses",null,cv);
        name.setText(""); phone.setText(""); rooms.setText(""); remarks.setText("");
        taxpayerGroup.clearCheck(); houseGroup.clearCheck(); slipsGroup.clearCheck();
        Toast.makeText(this,"House record saved on this phone.",Toast.LENGTH_SHORT).show(); refreshRecords();
    }
    void refreshRecords() {
        recordsBox.removeAllViews();
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM houses",null); c.moveToFirst();
        int count=c.getInt(0); c.close();
        TextView v=new TextView(this); v.setText("Total houses recorded: "+count); v.setTextSize(15); v.setTextColor(0xFF263746); recordsBox.addView(v);
        Cursor last=db.getReadableDatabase().rawQuery("SELECT name, house_type, rooms, slips FROM houses ORDER BY id DESC LIMIT 5",null);
        while(last.moveToNext()) {
            TextView row=new TextView(this);
            row.setText("• "+last.getString(0)+"  |  "+last.getString(1)+"  |  Rooms: "+last.getString(2)+"  |  Tax slips: "+last.getString(3));
            row.setTextSize(13); row.setTextColor(0xFF526575); row.setPadding(0,8,0,8); recordsBox.addView(row);
        } last.close();
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CREATE_CSV && resultCode==RESULT_OK && data!=null) {
            try {
                Uri uri=data.getData();
                java.io.OutputStream os=getContentResolver().openOutputStream(uri);
                StringBuilder out=new StringBuilder("Name,Contact Number,Taxpayer,House Type,Rooms,Tax Slips,Remarks,Created\n");
                Cursor c=db.getReadableDatabase().rawQuery("SELECT name,phone,taxpayer,house_type,rooms,slips,remarks,created FROM houses ORDER BY id",null);
                while(c.moveToNext()) {
                    for(int j=0;j<8;j++) { if(j>0) out.append(','); out.append(csv(c.getString(j))); }
                    out.append('\n');
                } c.close(); os.write(out.toString().getBytes("UTF-8")); os.close();
                Toast.makeText(this,"CSV exported successfully.",Toast.LENGTH_LONG).show();
            } catch(Exception e) { Toast.makeText(this,"Export failed: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
        }
    }
    String csv(String s) { if(s==null) return ""; return "\""+s.replace("\"","\"\"")+"\""; }

    static class DB extends SQLiteOpenHelper {
        DB(Context c) { super(c,"water_survey.db",null,1); }
        public void onCreate(SQLiteDatabase d) {
            d.execSQL("CREATE TABLE houses (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, taxpayer TEXT, house_type TEXT, rooms TEXT, slips TEXT, remarks TEXT, created TEXT)");
        }
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV) {}
    }
}
