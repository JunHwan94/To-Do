package com.polarbearr.todo;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.polarbearr.todo.DatabaseHelper.COMPLETED_TABLE;
import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;

public class MainActivity extends AppCompatActivity{
    private static ViewPager pager;
    private static long backPressedTime = 0;

    static final String TODO_KEY = "0";
    static final String TODO_DB = "tododb";
    static final int TODO_WRITE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper.openDatabase(getApplicationContext(), TODO_DB);
        DatabaseHelper.createTable(TODO_TABLE);
        DatabaseHelper.createTable(COMPLETED_TABLE);

        pager = findViewById(R.id.container);
//        TabLayout tabLayout = findViewById(R.id.tabs);

//        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));

        setPagerAdapter();
    }

    // 어댑터 설정
    public void setPagerAdapter(){
        ListPagerAdapter adapter = new ListPagerAdapter(getSupportFragmentManager());
        ListFragment fragment;
//        Bundle bundle;

        fragment = new ListFragment();
        adapter.addItem(fragment);

        // 완료 목록 있을 때
//        for(int i = 0; i < 2; i++) {
//            fragment = new ListFragment();
//            bundle = new Bundle();
//            bundle.putInt(TODO_KEY, i);
//            fragment.setArguments(bundle);
//            adapter.addItem(fragment);
//        }

        pager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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