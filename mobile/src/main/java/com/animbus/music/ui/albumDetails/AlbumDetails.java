package com.animbus.music.ui.albumDetails;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.animbus.music.R;
import com.animbus.music.customImpls.ThemableActivity;
import com.animbus.music.data.adapter.AlbumDetailsAdapter;
import com.animbus.music.media.MediaData;
import com.animbus.music.media.PlaybackManager;
import com.animbus.music.media.objects.Album;
import com.animbus.music.media.objects.Song;
import com.animbus.music.ui.nowPlaying.NowPlaying;
import com.animbus.music.ui.settings.Settings;
import com.animbus.music.ui.settings.chooseIcon.IconManager;
import com.animbus.music.ui.theme.Theme;

import java.util.List;

public class AlbumDetails extends ThemableActivity {
    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingToolbar;
    RecyclerView mList;
    FloatingActionButton mFAB;
    Album mAlbum;
    TextView mTitle, mArtist;
    LinearLayout mDetailsRoot;
    boolean tempFavorite = false;

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.activity_album_details);
        configureTransition();
        mAlbum = MediaData.get().findAlbumById(getIntent().getLongExtra("album_id", -1));
    }

    private void configureTransition(){
        ViewCompat.setTransitionName(findViewById(R.id.album_details_album_art), "art");
        ViewCompat.setTransitionName(findViewById(R.id.album_details_info_toolbar), "info");
        ViewCompat.setTransitionName(findViewById(R.id.album_details_toolbar), "appbar");
    }

    @Override
    protected void setVariables() {
        mToolbar = (Toolbar) findViewById(R.id.album_details_toolbar);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.album_details_collapsing_toolbar);
        mList = (RecyclerView) findViewById(R.id.album_details_recycler);
        mFAB = (FloatingActionButton) findViewById(R.id.album_details_fab);
        mDetailsRoot = (LinearLayout) findViewById(R.id.album_details_info_toolbar);
        mTitle = (TextView) findViewById(R.id.album_details_info_toolbar_title);
        mArtist = (TextView) findViewById(R.id.album_details_info_toolbar_artist);
    }

    @Override
    protected void setUp() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        configureFab();
        configureRecyclerView();
        configureUI();
    }

    private void configureRecyclerView() {
        AlbumDetailsAdapter adapter = new AlbumDetailsAdapter(this, mAlbum.getSongs());
        mList.setAdapter(adapter);
        adapter.setOnItemClickedListener(new AlbumDetailsAdapter.AlbumDetailsClickListener() {
            @Override
            public void onAlbumDetailsItemClicked(View v, List<Song> data, int pos) {
                PlaybackManager.get().play(data, pos);
            }
        });
        mList.setItemAnimator(new DefaultItemAnimator());
        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void configureFab() {
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackManager.get().play(mAlbum.getSongs(), 0);
                transitionNowPlaying();
            }
        });
        mFAB.setAlpha(0.0f);
        mFAB.setScaleX(0.0f);
        mFAB.setScaleY(0.0f);
        mFAB.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(200).setStartDelay(500).start();
    }

    private void configureUIColors() {
        FabHelper.setFabBackground(mFAB, mAlbum.accentColor);
        FabHelper.setFabTintedIcon(mFAB, getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp), mAlbum.accentIconColor);
        mDetailsRoot.setBackgroundColor(mAlbum.BackgroundColor);
        mTitle.setTextColor(mAlbum.TitleTextColor);
        mArtist.setTextColor(mAlbum.SubtitleTextColor);
        mCollapsingToolbar.setContentScrimColor(mAlbum.BackgroundColor);
        mCollapsingToolbar.setStatusBarScrimColor(mAlbum.BackgroundColor);

        //Sets Window description in Multitasking menu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IconManager iconM = IconManager.get().setContext(this);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), iconM.getDrawable(iconM.getOverviewIcon(iconM.getIcon()).getId()));
            setTaskDescription(new ActivityManager.TaskDescription(mAlbum.getAlbumTitle(), bm, mAlbum.BackgroundColor));
            bm.recycle();
        }
    }

    private void configureUI() {
        ImageView mImage = (ImageView) findViewById(R.id.album_details_album_art);
        mAlbum.requestArt(mImage);
        mTitle.setText(mAlbum.getAlbumTitle());
        mCollapsingToolbar.setTitle(mAlbum.getAlbumTitle());
        mArtist.setText(mAlbum.getAlbumArtistName());
        configureUIColors();
    }

    private void transitionNowPlaying() {
        startActivity(new Intent(this, NowPlaying.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }

    @Override
    protected void setUpTheme(Theme theme) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mFAB.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                AlbumDetails.super.onBackPressed();
            }
        });
    }
}
