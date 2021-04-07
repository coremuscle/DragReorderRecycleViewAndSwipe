package com.example.dragreorderrecycleviewandswipe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ITHCallback mCallback;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.recycler_view_holder, null);
        View backgroundTextView = viewGroup.findViewById(R.id.backgroundTextView);
        backgroundTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mCallback = new ITHCallback();
        mCallback.setClamp(backgroundTextView.getMeasuredWidth());

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new MainRecyclerViewAdapterJava(this, mCallback));
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                int position = rv.getChildAdapterPosition(child);
                Log.d("temp","onInterceptTouchEvent() mCallback.previousPosition: " + mCallback.previousPosition);
                if(position != mCallback.previousPosition){
                    mCallback.removePreviousClamp(rv);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    public void startDragging(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    class ITHCallback extends ItemTouchHelper.Callback {

        private int previousPosition = -1;
        private float currentDx = 0f;
        private float clamp = 0f;
        private ObjectAnimator objectAnimator;

        /**
         * 드래그, 스와이프 방향 설정
         * @param recyclerView
         * @param viewHolder
         * @return
         */
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        /**
         * 드랍 범위 제한
         * @param recyclerView
         * @param current
         * @param target
         * @return
         */
        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current,
                                   @NonNull RecyclerView.ViewHolder target) {
            return current.getItemViewType() == MainRecyclerViewAdapterJava.TYPE_ITEM &&
                    target.getItemViewType() == MainRecyclerViewAdapterJava.TYPE_ITEM;
        }

        /**
         * 아이템롱클릭 드래그 사용여부
         * @return
         */
        @Override
        public boolean isLongPressDragEnabled(){
            return false;
        }

        /**
         * 드래그로 아이템의 순서가 변할때마다 호출됨
         * @param recyclerView
         * @param viewHolder
         * @param target
         * @return
         */
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target){
            MainRecyclerViewAdapterJava adapter = (MainRecyclerViewAdapterJava) recyclerView.getAdapter();
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            adapter.moveItem(from, to);
            adapter.notifyItemMoved(from, to);
            return true;
        }

        /**
         * 스와이프로 아이템 삭제시 호출됨
         * @param viewHolder
         * @param direction
         */
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
//            MainRecyclerViewAdapterJava adapter = (MainRecyclerViewAdapterJava) mRecyclerView.getAdapter();
//            adapter.removeItem(viewHolder.getAdapterPosition());
//            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
        }

        /**
         * 아이템 상태변경시 호출
         * ex) IDLE -> DRAG, DRAG -> IDLE, IDLE -> SWIPE, SWIPE -> IDLE
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.setAlpha(0.5f);
            } else if(viewHolder != null && viewHolder instanceof MainRecyclerViewAdapterJava.MainRecyclerViewHolder) {
                getDefaultUIUtil().onSelected(getView(viewHolder));
            }
        }

        /**
         * 유저인터렉션이 종료되는시점에 호출
         * ex) DRAG -> IDLE, SWIPE -> IDLE
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if(viewHolder instanceof MainRecyclerViewAdapterJava.MainRecyclerViewHolder){
                currentDx = 0f;
                previousPosition = viewHolder.getAdapterPosition();
                getDefaultUIUtil().clearView(getView(viewHolder));
                viewHolder.itemView.setAlpha(1.0f);
            }
        }

        /**
         * 유저인터렉션의 시작부터 끝까지 계속호출됨
         * @param c
         * @param recyclerView
         * @param viewHolder
         * @param dX
         * @param dY
         * @param actionState
         * @param isCurrentlyActive
         */
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            if(viewHolder instanceof MainRecyclerViewAdapterJava.FooterViewHolder) return;

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View view = getView(viewHolder);
                boolean isClamped = getTag(viewHolder);
                float x = clampViewPositionHorizontal(view, dX, isClamped, isCurrentlyActive);

                currentDx = x;
                getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        view,
                        x,
                        dY,
                        actionState,
                        isCurrentlyActive
                );
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        /**
         * 스와이프가 동작하는 최소속도 설정
         * @param defaultValue
         * @return
         */
        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue * 8;
        }

        /**
         * 스와이프로 간주되는 경계선의 위치
         * ex) 0.5f : 뷰가 절반이상 넘어가면 스와이프로 간주
         * @param viewHolder
         * @return
         */
        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            boolean isClamped = getTag(viewHolder);
            // 현재 View가 고정되어있지 않고 사용자가 clamp 이상 swipe시 isClamped true로 변경 아닐시 false로 변경
            setTag(viewHolder, !isClamped && currentDx >= clamp);
            return 2f;
        }

        /**
         * 스와이프 뷰 고정 및 해제를 위한 x좌표계산
         * @param view
         * @param dX
         * @param isClamped
         * @param isCurrentlyActive
         * @return
         */
        private float clampViewPositionHorizontal(View view, float dX, boolean isClamped, boolean isCurrentlyActive){
            // LEFT 방향으로 swipe 막기
            float min = 0f;
            // View의 가로 길이의 절반까지만 swipe 되도록
            float max = view.getWidth()/2;

            float x;
            if (isClamped) {
                // View가 고정되었을 때 swipe되는 영역 제한
                if (isCurrentlyActive){
                    x = dX + clamp;
                } else{
                    x = clamp;
                }
            } else {
                x = dX;
            }

            return Math.min(Math.max(min, x), max);
        }

        public void setPreviousPosition(int previousPosition) {
            this.previousPosition = previousPosition;
        }

        private void setTag(RecyclerView.ViewHolder viewHolder, Boolean isClamped){
            viewHolder.itemView.setTag(isClamped);
        }

        private boolean getTag(RecyclerView.ViewHolder viewHolder){
            if(viewHolder.itemView.getTag() != null){
                return (boolean) viewHolder.itemView.getTag();
            } else {
                return false;
            }
        }

        public void setClamp(float clamp){
            this.clamp = clamp;
        }

        @SuppressLint("ObjectAnimatorBinding")
        public void removePreviousClamp(RecyclerView recyclerView){
            if(previousPosition >= 0){
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(previousPosition);
                View view = getView(viewHolder);
                objectAnimator = ObjectAnimator.ofFloat(view, "translationX", view.getX(), 0).setDuration(200);
                objectAnimator.start();
                setTag(viewHolder, false);
                previousPosition = -1;
            }
        }

        private View getView(RecyclerView.ViewHolder viewHolder){
            return ((MainRecyclerViewAdapterJava.MainRecyclerViewHolder)viewHolder).itemView.findViewById(R.id.foregroundView);
        }

        public ObjectAnimator getObjectAnimator(){
            return objectAnimator;
        }

    };

}