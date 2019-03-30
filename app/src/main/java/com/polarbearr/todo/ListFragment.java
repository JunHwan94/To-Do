package com.polarbearr.todo;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.polarbearr.todo.data.DatabaseHelper;
import com.polarbearr.todo.data.TodoAdapter;
import com.polarbearr.todo.data.TodoItem;
import com.polarbearr.todo.databinding.FragmentListBinding;

import static com.polarbearr.todo.MainActivity.TODO_KEY;
import static com.polarbearr.todo.WriteActivity.TYPE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.DATE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.GREATEST_ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.WriteActivity.DATE_NOT_SELECTED;

public class ListFragment extends Fragment implements View.OnTouchListener{
    static final String NOTHING = "기한 없음";
    static final String isCompletedYn = "사용안함";
    static final String COMPLETED = "Y";
    static final String NOT_COMPLETED = "N";
    static final int WRITE_REQUEST_CODE = 101;
    public final int HORIZONTAL_MIN_DISTANCE = 10;
    public final int VERTICAL_MIN_DISTANCE = 10;
    private static final String LOG_TAG = "SwipeDetector";

    private Bundle loadedData;
    private int count;
    private int fragmentType;
    int _xDelta;
    int _yDelta;
    private FragmentListBinding binding;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;

    public static enum Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false);

        // 리사이클러뷰 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);

        // 플로팅버튼 설정
        binding.fab.setOnTouchListener(this);
        binding.fab.setOnClickListener(v-> {
            if(!swipeDetected()) {
                Intent intent = new Intent(getContext(), WriteActivity.class);

                // DB테이블에 행 하나라도 있으면 id를 조회해서 해당 id를 넘겨줌, 작성할때 +1
                if (count != 0) {
                    int greatestId = DatabaseHelper.selectGreatestId();
                    intent.putExtra(GREATEST_ID_KEY, greatestId);
                }
                startActivityForResult(intent, WRITE_REQUEST_CODE);
            }
        });
        DisplayMetrics metrics = getMetrics(getContext());
        setButtonPosition(metrics, binding.fab);
        
        // 프래그먼트 구분해서 데이터 설정
        Bundle bundle = getArguments();
        if(bundle != null) processBundle(bundle);

        // 리사이클러뷰에 어댑터 설정
        setTodoAdapter(loadedData);

        return binding.getRoot();
    }

    // 목록 테이블 구분해서 데이터베이스에서 로드
    public void processBundle(final Bundle bundle){
        fragmentType = bundle.getInt(TODO_KEY);

        switch(fragmentType){
            case 0:
                // 할 일 데이터베이스에서 불러오기
                loadedData = DatabaseHelper.selectAllData(NOT_COMPLETED);
                break;
            case 1:
                // 완료한 일 데이터베이스에서 불러오기
                loadedData = DatabaseHelper.selectAllData(COMPLETED);
                binding.fab.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void setTodoAdapter(Bundle loadedData){
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
                String repeatability = itemBundle.getString(REPEATABILITY_KEY);
                // 날짜 선택안한 데이터 불러오면 없음으로 표시
                if(date.equals(DATE_NOT_SELECTED)) date = NOTHING;
                int id = itemBundle.getInt(ID_KEY);

                item = new TodoItem(title, content, date, alarmTime, repeatability, isCompletedYn, id);
                adapter.addItem(item);
            }
            count = adapter.getItemCount();

            adapter.setOnItemClickListener((holder, view, position)->{
                TodoItem gotItem = adapter.getItem(position);
                String title = gotItem.getTitle();
                String content = gotItem.getContent();
                String date = gotItem.getDate();
                String alarmTime = gotItem.getAlarmTime();
                String repeatability = gotItem.getRepeatability();
                int id = gotItem.getId();

                Intent intent = new Intent(getContext().getApplicationContext(), WriteActivity.class);
                intent.putExtra(TITLE_KEY, title);
                intent.putExtra(CONTENT_KEY, content);
                intent.putExtra(ID_KEY, id);
                intent.putExtra(DATE_KEY, date);
                intent.putExtra(ALARM_TIME_KEY, alarmTime);
                intent.putExtra(REPEATABILITY_KEY, repeatability);
                intent.putExtra(TYPE_KEY, fragmentType);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, WRITE_REQUEST_CODE);
            });
            binding.recyclerView.setAdapter(adapter);
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
        view.setX(metrics.widthPixels * 8F / 10);
        view.setY(metrics.heightPixels * 75F / 100);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        binding.rootLayout.invalidate();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                mSwipeDetected = Action.None;
                return false; // allow other events like Click to be processed
            }
            case MotionEvent.ACTION_MOVE: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // horizontal swipe detection
                if (Math.abs(deltaX) > HORIZONTAL_MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        Log.i(LOG_TAG, "Swipe Left to Right");
                        mSwipeDetected = Action.LR;
                        return true;
                    }
                    if (deltaX > 0) {
                        Log.i(LOG_TAG, "Swipe Right to Left");
                        mSwipeDetected = Action.RL;
                        return true;
                    }
                } else

                    // vertical swipe detection
                    if (Math.abs(deltaY) > VERTICAL_MIN_DISTANCE) {
                        // top or down
                        if (deltaY < 0) {
                            Log.i(LOG_TAG, "Swipe Top to Bottom");
                            mSwipeDetected = Action.TB;
                            return false;
                        }
                        if (deltaY > 0) {
                            Log.i(LOG_TAG, "Swipe Bottom to Top");
                            mSwipeDetected = Action.BT;
                            return false;
                        }
                    }
                return true;
            }
        }
        return false;
    }
}
