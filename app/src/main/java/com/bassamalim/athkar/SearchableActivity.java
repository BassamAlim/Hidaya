package com.bassamalim.athkar;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.bassamalim.athkar.models.SurahName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {

    SearchView searchView;
    ListView listView;
    ArrayList<String> list;

    public SearchableActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            /* to clear the history
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
            HelloSuggestionProvider.AUTHORITY, HelloSuggestionProvider.MODE);
            suggestions.clearHistory();*/

            suggestions.saveRecentQuery(query, null);
            doMySearch(query);
        }


        list = getSurahNames();
        String[] namesArray = getSurahNamesArray();

        ArrayList<SurahName> arrayList = new ArrayList<>();

        for (int i=0; i<list.size(); i++) {
            SurahName surahName = new SurahName(namesArray[i]);
            arrayList.add(surahName);
        }


    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }*/


    private void doMySearch(String query) {

    }

    public ArrayList<String> getSurahNames() {
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
        return names;
    }

    public String[] getSurahNamesArray() {
        String[] names = new String[Constants.NUMBER_OF_SURAHS];
        String surahNamesJson = Utils.getJsonFromAssets(this, "surah_names.json");
        try {
            assert surahNamesJson != null;
            JSONObject jsonObject = new JSONObject(surahNamesJson);
            JSONArray array = jsonObject.getJSONArray("names");
            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject object = array.getJSONObject(i);
                names[i] = (object.getString("name"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

}
