package javatesting.com.sqlandservice.pic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javatesting.com.sqlandservice.R;

/**
 *
 */

public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * GridView的实例
     */
    private GridView mPhotoWall;

    /**
     * 第一张可见图片的下标
     */
    private int mFirstVisibleItem;

    /**
     * 一屏有多少张图片可见
     */
    private int mVisibleItemCount;

    /**
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。
     */
    private boolean isFirstEnter = true;

    public PhotoWallAdapter(Context context, int textViewResourceId, GridView photoWall) {
        super(context, textViewResourceId, imageThumbUrls);
        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();
        // 获取应用程序最大可用内存
        final int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                Log.e("getByteCount:", bitmap.getByteCount() + "\nall:"+maxMemory);
                return bitmap.getByteCount();
            }
        };
        mPhotoWall.setOnScrollListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout, null);
        } else {
            view = convertView;
        }
        final ImageView photo = (ImageView) view.findViewById(R.id.photo);
        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序
        photo.setTag(url);
        setImageView(url, photo);
        return view;
    }

    /**
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存，
     * 就给ImageView设置一张默认图片。
     *
     * @param imageUrl  图片的URL地址，用于作为LruCache的键。
     * @param imageView 用于显示图片的控件。
     */
    private void setImageView(String imageUrl, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key    LruCache的键，这里传入图片的URL地址。
     * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的Bitmap对象，或者null。
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 仅当GridView静止时才去下载图片，GridView滑动时取消所有正在下载的任务
        if (scrollState == SCROLL_STATE_IDLE) {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        } else {
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        // 下载的任务应该由onScrollStateChanged里调用，但首次进入程序时onScrollStateChanged并不会调用，
        // 因此在这里为首次进入程序开启下载任务。
        if (isFirstEnter && visibleItemCount > 0) {
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。
     *
     * @param firstVisibleItem 第一个可见的ImageView的下标
     * @param visibleItemCount 屏幕中总共可见的元素数
     */
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                String imageUrl = imageThumbUrls[i];
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap == null) {
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(imageUrl);
                } else {
                    ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }

    /**
     * 异步下载图片的任务。
     *
     * @author guolin
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        /**
         * 图片的URL地址
         */
        private String imageUrl;

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            // 在后台开始下载图片
            Bitmap bitmap = downloadBitmap(params[0]);
            if (bitmap != null) {
                // 图片下载完成后缓存到LrcCache中
                addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

        /**
         * 建立HTTP请求，并获取Bitmap对象。
         *
         * @param imageUrl 图片的URL地址
         * @return 解析后的Bitmap对象
         */
        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return bitmap;
        }

    }

    public static String[] imageThumbUrls = new String[]{
            "https://ps.ssl.qhimg.com/dm/364_207_/t019c6208859dcb82ae.jpg",
            "http://img5.imgtn.bdimg.com/it/u=865490461,298614916&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=811071632,1787347836&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2097996470,2706206864&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=2293675559,2668479973&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=409040175,1823726235&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2065245370,1446305286&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=105716856,2056319326&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1284820083,2828774046&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3888693475,2493143600&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3137457130,171366155&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=759399609,3656639612&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=774007663,3303363317&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=2443517348,1360690026&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=575406952,2734030340&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=3428486457,1250376236&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3663079106,3493364143&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=3814234504,106066374&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2708401893,2279207785&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2857587871,3727899466&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3821526675,3796993096&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=668641845,2654565205&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1127021212,1862260799&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2074660006,1174425303&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3513026479,3548693918&fm=27&gp=0.jpg",
            "https://ps.ssl.qhimg.com/dm/364_207_/t019c6208859dcb82ae.jpg",
            "http://img5.imgtn.bdimg.com/it/u=865490461,298614916&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=811071632,1787347836&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2097996470,2706206864&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=2293675559,2668479973&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=409040175,1823726235&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2065245370,1446305286&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=105716856,2056319326&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1284820083,2828774046&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3888693475,2493143600&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3137457130,171366155&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=759399609,3656639612&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=774007663,3303363317&fm=27&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=2443517348,1360690026&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=575406952,2734030340&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=3428486457,1250376236&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3663079106,3493364143&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=3814234504,106066374&fm=27&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2708401893,2279207785&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2857587871,3727899466&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3821526675,3796993096&fm=27&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=668641845,2654565205&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1127021212,1862260799&fm=27&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2074660006,1174425303&fm=27&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3513026479,3548693918&fm=27&gp=0.jpg"
    };
}
