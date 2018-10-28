package com.polarbearr.todo;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.polarbearr.todo.DatabaseHelper.COMPLETED_TABLE;
import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private static long backPressedTime = 0;

    static final String TODO_KEY = "0";
    static final String TODO_DB = "tododb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper.openDatabase(getApplicationContext(), TODO_DB);
        DatabaseHelper.createTable(TODO_TABLE);
        DatabaseHelper.createTable(COMPLETED_TABLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        ListPagerAdapter adapter = new ListPagerAdapter(getSupportFragmentManager());
        ListFragment fragment = new ListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TODO_KEY, 0);
        fragment.setArguments(bundle);
        adapter.addItem(fragment);

        fragment = new ListFragment();
        bundle = new Bundle();
        bundle.putInt(TODO_KEY, 1);
        fragment.setArguments(bundle);
        adapter.addItem(fragment);

        mViewPager.setAdapter(adapter);
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

    static class ListPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> items = new ArrayList<>();

        public ListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addItem(Fragment item){
            items.add(item);
        }

        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(this, R.string.finish_toast, Toast.LENGTH_SHORT);

        if(System.currentTimeMillis() > backPressedTime + 2000){
            backPressedTime = System.currentTimeMillis();
            toast.show();
            return;
        }

        if(System.currentTimeMillis() <= backPressedTime + 2000){
            super.onBackPressed();
            toast.cancel();
        }

    }
}
