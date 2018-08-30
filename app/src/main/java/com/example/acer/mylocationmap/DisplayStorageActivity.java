package com.example.acer.mylocationmap;



        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.ActivityInfo;
        import android.location.Location;
        import android.preference.PreferenceManager;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Html;
        import android.text.Spanned;
        import android.util.SparseArray;
        import android.view.ContextMenu;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.BaseAdapter;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.gson.Gson;
        import com.google.gson.reflect.TypeToken;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.lang.reflect.Type;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.LinkedHashMap;
        import java.util.LinkedHashSet;
        import java.util.List;
        import java.util.Map;
        import java.util.Set;

public class DisplayStorageActivity extends AppCompatActivity {
    private ListView lv;
    ArrayList<String> parked_addrs = new ArrayList<>();
    AddressAndLocation al = new AddressAndLocation();
    double lati,longi;
    ArrayList<AddressAndLocation> arraylist = new ArrayList<>();
    ArrayListHelper arrayListHelper;

    CharSequence menuItems[] = new CharSequence[] {"Share","Return to place","Delete"};//"Details",

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_storage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        arrayListHelper = new ArrayListHelper(this);
        String adrs;
        arraylist= arrayListHelper.getArray();
        if(arraylist!=null){
           for(int i=0; i<arraylist.size();i++){
                al=arraylist.get(i);
                adrs=al.getAddrs();
                parked_addrs.add(adrs);
            }
            // This is the array adapter, it takes the context of the activity as a
            // first parameter, the type of list view as a second parameter and your
            // array as a third parameter.
            lv=(ListView) findViewById(R.id.listview);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    parked_addrs);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String item_selected = ((TextView) view).getText().toString();
                    dialogMenuItems(position,arrayAdapter, item_selected);
                    return false;
                }
            });
        }
    }

    private void dialogMenuItems(final int position, final ArrayAdapter<String> arrayAdapter, final String item_selected) {
        //Standard Dialog
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setItems(menuItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on item[which]
                switch (which){
                    case 0:{
                        try {
                            al=arraylist.get(position);
                            lati= al.getLocation().getLatitude();
                            longi = al.getLocation().getLongitude();
                            String url = "https://www.google.com/maps/search/?api=1&query=" + lati + "," + longi + "";

                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, "This message is from application under testing(AUT).Application is in the stage of maturity.PLEASE IGNORE \n"
                                    + url + "\nAddress : "+ item_selected);
                            startActivity(Intent.createChooser(sharingIntent,"Share using"));
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "SMS failed, please try again later!",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 1:{
                        al=arraylist.get(position);
                        lati= al.getLocation().getLatitude();
                        longi = al.getLocation().getLongitude();
                        Intent i = new Intent(getBaseContext(), MapsActivityRoute.class);
                        i.putExtra("Uniqid","From_Activity_Storage");
                        i.putExtra("lati",lati);
                        i.putExtra("longi", longi);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case 2:{
                        deleteItemDialog(position,arrayAdapter,item_selected);
                        dialog.dismiss();
//                        showDetailsDialog(position, item_selected);
                        break;
                    }
                }
            }
        });
        alertBuilder.show();
        AlertDialog ad = alertBuilder.create();
       // ad.show();
    }
    /*
        private void showDetailsDialog(int position, String item_selected) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
       //     String datt="<b>"+dates_list.get(position)+"</b>";
            String dat="<B>"+dataa_OLD.get(position).get("DATE")+"</B>";
            alertBuilder.setMessage("You visited "+item_selected+"\n"+"on "+ fromHtml(dat));//+"\n\n"+dat);
            alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog ad = alertBuilder.create();
            ad.show();
        }

        @SuppressWarnings("deprecation")
        public static Spanned fromHtml(String html){
            Spanned result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(html);
            }
            return result;
        }
    */
    public void deleteItemDialog(final int position, final ArrayAdapter<String> arrayAdapter, final String item_selected) {

        //Standard Dialog
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("This address will be removed.");
        alertBuilder.setMessage(item_selected);
        alertBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                arraylist.remove(position);
               // Toast.makeText(getBaseContext(),""+arraylist.size(),Toast.LENGTH_LONG).show();
                arrayListHelper.putArray(arraylist);
                //https://stackoverflow.com/questions/5497580/how-to-dynamically-remove-items-from-listview-on-a-button-click
                parked_addrs.remove(position);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        alertBuilder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog ad = alertBuilder.create();
        ad.show();
    }

}
