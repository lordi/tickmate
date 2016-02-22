package lab.prada.android.ui.infinitescroll;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class InfiniteScrollAdapter<T extends BaseAdapter> extends BaseAdapter {

    private boolean shouldInfiniteScrollAtTop = true; // If false, then scroll at the bottom
    private final T mAdapter;
    private final View mProgressView;
    private Vector<InfiniteScrollListener> mListeners = new Vector<InfiniteScrollListener>();
    
    private final static int NONE_STATE = 0;
    private final static int REFRESHING_STATE = 1;
    private AtomicInteger state = new AtomicInteger(NONE_STATE);
    private boolean mCanReadMore = true;


    // SCROLL_DOWN_THRESHOLD should be less than TickAdapter.DEFAULT_COUNT_PAST, otherwise the first load happens in two (or more) chunks
    private final static int SCROLL_DOWN_THRESHOLD = 15;  // Determines when to 'infinite scroll'; must be large enough to ensure position reaches the threshold, even on larger screens
    private Context context;

    public interface InfiniteScrollListener {
        public void onInfiniteScrolled();
    }

    public InfiniteScrollAdapter(Context context, T adapter, View progressView) {
        mAdapter = adapter;
        mProgressView = progressView;
        this.context = context;
    }

    public InfiniteScrollAdapter(Context context, T adapter, int itemWidth,
                                 int itemHeight) {
        mAdapter = adapter;
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new GridView.LayoutParams(itemWidth,
                itemHeight));
        layout.setGravity(Gravity.CENTER);
        layout.addView(new ProgressBar(context));

        mProgressView = layout;
    }

    public InfiniteScrollAdapter(T adapter, View progressView) {
        mAdapter = adapter;
        mProgressView = progressView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return (isProgressViewPosition(position)) ? null : mAdapter
                .getDropDownView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return (isProgressViewPosition(position)) ? Adapter.IGNORE_ITEM_VIEW_TYPE
                : mAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        return (isProgressViewPosition(position)) ? false : mAdapter
                .isEnabled(position);
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return shouldShowProgressView() ? mAdapter.getCount() + 1 : mAdapter
                .getCount();
    }

    @Override
    public Object getItem(int position) {
        return (isProgressViewPosition(position)) ? null : mAdapter
                .getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return (isProgressViewPosition(position)) ? -1 : mAdapter
                .getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isProgressViewPosition(position)) {
            if (isRefreshing() == false) {
                state.set(REFRESHING_STATE);
                if (mListeners != null) {
                    for (InfiniteScrollListener listener : mListeners) {
                        listener.onInfiniteScrolled();
                    }
                }
            }
            if (mProgressView.getVisibility() == View.GONE) {
                mProgressView.setVisibility(View.VISIBLE);
            }
            return mProgressView;
        } else {
            return mAdapter.getView(position, convertView, parent);
        }
    }

    public T getOriginalAdapter() {
        return mAdapter;
    }

    public void removeListener(InfiniteScrollListener listener) {
        if (listener != null) {

        }
    }

    public void addListener(InfiniteScrollListener listener) {
        if (listener != null && mListeners.contains(listener) == false) {
            mListeners.add(listener);
        }
    }

    public boolean isProgressViewPosition(int position) {
        shouldInfiniteScrollAtTop = ! PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean("reverse-date-order-key", false);

        if (shouldInfiniteScrollAtTop) {
            return shouldShowProgressView() && position == 0;
        } else {  // scroll at bottom
            return shouldShowProgressView() && position > (getCount() - SCROLL_DOWN_THRESHOLD);
        }
    }

    private boolean shouldShowProgressView() {
        return mAdapter.getCount() > 0 && mCanReadMore;
    }

    public void canReadMore(boolean enable) {
        mCanReadMore  = enable;
    }

    public boolean isRefreshing() {
        return state.get() == REFRESHING_STATE;
    }

    public void handledRefresh() {
        assert(Looper.myLooper().equals(Looper.getMainLooper()));
        if (isRefreshing()) {
            mProgressView.setVisibility(View.GONE);
            state.set(NONE_STATE);
        }
    }

    public T getAdapter() {
        return mAdapter;
    }
}
