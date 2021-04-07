package com.example.dragreorderrecycleviewandswipe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class MainRecyclerViewAdapterJava extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mList;
    private MainActivity mActivity;
    private MainActivity.ITHCallback mITHCallback;
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;

    public MainRecyclerViewAdapterJava(MainActivity activity, MainActivity.ITHCallback ithCallback){
        this.mActivity = activity;
        this.mITHCallback = ithCallback;
        mList = new ArrayList<>();
        for(int i=0; i<20; i++){
            mList.add("아이템" + i);
        }
        mList.add("footer");
    }

    public void moveItem(int from, int to){
        String fromEmoji = mList.get(from);
        mList.remove(from);
        mList.add(to, fromEmoji);
    }

    public void removeItem(int position){
        mList.remove(position);
    }

    private void showAlert(String message, DialogInterface.OnClickListener pListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(message);
        builder.setPositiveButton("확인", pListener);
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == mList.size()-1){
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){
        if(holder instanceof MainRecyclerViewHolder){
            String emoji = mList.get(position);
            ((MainRecyclerViewHolder)holder).setText(emoji);
            if(mITHCallback.getObjectAnimator() != null && ((MainRecyclerViewHolder) holder).getForegroundView().getX() > 0) {
                mITHCallback.getObjectAnimator().cancel();
                ((MainRecyclerViewHolder) holder).getForegroundView().setX(0f);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        if(viewType == TYPE_ITEM){
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.recycler_view_holder,
                    parent,
                    false
            );
            final MainRecyclerViewHolder viewHolder = new MainRecyclerViewHolder(itemView);

            viewHolder.itemView.findViewById(R.id.handleView).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mActivity.startDragging(viewHolder);
                    }
                    return true;
                }
            });

            viewHolder.itemView.findViewById(R.id.backgroundTextView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlert("삭제하시겠습니까?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(viewHolder.getAdapterPosition());
                            notifyItemRemoved(viewHolder.getAdapterPosition());
                            mITHCallback.setPreviousPosition(-1);
                        }
                    });
                }
            });

            return viewHolder;

        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.recycler_view_footer,
                    parent,
                    false
            );
            final FooterViewHolder footerViewHolder = new FooterViewHolder(itemView);
            return footerViewHolder;
        }

    }

    class MainRecyclerViewHolder extends RecyclerView.ViewHolder {
        public MainRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void setText(String text) {
            ((TextView) itemView.findViewById(R.id.textView)).setText(text);
        }
        public View getForegroundView(){
            return itemView.findViewById(R.id.foregroundView);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlert("추가하시겠습니까?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mList.add(mList.size() - 1, "추가된 아이템");
                            notifyItemInserted(mList.size() - 1);
                        }
                    });
                }
            });
        }
    }

}