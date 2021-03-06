package com.wewow.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.wewow.R;
import com.wewow.adapter.FooterAdapter;
import com.wewow.utils.LoadMoreListener;

import java.util.Date;

/**
 * Created by iris on 17/5/9.
 */

/**
 * zhangyao
 * 16/9/4
 * zhangyao@jiandanxinli.com
 */
public class RecyclerViewUpRefresh extends RecyclerView {

    private Context mContext;

    private LoadMoreListener loadMoreListener;
    //是否可加载更多
    private boolean canloadMore = true;

    private Adapter mAdapter;

    private Adapter mFooterAdapter;

    private ListView list;

    public boolean isLoadingData = false;
    //加载更多布局
    private LoadingMoreFooter loadingMoreFooter;
    private long TimeFlag;
    private int eview_height = 1;

    public RecyclerViewUpRefresh(Context context) {
        this(context, null);
    }

    public RecyclerViewUpRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewUpRefresh(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LoadingMoreFooter footView = new LoadingMoreFooter(mContext);
        addFootView(footView);
        footView.setGone();
    }


    //点击监听
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemClickListener(onItemClickListener);
        }
    }


    //长按监听
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemLongClickListener(listener);
        }
    }

    /**
     * 底部加载更多视图
     *
     * @param view
     */
    public void addFootView(LoadingMoreFooter view) {
        loadingMoreFooter = view;
    }

    //设置底部加载中效果
    public void setFootLoadingView(View view) {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.addFootLoadingView(view);
        }
    }

    //设置底部到底了布局
    public void setFootEndView(View view) {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.addFootEndView(view);
        }
    }

    //下拉刷新后初始化底部状态
    public void refreshComplete() {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.setGone();
        }
        isLoadingData = false;
    }

    public void loadMoreComplete() {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.setGone();
        }
        isLoadingData = false;
    }


    //到底了
    public void loadMoreEnd() {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.setEnd();
        }
    }

    //设置是否可加载更多
    public void setCanloadMore(boolean flag) {
        canloadMore = flag;
    }

    //设置加载更多监听
    public void setLoadMoreListener(LoadMoreListener listener) {
        loadMoreListener = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mFooterAdapter = new FooterAdapter(this, loadingMoreFooter, adapter);
        super.setAdapter(mFooterAdapter);
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && loadMoreListener != null && !isLoadingData && canloadMore) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = last(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 2) {
                setNestedScrollingEnabled(true);
                showFooterAnimation();

            }
        }
    }

    public void showFooterAnimation() {
        if (loadingMoreFooter != null) {
            loadingMoreFooter.setVisible();
            ImageView imageViewCircle1 = (ImageView) loadingMoreFooter.findViewById(R.id.imageViewCircle1);
            ImageView imageViewCircle2 = (ImageView) loadingMoreFooter.findViewById(R.id.imageViewCircle2);
            ImageView imageViewCircle3 = (ImageView) loadingMoreFooter.findViewById(R.id.imageViewCircle3);
            startAlphaAnimation(imageViewCircle1, 0.8f, 0.2f);
            startAlphaAnimation(imageViewCircle2, 0.5f, 0.8f);
            startAlphaAnimation(imageViewCircle3, 0.2f, 0.8f);

        }
        isLoadingData = true;
//        mAdapter.notifyDataSetChanged();

        loadMoreListener.onLoadMore();
    }

    //取到最后的一个节点
    private int last(int[] lastPositions) {
        int last = lastPositions[0];
        for (int value : lastPositions) {
            if (value > last) {
                last = value;
            }
        }
        return last;
    }


    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mFooterAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mFooterAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mFooterAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    public void startAlphaAnimation(View view, float from, float to) {
        /**
         * @param fromAlpha 开始的透明度，取值是0.0f~1.0f，0.0f表示完全透明， 1.0f表示和原来一样
         * @param toAlpha 结束的透明度，同上
         */
        AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        //设置动画持续时长
        alphaAnimation.setDuration(300);

        //设置动画播放次数
        alphaAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        //开始动画
        view.startAnimation(alphaAnimation);

    }

    private void resetEmptyView() {
        final int dx = eview_height;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final int time = 500;
                final long startTime = new Date().getTime();
                TimeFlag = startTime;
                long nowTime = new Date().getTime();
                while (startTime + time > nowTime && TimeFlag == startTime) {
                    nowTime = new Date().getTime();
                    final int dt = (int) (nowTime - startTime);
                    post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            eview_height = eview_height * (time - dt) / time;
                            mFooterAdapter.notifyDataSetChanged();
                        }
                    });

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
               post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        eview_height = 0;
                        mFooterAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void notifyEmptyView(int height) {
        this.eview_height = height;
        mFooterAdapter.notifyItemChanged(mFooterAdapter.getItemCount() - 1);
    }

}
