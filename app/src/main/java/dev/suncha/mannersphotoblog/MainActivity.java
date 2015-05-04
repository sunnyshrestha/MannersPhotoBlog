package dev.suncha.mannersphotoblog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    ListView list;
    TextView title, excerpt_tv;
    ImageView thumbnail;
    ImageLoader imageLoader = new ImageLoader(this);

    ArrayList<HashMap<String, String>> titleList = new ArrayList<HashMap<String, String>>();
    //URL to get JSON Array
    private static String url = "http://www.mannersphoto.com/api/get_recent_posts/";
    //JSON node names
    private static final String TAG_POSTS = "posts";
    private static final String TAG_TITLE = "title";
    private static final String TAG_EXCERPT="excerpt";

    private static final String TAG_ATTACHMENTS = "attachments";
    private static final String TAG_URL = "url";
    JSONArray titles = null,attachments = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_list, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        titleList = new ArrayList<HashMap<String, String>>();

        if (isNetworkAvailable() == true) {

            new JSONParse().execute();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please connect to internet first");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("No Connection");
            alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            title = (TextView) findViewById(R.id.title);
            excerpt_tv=(TextView)findViewById(R.id.excerpt);
            thumbnail = (ImageView) findViewById(R.id.image);

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Fetching Content...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            JSONObject json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                //Getting JSON from URL
                titles = json.getJSONArray(TAG_POSTS);


                for (int i = 0; i < titles.length(); i++) {
                    JSONObject c = titles.getJSONObject(i);
                    //JSONObject e=titles.getJSONObject(i);

                    //Storing JSON item in a Variable
                    String title = c.getString(TAG_TITLE);
                    String excerpt= c.getString(TAG_EXCERPT);

                    //Adding value Hashmap key=> value
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TAG_TITLE, title);
                    map.put(TAG_EXCERPT, excerpt);
                    titleList.add(map);

                    list = (ListView) findViewById(R.id.list);
                    ListAdapter adapter = new SimpleAdapter(MainActivity.this, titleList,
                            R.layout.list_v, new String[]{TAG_TITLE,TAG_EXCERPT}, new int[]{
                            R.id.title,R.id.excerpt});
                    list.setAdapter(adapter);
                    pDialog.dismiss();
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(MainActivity.this, titleList.get(+position).get("title"), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
