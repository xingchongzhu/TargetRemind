package com.wtach.stationremind.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.SuggestionResult;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.wtach.stationremind.R;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CityInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>{

    public static final int CityItemViewType = 1;
    public static final int StationItemViewType = 2;
    public static final int SugInfoItemViewType = 3;
    private List<Object> list;

    private OnRecyItemClickListener mOnRecyItemClickListener;
    public CustomAdapter(List<Object> list){
        this.list = list;
    }

    public List<Object> getList() {
        return list;
    }

    public void setData(List<Object> list){
        if(this.list != null){
            this.list.clear();
        }
        this.list = list;
        notifyDataSetChanged();
    }

    public void setOnRecyItemClickListener(OnRecyItemClickListener mOnRecyItemClickListener) {
        this.mOnRecyItemClickListener = mOnRecyItemClickListener;
    }

    @Override
    public int getItemViewType(int i) {
        if(list.get(i) instanceof StationInfo){
            return StationItemViewType;
        }else if (list.get(i) instanceof CityInfo){
            return CityItemViewType;
        }else if (list.get(i) instanceof SuggestionResult.SuggestionInfo){
            return SugInfoItemViewType;
        }else{
             return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case StationItemViewType:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
                return new StationViewHolder(view);
            case CityItemViewType:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
                return new CityViewHolder(view);
            case SugInfoItemViewType:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
                return new SugViewHolder(view);
                default:
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if(viewHolder instanceof StationViewHolder){
            ((StationViewHolder) viewHolder).textView.setText(((StationInfo)list.get(position)).cname);
        }else if(viewHolder instanceof CityViewHolder){
            ((CityViewHolder) viewHolder).textView.setText(((CityInfo)list.get(position)).getCityName());
        }if(viewHolder instanceof SugViewHolder){
            ((SugViewHolder) viewHolder).textView.setText(((SuggestionResult.SuggestionInfo)list.get(position)).key);
        }
        if(mOnRecyItemClickListener != null){
            final View itemView =  viewHolder.itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnRecyItemClickListener.onItemClick(itemView,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public StationViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text);
        }
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public CityViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text);
        }
    }

    static class SugViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public SugViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text);
        }
    }


}
