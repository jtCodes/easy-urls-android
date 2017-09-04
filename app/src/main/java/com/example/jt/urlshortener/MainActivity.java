package com.example.jt.urlshortener;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jt.urlshortener.data.DBHelper;
import com.example.jt.urlshortener.model.Link;
import com.example.jt.urlshortener.model.MyListItem;
import com.example.jt.urlshortener.model.UrlResponse;
import com.example.jt.urlshortener.network.UrlShortService;
import com.example.jt.urlshortener.view.MyListCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.dift.ui.SwipeToAction;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.jt.urlshortener.R.menu.main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    List<MyListItem> items = new ArrayList<>();
    final Context c = this;
    private static final String TAG = "Testing: ";
    private ProgressBar spinner;
    private static String BASE_URL = "http://lnk-sh.herokuapp.com/new/";
    DBHelper databaseHelper;
    MyListCursorAdapter mAdapter;
    RecyclerView mRecycler;
    SQLiteDatabase db;
    Cursor todoCursor;
    LinearLayoutManager layoutManager;
    SearchView sv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        spinner = (ProgressBar) findViewById(R.id.progressBar2);
        spinner.setVisibility(View.GONE);
        // Get singleton instance of database
        databaseHelper = DBHelper.getInstance(getApplicationContext());

        db = databaseHelper.getReadableDatabase();
        todoCursor = db.rawQuery("SELECT  * FROM links", null);

        // Find RecyclerView to populate
        mRecycler = (RecyclerView) findViewById(R.id.mobile_list);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(layoutManager);
        //divider
        mAdapter = new MyListCursorAdapter(this, todoCursor, this.items);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecycler.getContext(),
                layoutManager.getOrientation());
        mRecycler.addItemDecoration(dividerItemDecoration);

        //Set the Adapter to use timestamp as the item id for each row from our database
        mAdapter.setHasStableIds(true);
        mRecycler.setAdapter(mAdapter);

        SwipeToAction swipeToAction = new SwipeToAction(mRecycler, new SwipeToAction.SwipeListener<MyListItem>() {
            @Override
            public boolean swipeLeft(MyListItem itemData) {
                copy("copied",itemData.getShortened());
                displaySnackbar("Copied " + itemData.getShortened(), null, null);
                return true;
            }

            @Override
            public boolean swipeRight(final MyListItem itemData) {
                final int pos = removeUrl(itemData);
                databaseHelper.deleteLink(itemData.getID());
                refresh();
                displaySnackbar(itemData.getShortened() + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDB(itemData.getShortened(),itemData.getOriginal(),itemData.getTime());
                        populate();
                        refresh();
                    }
                });
                return true;
            }

            @Override
            public void onClick(MyListItem itemData) {
                String url = itemData.getOriginal();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                displaySnackbar(itemData.getOriginal() + " clicked", null, null);
            }

            @Override
            public void onLongClick(MyListItem itemData) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, itemData.getShortened());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                displaySnackbar(" long clicked", null, null);
            }
        });
        populate();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                spinner.setVisibility(View.VISIBLE);
                                String url = userInputDialogEditText.getText().toString();
                                //http get
                                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                                OkHttpClient client = new OkHttpClient.Builder()
                                        .addInterceptor(interceptor)
                                        .build();
                                Retrofit retrofit = new Retrofit
                                        .Builder()
                                        .baseUrl(BASE_URL)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .client(client)
                                        .build();

                                UrlShortService service = retrofit.create(UrlShortService.class);

                                Call<UrlResponse> call = service.getShort(BASE_URL + url);
                                call.enqueue(new Callback<UrlResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<UrlResponse> call, Response<UrlResponse> response) {
                                        String shorten = response.body().getShorten();

                                        Log.d("yass queen", shorten);
                                        updateDB(shorten, userInputDialogEditText.getText().toString(), getTime());

                                        spinner.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<UrlResponse> call, Throwable t) {
                                        Toast.makeText(MainActivity.this, "error :(", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "failed");
                                    }
                                });

                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void updateDB(String shorten, String input, String currentDate) {
        // Create sample data
        Link sampleLink = new Link();
        sampleLink.shortened = shorten;
        sampleLink.original = input;
        sampleLink.time = currentDate;

        // Add sample post to the database
        databaseHelper.addLink(sampleLink);
        refresh();
    }

    public void copy(String label,String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label,text);
        clipboard.setPrimaryClip(clip);
    }

    public void refresh() {
        db = databaseHelper.getReadableDatabase();
        todoCursor = db.rawQuery("SELECT  * FROM links", null);
        populate();
        mAdapter = new MyListCursorAdapter(this, todoCursor, this.items);
        mRecycler.setAdapter(mAdapter);
    }

    public void filt(String text) {
        db = databaseHelper.getReadableDatabase();
        todoCursor = db.rawQuery("SELECT * FROM links WHERE original LIKE '%"+ text + "%'" , null);
        mAdapter = new MyListCursorAdapter(this, todoCursor, this.items);
        mRecycler.setAdapter(mAdapter);
    }

    public void populate() {
        // Get all posts from database
        List<Link> links = databaseHelper.getAllLinks();
        for (Link link : links) {
            this.items.add(new MyListItem(link.shortened, link.original, link.time, link.id));
            Log.d(TAG, link.original + " " + link.shortened + " " + link.time);
        }

    }

    private void addUrl(int pos, MyListItem item) {
        items.add(pos, item);
        mAdapter.notifyItemInserted(pos);
    }

    private int removeUrl(MyListItem item) {
        int pos = items.indexOf(item);
        items.remove(item);
        mAdapter.notifyItemRemoved(pos);
        return pos;
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        View v = snack.getView();
        v.setBackgroundColor(getResources().getColor(R.color.secondary));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snack.show();
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(new Date());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);

        sv= (SearchView) menu.findItem(R.id.action_search).getActionView();
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                filt(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (mAdapter.getItemCount() > 0) {
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_top) {
                mRecycler.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                return true;
            }
            if (id == R.id.action_bottom) {
                mRecycler.smoothScrollToPosition(0);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
