package com.sensustech.mytvcast;

import static com.vincent.filepicker.Constant.showBanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.sensustech.mytvcast.Adapters.YoutubeAdapter;
import com.sensustech.mytvcast.Model.VideoModel;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.ItemClickSupport;
import com.sensustech.mytvcast.utils.StreamingManager;
import com.vincent.filepicker.AdLoadedCallback;
import com.vincent.filepicker.AdsUtill;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class YoutubeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private YoutubeAdapter adapter;
    private ProgressBar progress;
    private SearchView searchView;
    private SearchView.OnQueryTextListener queryTextListener;
    private ArrayList< VideoModel > videos = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FrameLayout bannerContainer;
    private AdsUtill adsUtill;
    private IronSourceBannerLayout banner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        adsUtill=new AdsUtill(this);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_youtube);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        manager = new LinearLayoutManager(this);
        adapter = new YoutubeAdapter(videos, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        bannerContainer = findViewById(R.id.bannerContainer);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener((recyclerView, position, v) -> {
            if (!StreamingManager.getInstance(YoutubeActivity.this).isDeviceConnected()) {
                startActivity(new Intent(YoutubeActivity.this, ConnectActivity.class));
                return;
            }
            VideoModel vm = videos.get(position);
            getVideoLinkSuperMagicTrick(vm.videoName, vm.videoId);
        });
      /*  if (!AdsManager.getInstance().isPremium(this)) {
            adsUtill.loadAPSBanner(bannerContainer, this);
        }*/
    }

    private void loadBannerAd() {
        final FrameLayout bannerContainer = findViewById(R.id.bannerContainer);
        banner = IronSource.createBanner(this, ISBannerSize.BANNER);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        bannerContainer.addView(banner, 0, layoutParams);
        banner.setBannerListener(new BannerListener() {
            @Override
            public void onBannerAdLoaded() {
                Log.d("main_banner_failed", "banner ad loaded");
                banner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBannerAdLoadFailed(IronSourceError error) {
                Log.d("main_banner_failed", error.getErrorMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerContainer.removeAllViews();
                    }
                });
            }

            @Override
            public void onBannerAdClicked() {

            }

            @Override
            public void onBannerAdScreenPresented() {

            }

            @Override
            public void onBannerAdScreenDismissed() {

            }

            @Override
            public void onBannerAdLeftApplication() {

            }
        });
        IronSource.loadBanner(banner);
    }

    public void getVideoLinkSuperMagicTrick(final String title, String videoId) {
        progressDialog = new ProgressDialog(YoutubeActivity.this);
        progressDialog.setMessage("Preparing video...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = 22;
                    if (ytFiles != null && ytFiles.size() > 0) {
                        String downloadUrl;
                        if (ytFiles.get(22) != null) {
                            itag = 22;
                        } else {
                            itag = 18;
                        }
                        downloadUrl = ytFiles.get(itag).getUrl();
                        try {
                            downloadUrl = URLDecoder.decode(downloadUrl, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        StreamingManager.getInstance(YoutubeActivity.this)
                                .playMedia(title, "video/" + ytFiles.get(itag).getFormat().getExt(), downloadUrl);
                    }
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }.extract("https://www.youtube.com/watch?v=" + videoId,true,true);
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
            searchView.setQueryHint(Html.fromHtml("<font color = #DCDCDC>Search Video</font>"));
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    hideKeyboard(YoutubeActivity.this);
                    searchVideo(query);
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

    public void searchVideo(String query) {
        progress.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/youtube/v3/search?part=snippet&order=relevance&q=" + query + "&type=video&key=AIzaSyAP845jzGdchRiWhJfcLAiT68eQ234H5AE&maxResults=50")
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
                        videos.clear();
                        JSONObject videoData = new JSONObject(responseBody.string());
                        JSONArray value = videoData.getJSONArray("items");
                        for (int i = 0; i < value.length() - 1; i++) {
                            JSONObject video = value.getJSONObject(i);
                            JSONObject snippet = video.getJSONObject("snippet");
                            VideoModel vm = new VideoModel();
                            vm.videoId = video.getJSONObject("id").getString("videoId");
                            vm.videoAuthor = snippet.getString("channelTitle");
                            vm.videoName = snippet.getString("title");
                            vm.videoThumb = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                            videos.add(vm);
                        }
                        updateVideos();
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

    public void updateVideos() {
        runOnUiThread(new Runnable() {
            public void run() {
                adapter = new YoutubeAdapter(videos, YoutubeActivity.this);
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
       removeIronSource();
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
    private void removeIronSource() {
        showBanner=true;
        if (bannerContainer != null &&
                bannerContainer.getChildCount() > 0 &&
                bannerContainer.getChildAt(0) instanceof IronSourceBannerLayout) {
            IronSource.destroyBanner((IronSourceBannerLayout) bannerContainer.getChildAt(0));
            bannerContainer.removeAllViews();
        }
    }
}