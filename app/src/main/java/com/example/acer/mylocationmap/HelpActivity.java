package com.example.acer.mylocationmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {

    ListView helplist;
    ArrayList<String> helpItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helplist = (ListView) findViewById(R.id.listviewhelp);
        helpItems.add("FAQ");
        helpItems.add("App info");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                helpItems);
        helplist.setAdapter(arrayAdapter);
        helplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item_selected = ((TextView) view).getText().toString();
                if(item_selected.equalsIgnoreCase("App info")){
                    Intent intent = new Intent(getBaseContext(), AppInfo.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(getBaseContext(), FAQ_activity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
