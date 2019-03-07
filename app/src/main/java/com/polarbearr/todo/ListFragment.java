package com.polarbearr.todo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.polarbearr.todo.data.DatabaseHelper;
import com.polarbearr.todo.data.TodoAdapter;
import com.polarbearr.todo.data.TodoItem;

import static com.polarbearr.todo.MainActivity.TODO_KEY;
import static com.polarbearr.todo.WriteActivity.TYPE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.COMPLETED_TABLE;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.WriteActivity.DATABASE_FLAG_KEY;
import static com.polarbearr.todo.WriteActivity.DATE_NOT_SELECTED;

public class ListFragment extends Fragment {
    public static final String TITLE_KEY = "titlekey";
    public static final String CONTENT_KEY = "contentkey";
    public static final String ID_KEY = "idkey";
    public static final String DATE_KEY = "datekey";
    static final String GREATEST_ID_KEY = "greatestidkey";
    public static final String ALARM_TIME_KEY = "alarmtimekey";
    static final String NOTHING = "없음";
    static final int WRITE_REQUEST_CODE = 101;

    private Bundle loadedData;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private int count;
    private int fragmentType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);

        // 리사이클러뷰 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        // 플로팅버튼 설정
        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WriteActivity.class);

                // DB테이블에 행 하나라도 있으면 id를 조회해서 해당 id를 넘겨줌, 작성할때 +1
                if(count != 0) {
                    int greatestId = DatabaseHelper.selectGreatestId(TODO_TABLE);
                    intent.putExtra(GREATEST_ID_KEY, greatestId);
                }
                startActivityForResult(intent, WRITE_REQUEST_CODE);
            }
        });
        DisplayMetrics metrics = getMetrics(getContext());
        setButtonPosition(metrics, fab);
        
        // 프래그먼트 구분해서 데이터 설정
        Bundle bundle = getArguments();
        if(bundle != null) processBundle(bundle);

        // 리사이클러뷰에 어댑터 설정
        setTodoAdapter();

        return rootView;
    }

    // 목록 테이블 구분해서 데이터베이스에서 로드
    public void processBundle(final Bundle bundle){
        fragmentType = bundle.getInt(TODO_KEY);

        switch(fragmentType){
            case 0:
                // 할 일 데이터베이스에서 불러오기
                loadedData = DatabaseHelper.selectAllData(DatabaseHelper.TODO_TABLE);
                break;
            case 1:
                loadedData = DatabaseHelper.selectAllData(DatabaseHelper.COMPLETED_TABLE);
                fab.setVisibility(View.INVISIBLE);
                // 완료한 일 데이터베이스에서 불러오기
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadedData = DatabaseHelper.selectData(DatabaseHelper.COMPLETED_TABLE);
//                    }
//                }).start();
                break;
        }
    }

    public void setTodoAdapter(){
        final TodoAdapter adapter = new TodoAdapter(getContext());
        Bundle itemBundle;
        TodoItem item;
        if(loadedData != null) {
            for (int i = 0; i < loadedData.size(); i++) {
                itemBundle = loadedData.getBundle(TODO_ITEM + i);

                String title = itemBundle.getString(TITLE_KEY);
                String content = itemBundle.getString(CONTENT_KEY);
                String date = itemBundle.getString(DATE_KEY);
                String alarmTime = itemBundle.getString(ALARM_TIME_KEY);
                // 날짜 선택안한 데이터 불러오면 없음으로 표시
                if(date.equals(DATE_NOT_SELECTED)) date = NOTHING;
                int id = itemBundle.getInt(ID_KEY);

                item = new TodoItem(title, content, date, alarmTime, id);
                adapter.addItem(item);
            }
            count = adapter.getItemCount();

            adapter.setOnItemClickListener(new TodoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(TodoAdapter.ViewHolder holder, View view, int position) {
                    TodoItem item = adapter.getItem(position);
                    String title = item.getTitle();
                    String content = item.getContent();
                    String date = item.getDate();
                    String alarmTime = item.getAlarmTime();
                    int id = item.getId();

                    Intent intent = new Intent(getContext().getApplicationContext(), WriteActivity.class);
                    intent.putExtra(TITLE_KEY, title);
                    intent.putExtra(CONTENT_KEY, content);
                    intent.putExtra(ID_KEY, id);
                    intent.putExtra(DATE_KEY, date);
                    intent.putExtra(ALARM_TIME_KEY, alarmTime);
                    intent.putExtra(TYPE_KEY, fragmentType);

                    startActivityForResult(intent, WRITE_REQUEST_CODE);
                }
            });

            recyclerView.setAdapter(adapter);
        }
    }

    // 화면 크기 얻기
    public DisplayMetrics getMetrics(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    // 버튼 위치 설정
    public static void setButtonPosition(DisplayMetrics metrics, View view){
        view.setX(metrics.widthPixels * 8 / 10);
        view.setY(metrics.heightPixels * 75 / 100);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 리사이클러뷰 목록 업데이트
        if(data != null) {
            boolean databaseChangeFlag = data.getBooleanExtra(DATABASE_FLAG_KEY, false);
            if(databaseChangeFlag == true) {
                switch(fragmentType){
                    case 0:
                        loadedData = DatabaseHelper.selectAllData(DatabaseHelper.TODO_TABLE);
                        break;
                    case 1:
                        loadedData = DatabaseHelper.selectAllData(DatabaseHelper.COMPLETED_TABLE);
                        break;
                }
                setTodoAdapter();
            }
        }
    }

    @Override
    public void onResume() {
        switch(fragmentType){
            case 0:
                loadedData = DatabaseHelper.selectAllData(DatabaseHelper.TODO_TABLE);
                break;
            case 1:
                loadedData = DatabaseHelper.selectAllData(DatabaseHelper.COMPLETED_TABLE);
                break;
        }
        setTodoAdapter();
        super.onResume();
    }
}
