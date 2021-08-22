package com.bassamalim.athkar;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.widget.ListView;
import android.widget.SearchView;
import com.bassamalim.athkar.models.SurahButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SearchableActivity extends ListActivity {

    SearchView searchView;
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

        ArrayList<SurahButton> arrayList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            //SurahButton surahButton = new SurahButton(namesArray[i]);
            //arrayList.add(surahButton);
        }


    }

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
