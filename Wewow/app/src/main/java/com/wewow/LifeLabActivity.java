package com.wewow;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jaeger.library.StatusBarUtil;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wewow.dto.LabCollection;
import com.wewow.utils.CommonUtilities;
import com.wewow.utils.HttpAsyncTask;
import com.wewow.utils.Utils;
import com.wewow.utils.WebAPIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by suncjs on 2017/3/26.
 */

public class LifeLabActivity extends BaseActivity {

    private ListView lvlifelab;
    private ProgressDialog pdlg = null;
    private int page = 1;
    private static final String TAG = "LifeLabActivity";
    private LabData labData = new LabData();
    private LabData filteredLabData = new LabData();
    CircleProgressBar progressBar;
    private View foot;
    private boolean isLoading = false;
    private ImageView anim_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuselectedPosition(2);
        setContentView(R.layout.activity_lifelab_list);

        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 50);
        this.getSupportActionBar().setTitle(R.string.lifelab_title);
        this.setupUI();
    }

    private void setupUI() {
        progressBar = (CircleProgressBar) this.findViewById(R.id.progressBar);
//        this.foot = View.inflate(this, R.layout.lifelab_foot, null);
//        ImageView iv_loading = (ImageView) foot.findViewById(R.id.iv_loading);
//        Glide.with(this).load(R.drawable.bottom_loading).into(iv_loading);
        this.foot = View.inflate(this, R.layout.circle_load_more, null);

        foot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabActivity.this.startDataLoading();
            }
        });
        this.lvlifelab = (ListView) this.findViewById(R.id.list_lifelab);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 12);
        View view = getLayoutInflater().inflate(R.layout.list_header_life_labs,null);


        this.lvlifelab.addHeaderView(view);
        this.lvlifelab.addFooterView(this.foot);
        this.lvlifelab.setAdapter(new LifeLabAdapter());
        this.lvlifelab.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LabCollection lc = (LabCollection) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(LifeLabActivity.this, LifeLabItemActivity.class);
                intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);
                LifeLabActivity.this.startActivityForResult(intent, 0);

