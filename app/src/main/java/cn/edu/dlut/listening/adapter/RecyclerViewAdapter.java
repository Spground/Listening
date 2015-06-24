package cn.edu.dlut.listening.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.edu.dlut.listening.R;

/**
 * Created by asus on 2015/6/21.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;

    public RecyclerViewAdapter(Context context){
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_recycle_view, parent, false);
        return new CellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    private static class CellViewHolder extends RecyclerView.ViewHolder{

        public CellViewHolder(View itemView) {
            super(itemView);
        }
    }
}
