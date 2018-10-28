package com.polarbearr.todo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.polarbearr.todo.DatabaseHelper.CONTENT;
import static com.polarbearr.todo.DatabaseHelper.DATEVALUE;
import static com.polarbearr.todo.DatabaseHelper.TITLE;
import static com.polarbearr.todo.DatabaseHelper.TODOITEM;
import static com.polarbearr.todo.MainActivity.TODO_KEY;

public class ListFragment extends Fragment {
    private static Bundle loadedData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        Bundle bundle = getArguments();
        processBundle(bundle);

        TodoAdapter adapter = new TodoAdapter(getContext());

        Bundle itemBundle;
        TodoItem item;
        for(int i = 0; i < loadedData.size(); i++){
            itemBundle = loadedData.getBundle(TODOITEM + i);

            String title = itemBundle.getString(TITLE);
            String content = itemBundle.getString(CONTENT);
            String dateValue = itemBundle.getString(DATEVALUE);

            item = new TodoItem(title, content, dateValue);
            adapter.addItem(item);
        }

        recyclerView.setAdapter(adapter);
        return rootView;
    }

    public void processBundle(Bundle bundle){
        int fragmentType = bundle.getInt(TODO_KEY);

        if(fragmentType == 0){
            // 할 일 데이터베이스에서 불러오기
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadedData = DatabaseHelper.selectData(DatabaseHelper.TODO_TABLE);
                }
            }).start();
        }else if(fragmentType == 1){
            // 완료한 일 데이터베이스에서 불러오기
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadedData = DatabaseHelper.selectData(DatabaseHelper.COMPLETED_TABLE);
                }
            }).start();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
