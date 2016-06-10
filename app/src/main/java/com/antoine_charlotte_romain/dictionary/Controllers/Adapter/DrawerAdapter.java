package com.antoine_charlotte_romain.dictionary.Controllers.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.antoine_charlotte_romain.dictionary.R;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {


    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    static OnItemClickListener myItemClickListener;

    private String myNavTitles[];
    private int myIcons[];

    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {

        int holderid;
        TextView title;
        TextView textView;
        ImageView imageHeader;
        ImageView imageView;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            if(ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.textViewDrawer);
                imageView = (ImageView) itemView.findViewById(R.id.imageViewDrawer);
                holderid = 1;
                itemView.setOnClickListener(this);
            }
            else{
                title = (TextView) itemView.findViewById(R.id.textViewHeader);
                imageHeader = (ImageView) itemView.findViewById(R.id.imageViewHeader);
                holderid = 0;
            }

        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }

    }

    public DrawerAdapter(String titles[],int icons[]){
        myNavTitles = titles;
        myIcons = icons;
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_row, parent, false);
            ViewHolder vhItem = new ViewHolder(v,viewType);
            return vhItem;
        }
        else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
            ViewHolder vhHeader = new ViewHolder(v,viewType);
            return vhHeader;
        }
        return null;

    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        if (holder.holderid == 1) {
            holder.textView.setText(myNavTitles[position - 1]);
            holder.imageView.setImageResource(myIcons[position - 1]);
        } else {
            holder.imageHeader.setImageResource(R.drawable.ic_settings_white_36dp);
            holder.title.setText(R.string.action_settings);
        }
    }

    @Override
    public int getItemCount() {
        return myNavTitles.length+1;
    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.myItemClickListener = mItemClickListener;
    }

}