package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 10/30/2015.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.ccrama.redditslide.R;


public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ViewHolder> {




    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        public ViewHolder(View itemView)
        {
            super(itemView);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nointernet, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 1;
    }

}