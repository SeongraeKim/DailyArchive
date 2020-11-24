package com.ksr.dailyarchive;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ListData> arrayList;
    private SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy  /  MM  /  dd  E요일");

    public RecyclerAdapter(ArrayList<ListData> arrayList){ this.arrayList = arrayList; }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_date, tv_content;

        public MyViewHolder(@NonNull final View view) {
            super(view);
            tv_date = view.findViewById(R.id.tv_date);
            tv_content = view.findViewById(R.id.tv_content);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                                                         // recyclerView item 클릭 시
                    int pos = getAdapterPosition();     // 행 번호 pos에 저장
                    if(pos != RecyclerView.NO_POSITION) {

                        int date = arrayList.get(pos).getTv_date();

                        Intent intent = new Intent(v.getContext(), ShowActivity.class);
                        intent.putExtra("date", date);
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {    // item 뷰 설정

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {           // 뷰홀더에 데이터 연결
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        Date parse_date = null;
        String str_date = String.valueOf(arrayList.get(position).getTv_date());
        try {
            parse_date = dateFormat1.parse(str_date);
        } catch (Exception e){ e.printStackTrace(); }

        String format_date = dateFormat2.format(parse_date);

        myViewHolder.tv_date.setText(format_date);
        myViewHolder.tv_content.setText(arrayList.get(position).getTv_content());
    }

    @Override
    public int getItemCount() {                                                                     // arryList 사이즈 반환
        return arrayList.size();
    }
}
