package com.polarbearr.todo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.polarbearr.todo.data.DatabaseHelper;
import com.polarbearr.todo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import static com.polarbearr.todo.ListFragment.COMPLETED;
import static com.polarbearr.todo.ListFragment.NOT_COMPLETED;
import static com.polarbearr.todo.WriteActivity.DATABASE_FLAG_KEY;

public class MainActivity extends AppCompatActivity{
    private static long backPressedTime = 0;

    static final String TODO_KEY = "0";
    static final String TODO_DB = "todoDB";

    private ListFragment[] fragment;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);

        DatabaseHelper.openDatabase(getApplicationContext(), TODO_DB);
        DatabaseHelper.createTable();

        binding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabs));
        binding.tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.pager));

        setFragment();

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

//        Intent intent = getIntent();
//        processIntent(intent);
    }

    // 인텐트처리
//    public void processIntent(Intent intent){
//        int id = intent.getIntExtra(ID_KEY, 0);
//        Toast.makeText(getBaseContext(), "id = " + id, Toast.LENGTH_SHORT).show();
//        intent = new Intent(getApplicationContext(), WriteActivity.class);
//        intent.putExtra(ID_KEY, id);
//        if(id != 0)
//            startActivityForResult(intent, WRITE_REQUEST_CODE);
//    }

    // 어댑터 설정
    public void setFragment(){
        ListPagerAdapter adapter = new ListPagerAdapter(getSupportFragmentManager());

        fragment = new ListFragment[2];
        Bundle[] bundle = new Bundle[2];
        for(int i = 0; i < 2; i++){
            fragment[i] = new ListFragment();
            bundle[i] = new Bundle();
            bundle[i].putInt(TODO_KEY, i);
            fragment[i].setArguments(bundle[i]);
            adapter.addItem(fragment[i]);
        }

        binding.pager.setAdapter(adapter);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 리사이클러뷰 목록 업데이트
        if(data != null) {
            boolean databaseChangeFlag = data.getBooleanExtra(DATABASE_FLAG_KEY, false);
            if(databaseChangeFlag) {
                Bundle loadedData = DatabaseHelper.selectAllData(NOT_COMPLETED);
                fragment[0].setTodoAdapter(loadedData);
                loadedData = DatabaseHelper.selectAllData(COMPLETED);
                fragment[1].setTodoAdapter(loadedData);
            }
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