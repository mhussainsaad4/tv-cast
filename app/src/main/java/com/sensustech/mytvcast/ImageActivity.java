package com.sensustech.mytvcast;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Adapters.ImageAdapter;
import com.sensustech.mytvcast.Model.ImageModel;
import com.sensustech.mytvcast.utils.ItemClickSupport;
import com.sensustech.mytvcast.utils.StreamingManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.sensustech.mytvcast.utils.StreamingWebServer.getMimeType;

public class ImageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private GridLayoutManager manager;
    private ImageAdapter adapter;
    private ProgressBar progress;
    private SearchView searchView;
    private SearchView.OnQueryTextListener queryTextListener;
    private ArrayList<ImageModel> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_image);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        manager = new GridLayoutManager(this, 3);
        adapter = new ImageAdapter(images, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (!StreamingManager.getInstance(ImageActivity.this).isDeviceConnected()) {
                    startActivity(new Intent(ImageActivity.this, ConnectActivity.class));
                    return;
                }
                ImageModel im = images.get(position);
                StreamingManager.getInstance(ImageActivity.this).showContent(im.imageName, getMimeType(im.imageURL), im.imageURL);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setQueryHint(Html.fromHtml("<font color = #DCDCDC>Search Image</font>"));
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchImage(query);
                    hideKeyboard(ImageActivity.this);
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.setIconifiedByDefault(true);
            searchItem.expandActionView();
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void searchImage(String query) {
        progress.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("Ocp-Apim-Subscription-Key", "e0d5f3bfc1814598891169a8bd6aaedd")
                .url("https://api.cognitive.microsoft.com/bing/v7.0/images/search?q=" + query + "&count=50")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (response.isSuccessful()) {
                    try {
                        images.clear();
                        JSONObject imgData = new JSONObject(responseBody.string());
                        JSONArray value = imgData.getJSONArray("value");
                        for (int i = 0; i < value.length() - 1; i++) {
                            JSONObject img = value.getJSONObject(i);
                            ImageModel im = new ImageModel();
                            im.imageName = img.getString("name");
                            im.imageURL = img.getString("contentUrl");
                            im.thumbURL = img.getString("thumbnailUrl");
                            images.add(im);
                        }
                        updateImages();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        progress.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void updateImages() {
        runOnUiThread(new Runnable() {
            public void run() {
                adapter = new ImageAdapter(images, ImageActivity.this);
                recyclerView.setAdapter(adapter);
                progress.setVisibility(View.GONE);
            }
        });
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

}