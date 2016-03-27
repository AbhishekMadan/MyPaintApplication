package com.example.abhishekmadan.mypaint.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.example.abhishekmadan.mypaint.R;


public class OperationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String[] mOperationList;

    private Context mContext;

    private LayoutInflater mInflater;
    //object of communication interface to transmit the onClick event on the view to the main activity.
    private OperationViewHolder.OperationCommunicator mCallback;

    public OperationRecyclerViewAdapter(Context context, OperationViewHolder.OperationCommunicator callback) {
        mContext = context;
        mCallback = callback;
        mOperationList = mContext.getResources().getStringArray(R.array.operation_icon);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.operation_recycler_view_single_row, viewGroup, false);
        OperationViewHolder viewHolder = new OperationViewHolder(view, mContext, mCallback);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        OperationViewHolder holder = (OperationViewHolder) viewHolder;
        holder.bindData(mOperationList[i], mContext);
    }

    @Override
    public int getItemCount() {
        return mOperationList.length;
    }

    public static class OperationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView operationImageHolder;
        private Context mHolderContext;
        private OperationCommunicator obj;

        public OperationViewHolder(View itemView, Context context, OperationCommunicator callback) {
            super(itemView);
            obj = callback;
            mHolderContext = context;
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            operationImageHolder = (ImageView) itemView.findViewById(R.id.operation_imageview);
        }

        public void bindData(String val, Context context) {
            operationImageHolder.setImageResource(context.getResources().getIdentifier(val, "drawable", context.getPackageName()));
        }

        /**
         * Overridden method which gets called when a tool from the toolkit (Recycler View)
         * is clicked.
         * @param v is the view being clicked
         */
        @Override
        public void onClick(View v) {
            // Method overridden in the main activity.
            obj.getPosition(getAdapterPosition());
        }

        //Communication interface
        public interface OperationCommunicator {
            public void getPosition(int position);
        }
    }

}
