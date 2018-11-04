package com.polarbearr.todo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static com.polarbearr.todo.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.MainActivity.TODO_KEY;
import static com.polarbearr.todo.MainActivity.TODO_WRITE_REQUEST_CODE;

public class ListFragment extends Fragment {
    static final String TITLE_KEY = "titlekey";
    static final String CONTENT_KEY = "contentkey";
    static final String ID_KEY = "idkey";

    private static Bundle loadedData;

    private TodoAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);

        Bundle bundle = getArguments();
        processBundle(bundle);

        adapter = new TodoAdapter(getContext());
        adapter.setOnItemClickListener(new TodoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TodoAdapter.ViewHolder holder, View view, int position) {
                TodoItem item = adapter.getItem(position);
                String title = item.getTitle();
                String content = item.getContent();
                int id = item.getId();

                Intent intent = new Intent(getContext().getApplicationContext(), WriteActivity.class);
                intent.putExtra(TITLE_KEY, title);
                intent.putExtra(CONTENT_KEY, content);
                intent.putExtra(ID_KEY, id);
                Toast.makeText(getContext(), "db에 저장된 id = " + id, Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });
        setTodoAdapter(adapter, recyclerView);

        return rootView;
    }

    public void processBundle(final Bundle bundle){
        int fragmentType = bundle.getInt(TODO_KEY);

        switch(fragmentType){
            case 0:
                // 할 일 데이터베이스에서 불러오기
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loadedData = DatabaseHelper.selectData(DatabaseHelper.TODO_TABLE);
                        } catch(Exception e){}
                    }
                }).start();
                break;
            case 1:
                // 완료한 일 데이터베이스에서 불러오기
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            loadedData = DatabaseHelper.selectData(DatabaseHelper.COMPLETED_TABLE);
                        } catch(Exception e){}
                    }
                }).start();
                break;
        }
    }

    public void setTodoAdapter(TodoAdapter adapter, RecyclerView recyclerView){
        Bundle itemBundle;
        TodoItem item;
        if(loadedData != null) {
            for (int i = 0; i < loadedData.size(); i++) {
                itemBundle = loadedData.getBundle(TODO_ITEM + i);

                String title = itemBundle.getString(TITLE_KEY);
                String content = itemBundle.getString(CONTENT_KEY);
                int id = itemBundle.getInt(ID_KEY);

                System.out.println(title + " " + content + " " + id);

                item = new TodoItem(title, content, id);
                adapter.addItem(item);
            }
            recyclerView.setAdapter(adapter);
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

    // 코드가 101이면 어댑터 업데이트
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TODO_WRITE_REQUEST_CODE){
            // WriteActivity에서 intent로 받아와서 어댑터에 추가하기

//            adapter.addItem();
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }
    }
}
