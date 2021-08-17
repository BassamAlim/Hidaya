package com.bassamalim.athkar;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.view.MenuItemCompat;

import com.bassamalim.athkar.ui.quran.QuranFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {

    ListView listView;
    ArrayList<String> list;
    ArrayAdapter<String > adapter;

    public SearchableActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

        listView = findViewById(R.id.list_view);

        getSurahNames();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

    }

    @Override
    public boolean onSearchRequested() {
        Toast.makeText(this, "in on search", Toast.LENGTH_SHORT).show();
        return super.onSearchRequested();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default


        //MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        //final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }
                else{
                    Toast.makeText(MainActivity.getInstance(), "No Match found",Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
        //return true;
    }


    private void doMySearch(String query) {

    }

    public void getSurahNames() {
        ArrayList<String> names = new ArrayList<>();
        String surahNamesJson = Utils.getJsonFromAssets(this, "surah_names.json");
        try {
            assert surahNamesJson != null;
            JSONObject jsonObject = new JSONObject(surahNamesJson);
            JSONArray array = jsonObject.getJSONArray("names");
            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject object = array.getJSONObject(i);
                names.add(object.getString("name"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        list = names;
    }

}
