package com.example.this_is_changwon;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.AdapterViewholder> {

    private List<Data> dataList;
    private FragmentActivity context;

    private Appdatabase db;

    public Adapter(FragmentActivity context, List<Data> dataList)
    {
        this.context = context;
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override  ///onCreateViewHolder에서는 어떤 레이아웃과 연결해야되는지 설정하고 view를 만들어준다.
    public Adapter.AdapterViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,parent,false);
        AdapterViewholder holder = new AdapterViewholder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewholder holder, int position) {
        db = Appdatabase.getInstance(context);
        holder.Tname.setText(dataList.get(position).getName());
        holder.btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Data data = dataList.get(holder.getAdapterPosition());

                final int sID = data.getId();
                String sText = data.getName();

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_update);
                int width = WindowManager.LayoutParams.MATCH_PARENT;
                int height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.show();

                final EditText editText = dialog.findViewById(R.id.dialog_edit_text);
                Button bt_update = dialog.findViewById(R.id.bt_update);

                editText.setText(sText);

                bt_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        String uText = editText.getText().toString().trim();

                        db.dataDao().update(sID, uText);

                        dataList.clear();
                        dataList.addAll(db.dataDao().getAll());
                        notifyDataSetChanged();
                    }
                });
            }
        });

        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data data = dataList.get(holder.getAdapterPosition());
                String delete_data = data.getTitle();

                db.dataDao().delete(data);

                int position = holder.getAdapterPosition();
                dataList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, dataList.size());

                switch (delete_data){
                    case "spot0":
                        db.table1().deleteAllTravelSpots();
                        break;
                    case "spot1":
                        db.table2().deleteAllTravelSpots();
                        break;
                    case "spot2":
                        db.table3().deleteAllTravelSpots();
                        break;
                    case "spot3":
                        db.table4().deleteAllTravelSpots();
                        break;
                    case "spot4":
                        db.table5().deleteAllTravelSpots();
                        break;
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data data = dataList.get(holder.getAdapterPosition());
                String click_data = data.getTitle();
                String click_name = data.getName();

                Log.d("클릭한 아이템", click_data);
                Log.d("클릭한 일정 이름", click_name);

                Intent intent = new Intent(context, ItemActivity.class);
                intent.putExtra("table", click_data);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class AdapterViewholder extends RecyclerView.ViewHolder {
        TextView Tname;
        ImageView btEdit, btDelete;

        String result = "";

        public AdapterViewholder(@NonNull View itemView) {
            super(itemView);

            Tname = (TextView) itemView.findViewById(R.id.name);
            btEdit = itemView.findViewById(R.id.bt_edit);
            btDelete = itemView.findViewById(R.id.bt_delete);
        }
    }
}
