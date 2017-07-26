package com.example.jungh.jeju_ar.listmodel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.jungh.jeju_ar.R;

/**
 * Created by jungh on 2017-06-09.
 */

public class ListImageAdapter extends RecyclerView.Adapter<ListImageAdapter.ViewHolder> {
    Context context;
    String[] list_imageArrayList;
    ViewHolder viewHolder;

    //아이템 클릭시 실행 함수
    private ItemClick itemClick;
    public interface ItemClick {
        public void onClick(View view,int position);
    }

    //아이템 클릭시 실행 함수 등록 함수
    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView list_image_item;
        View view;

        public ViewHolder(View itemView){
            super(itemView);
            list_image_item = (ImageView)itemView.findViewById(R.id.list_image_item);
            this.view = itemView;
        }
    }

    public ListImageAdapter(Context context, String[] list_imageArrayList){
        this.context = context;
        this.list_imageArrayList = list_imageArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.list_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String image_path = list_imageArrayList[position];
        final int Position = position;

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, Position);
                }
            }
        });

        ImageView imageView = holder.list_image_item;
        Glide.with(context).load(image_path).into(imageView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.list_imageArrayList.length;
    }
}