//                LifeLabActivity.this.openLifeLab(adapterView, view, lc);
            }
        });
        this.lvlifelab.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                //Log.d(TAG, String.format("onScroll firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", i, i1, i2));
                if ((LifeLabActivity.this.filteredLabData.getAllCount() == 0) || LifeLabActivity.this.filteredLabData.isAllLoaded()) {
                    LifeLabActivity.this.foot.setVisibility(View.GONE);
                    Log.w(TAG, String.format("onScroll: allCount:%d  isAllLoaded:%b", LifeLabActivity.this.filteredLabData.getAllCount(), LifeLabActivity.this.filteredLabData.isAllLoaded()));
                    return;
                }
                LifeLabActivity.this.foot.setVisibility(View.VISIBLE);
                ImageView imageViewCircle1 = (ImageView) foot.findViewById(R.id.imageViewCircle1);
                ImageView imageViewCircle2 = (ImageView) foot.findViewById(R.id.imageViewCircle2);
                ImageView imageViewCircle3 = (ImageView) foot.findViewById(R.id.imageViewCircle3);
                startAlphaAnimation(imageViewCircle1, 0.8f, 0.2f);
                startAlphaAnimation(imageViewCircle2, 0.5f, 0.8f);
                startAlphaAnimation(imageViewCircle3, 0.2f, 0.8f);
                if (i + i1 == i2) {
                    Log.d(TAG, "onScroll: Show refresh");
                    LifeLabActivity.this.foot.setVisibility(View.VISIBLE);
                    startAlphaAnimation(imageViewCircle1, 0.8f, 0.2f);
                    startAlphaAnimation(imageViewCircle2, 0.5f, 0.8f);
                    startAlphaAnimation(imageViewCircle3, 0.2f, 0.8f);
                    LifeLabActivity.this.startDataLoading();
                }
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        this.startDataLoading();
        this.anim_view = (ImageView) this.findViewById(R.id.anim_view);
    }

    private static class statusHolder {
        public static float listAlpha;
        public static float viewAlpha;
        public static float viewScaleX;
        public static float viewScaleY;
        public static View adapterView;
        public static View itemView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
//            this.revert();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void revert() {
        this.anim_view.setVisibility(View.GONE);
        statusHolder.itemView.setAlpha(statusHolder.viewAlpha);
        statusHolder.adapterView.setAlpha(statusHolder.listAlpha);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = 1.0f;
        this.getWindow().setAttributes(lp);
    }

    private void openLifeLab(AdapterView<?> adapterView, final View view, final LabCollection lc) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = 0.5f;
        this.getWindow().setAttributes(lp);
        statusHolder.listAlpha = adapterView.getAlpha();
        statusHolder.viewAlpha = view.getAlpha();
        statusHolder.viewScaleX = view.getScaleX();
        statusHolder.viewScaleY = view.getScaleY();
        statusHolder.adapterView = adapterView;
        statusHolder.itemView = view;
        AnimatorSet as = new AnimatorSet();
        ValueAnimator av = ObjectAnimator.ofFloat(view, "alpha", statusHolder.viewAlpha, 0f);
        ValueAnimator bv = ObjectAnimator.ofFloat(adapterView, "alpha", statusHolder.listAlpha, 0.5f);
        //as.play(av).with(bv);
        as.play(av);
        as.setDuration(500);
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //Bitmap bm = BlurBuilder.getScreenshot(view);
                //LifeLabActivity.this.anim_view.setBackground(new BitmapDrawable(LifeLabActivity.this.getResources(), bm));
                ViewGroup.LayoutParams lp = LifeLabActivity.this.anim_view.getLayoutParams();
                lp.width = view.getWidth();
                lp.height = view.getHeight();
                LifeLabActivity.this.anim_view.setLayoutParams(lp);
                LifeLabActivity.this.anim_view.setX(view.getX());
                LifeLabActivity.this.anim_view.setY(view.getY());
                LifeLabActivity.this.anim_view.setVisibility(View.VISIBLE);
                LifeLabActivity.this.anim_view.post(new Runnable() {
                    @Override
                    public void run() {
                        AnimatorSet as = new AnimatorSet();
                        ValueAnimator x = ObjectAnimator.ofFloat(LifeLabActivity.this.anim_view, "x", LifeLabActivity.this.anim_view.getX(), 0);
                        ValueAnimator y = ObjectAnimator.ofFloat(LifeLabActivity.this.anim_view, "y", LifeLabActivity.this.anim_view.getY(), 0);
                        float x0 = LifeLabActivity.this.anim_view.getScaleX();
                        float y0 = LifeLabActivity.this.anim_view.getScaleY();
                        float sx = (float) Utils.getScreenWidthPx(LifeLabActivity.this) / (float) LifeLabActivity.this.anim_view.getWidth();
                        ValueAnimator scalex = ObjectAnimator.ofFloat(LifeLabActivity.this.anim_view, "scaleX", x0, x0 * sx * 2);
                        //Log.d(TAG, String.format("run X: %f, %f", x0, x0 * sx));
                        float sy = (float) Utils.getScreenHeightPx(LifeLabActivity.this) / (float) LifeLabActivity.this.anim_view.getHeight();
                        //Log.d(TAG, String.format("screenH: %d, viewH:%d", Utils.getScreenHeightPx(LifeLabActivity.this), LifeLabActivity.this.anim_view.getHeight()));
                        //Log.d(TAG, String.format("y0: %f, sy: %f, y1: %f", y0, sy, y0 * sy));
                        ValueAnimator scaley = ObjectAnimator.ofFloat(LifeLabActivity.this.anim_view, "scaleY", y0, y0 * sy * 6);
                        //Log.d(TAG, String.format("run Y: %f, %f", x0, y0 * sy));
                        as.play(x).with(scalex).with(y).with(scaley);
                        //as.play(x).with(y);
                        as.setDuration(300);
                        as.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                View x = LifeLabActivity.this.anim_view;
                                //Log.d(TAG, String.format("onAnimationEnd: %f %f %d %d", x.getX(), x.getY(), x.getWidth(), x.getHeight()));
                                Intent intent = new Intent(LifeLabActivity.this, LifeLabItemActivity.class);
                                intent.putExtra(LifeLabItemActivity.LIFELAB_COLLECTION, lc);
                                LifeLabActivity.this.startActivityForResult(intent, 0);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        as.start();

                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //
            }
        });
        as.start();
    }

    private class LifeLabAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return LifeLabActivity.this.filteredLabData.getCount();
        }

        @Override
        public Object getItem(int i) {
            return LifeLabActivity.this.filteredLabData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(LifeLabActivity.this, R.layout.lifelab_item, null);
                //view = View.inflate(LifeLabActivity.this, R.layout.layout_lifelab_item1, null);
            }
            LabCollection labcol = (LabCollection) this.getItem(i);
            TextView tv = (TextView) view.findViewById(R.id.tv_lab_title);
            tv.setText(labcol.title);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_lab_image);
            Glide.with(LifeLabActivity.this)
                    .load(labcol.image_688_316)
                    .placeholder(R.drawable.banner_loading_spinner)
                    .crossFade()
                    .into(iv);
            TextView tvvol = (TextView) view.findViewById(R.id.tv_lab_num);
            tvvol.setText(String.format("vol. %d", labcol.order));
            TextView tvread = (TextView) view.findViewById(R.id.tv_lab_read);
            tvread.setText(labcol.read_count);
            TextView tvlike = (TextView) view.findViewById(R.id.tv_lab_collection);
            tvlike.setText(labcol.liked_count);
            return view;
        }
    }

    private void searchData(String keywords) {
        List<Pair<String, String>> ps = new ArrayList<>();
        ps.add(new Pair<>("keywords", keywords));
        progressBar.setVisibility(View.VISIBLE);
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/search_mini", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        progressBar.setVisibility(View.GONE);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(LifeLabActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject data = r.getJSONObject("data");
                                searchDataLoad(data.getJSONArray("collections"));
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                WebAPIHelper.HttpMethod.GET
        };
        new HttpAsyncTask().execute(params);
    }

    private void startDataLoading() {
        if (this.hasLocalData()) {
            this.loadLocalData();
        } else {
            this.loadRemoteData();
        }
    }

    private boolean hasLocalData() {
        return false;
    }

    private void loadLocalData() {
        // TODO: 2017/3/26
    }

    private void loadRemoteData() {
        //this.toggleProgressDialog(true);
        if (this.isLoading) {
            return;
        }
        this.isLoading = true;
        List<Pair<String, String>> ps = new ArrayList<Pair<String, String>>();
        ps.add(new Pair<String, String>("page", String.valueOf(this.labData.getPageToLoad())));
        Object[] params = new Object[]{
                WebAPIHelper.addUrlParams(String.format("%s/index_collections", CommonUtilities.WS_HOST), ps),
                new HttpAsyncTask.TaskDelegate() {

                    @Override
                    public void taskCompletionResult(byte[] result) {
                        //LifeLabActivity.this.toggleProgressDialog(false);
                        progressBar.setVisibility(View.GONE);
                        JSONObject jobj = HttpAsyncTask.bytearray2JSON(result);
                        if (jobj == null) {
                            Toast.makeText(LifeLabActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONObject r = jobj.getJSONObject("result");
                            if (r.getInt("code") != 0) {
                                Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                                Log.d(TAG, String.format("returned code: %d", r.getInt("code")));
                            } else {
                                LifeLabActivity.this.newDataLoad(r.getJSONObject("data"));
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LifeLabActivity.this, R.string.serverError, Toast.LENGTH_LONG).show();
                        }
                        LifeLabActivity.this.isLoading = false;
                    }
                },
                WebAPIHelper.HttpMethod.GET,
                null,
                null,
                true
        };
        new HttpAsyncTask().execute(params);
    }

    private void toggleProgressDialog(boolean isshow) {
        if (isshow && ((this.pdlg == null) || !this.pdlg.isShowing())) {
            this.pdlg = ProgressDialog.show(this, null, null, true, false);
        } else if (!isshow) {
            this.pdlg.dismiss();
        }
    }

    private void searchDataLoad(JSONArray lst) {
        try{
            LabData ld = new LabData();
            ArrayList<LabCollection> l = new ArrayList<>();
            for (int i = 0; i < lst.length(); i++) {
                JSONObject jj = lst.getJSONObject(i);
                LabCollection lc = new LabCollection();
                lc.id = jj.optLong("collection_id");
                lc.image = jj.optString("collection_image");
                lc.title = jj.optString("collection_title");
                lc.date = jj.optString("date");
                lc.liked_count = jj.optString("liked_count");
                lc.order = jj.optInt("order");
                lc.read_count = jj.optString("read_count");
                lc.image_642_320 = jj.optString("image_642_320");
                lc.image_688_316 = jj.optString("image_688_316");
                l.add(lc);
            }
            ld.collections.addAll(l);
            ld.collectionCount = ld.collections.size();
            this.filteredLabData = ld;
            ListAdapter la = this.lvlifelab.getAdapter();
            LifeLabAdapter adp = (LifeLabAdapter) (la instanceof LifeLabAdapter ? la : ((HeaderViewListAdapter) la).getWrappedAdapter());
            adp.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void newDataLoad(JSONObject data) {
        this.labData.addData(data);
        this.filteredLabData = this.labData.filter("");
        ListAdapter la = this.lvlifelab.getAdapter();
        LifeLabAdapter adp = (LifeLabAdapter) (la instanceof LifeLabAdapter ? la : ((HeaderViewListAdapter) la).getWrappedAdapter());
        adp.notifyDataSetChanged();
    }

    private class LabData {

        private ArrayList<LabCollection> collections = new ArrayList<LabCollection>();
        private int pagesize = 10;
        private int collectionCount = 0;
        private int pageCount = 0;
        private int currentPage = -1;

        public LabData() {

        }

        public synchronized void addData(JSONObject jobj) {
            Log.d(TAG, String.format("before add allcount: %d  count: %d ", this.getAllCount(), this.getCount()));
            try {
                this.pageCount = jobj.getInt("total_pages");
                this.pagesize = jobj.getInt("pagesize");
                this.currentPage = jobj.getInt("current_page");
                this.collectionCount = jobj.getInt("collection_count");
                JSONArray lst = jobj.getJSONArray("collection_list");
                ArrayList<LabCollection> l = new ArrayList<>();
                for (int i = 0; i < lst.length(); i++) {
                    JSONObject jj = lst.getJSONObject(i);
                    LabCollection lc = new LabCollection();
                    lc.id = jj.getLong("collection_id");
                    lc.image = jj.getString("collection_image");
                    lc.title = jj.getString("collection_title");
                    lc.date = jj.getString("date");
                    lc.liked_count = jj.getString("liked_count");
                    lc.order = jj.getInt("order");
                    lc.read_count = jj.getString("read_count");
                    lc.image_642_320 = jj.getString("image_642_320");
                    lc.image_688_316 = jj.getString("image_688_316");
                    l.add(lc);
                }
                this.collections.addAll(this.sort(l));
            } catch (JSONException e) {
                Log.e(TAG, "addData fail");
            }
            Log.d(TAG, String.format("after add allcount: %d  count: %d ", this.getAllCount(), this.getCount()));
        }

        private List<LabCollection> sort(ArrayList<LabCollection> list) {
            LabCollection[] arr = list.toArray(new LabCollection[]{});
            Arrays.sort(arr, this.comparator);
            return Arrays.asList(arr);
        }

        private Comparator<LabCollection> comparator = new Comparator<LabCollection>() {
            @Override
            public int compare(LabCollection t0, LabCollection t1) {
                return t0.order > t1.order ? -1 : 1;
            }
        };

        public int getCurrentPage() {
            return this.currentPage;
        }

        public synchronized int getPageToLoad() {
            return this.currentPage <= 0 ? 1 : this.currentPage + 1;
        }

        public int getCount() {
            return this.collections.size();
        }

        public int getAllCount() {
            return this.collectionCount;
        }

        public int getPageCount() {
            return this.pageCount;
        }

        public boolean isItemLoaded(int i) {
            return this.collections.size() > i;
        }

        public LabCollection get(int i) {
            return this.isItemLoaded(i) ? this.collections.get(i) : null;
        }

        public boolean isAllLoaded() {
            return this.getCount() == this.getAllCount();
        }

        public LabData filter(String query) {
            LabData ld = new LabData();
            if (query.trim().isEmpty()) {
                ld.collections = (ArrayList<LabCollection>) this.collections.clone();
                ld.collectionCount = this.collectionCount;
            } else {
                for (int i = 0; i < this.collections.size(); i++) {
                    LabCollection lab = this.collections.get(i);
                    if (lab.title.contains(query)) {
                        ld.collections.add(lab);
                    }
                }
                ld.collectionCount = ld.collections.size();
            }
            ld.currentPage = this.currentPage;
            ld.pageCount = this.pageCount;
            ld.pagesize = this.pagesize;
            return ld;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                LifeLabActivity.this.searchLab("");
                return true;
            }
        });
        menuItem.setVisible(true);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button)).setImageResource(R.drawable.search_b);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
//                LifeLabActivity.this.searchLab(query);
                searchData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    private void searchLab(String query) {
        this.filteredLabData = this.labData.filter(query);
        ListAdapter la = this.lvlifelab.getAdapter();
        LifeLabAdapter adp = (LifeLabAdapter) (la instanceof LifeLabAdapter ? la : ((HeaderViewListAdapter) la).getWrappedAdapter());
        adp.notifyDataSetChanged();
    }

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

}
