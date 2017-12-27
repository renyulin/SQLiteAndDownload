package javatesting.com.sqlandservice.lite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javatesting.com.sqlandservice.R;


/**
 * https://github.com/LitePalFramework/LitePal
 * LitePal的基本使用
 */
public class LitePalActivity extends Activity {
    private ImageView litePalImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lite_pal);
        litePalImg = findViewById(R.id.litePalImg);
    }

    public void litePalClick(View view) {
        switch (view.getId()) {
            case R.id.litePal_create:
                createLitePal();
                break;
            case R.id.litePal_add:
                litePalAddData();
                break;
            case R.id.litePal_update:
                litePalUpdateData();
                break;
            case R.id.litePal_delete:
                litePalDeleteData();
                break;
            case R.id.litePal_query:
                litePalQueryData();
                break;
            case R.id.litePal_query_all:
                litePalQueryAllData();
                break;
        }
    }

    private void litePalQueryData() {
        Book firstBook = DataSupport.findLast(Book.class);//findFirst
        //单查某几列数据
        List<Book> selectBook = DataSupport.select("name", "author").find(Book.class);
        //某一项数据对应范围查询方式
        List<Book> whereBook = DataSupport.where("pages > ? and name = ?", "51", "BBBB").find(Book.class);
        //根据条件从高到低 desc  asc 升序
        List<Book> orderBook = DataSupport.order("price desc").find(Book.class);
        //limit查询结果的数量   offset偏移量 第234条数据
        List<Book> limitBook = DataSupport.limit(3).offset(1).find(Book.class);
    }

    private void litePalQueryAllData() {
        List<Book> books = DataSupport.findAll(Book.class);
        for (Book book : books) {
            Log.e("litePalQueryAll", book.getAuthor() + "," + book.getName() + ","
                    + book.getPress() + "," + book.getPrice() + "," + book.getPages());
            if (book.getCover() != null) {
                litePalImg.setImageBitmap(byteToBitmap(book.getCover()));
            }
        }
    }

    private void litePalDeleteData() {
        DataSupport.deleteAll(Book.class, "price < ?", "15");
    }

    private void litePalUpdateData() {
        Book book = new Book();
        book.setPages(55);
        book.setPrice(188.02);
        book.setCover(bitmapToByte(resourcesToBitmap()));
//        book.setToDefault("pages");//设置页数为0
        //指定对应参数为对应的字段时的更新全部操作  不填，全部数据都更新
        book.updateAll("name = ? and author = ?", "BBBB", "ACB");
    }

    private Bitmap resourcesToBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher
//                , options
        );
        return bitmap;
    }

    private byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }
/**
 * 图片转换
 * http://blog.csdn.net/z104207/article/details/6634774
 */
    /**
     * 将字节数组转为Bitmap对象
     */
    private Bitmap byteToBitmap(byte[] b) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bitmap;
    }

    private void litePalAddData() {
        Book book = null;
        for (int i = 0; i < 2; i++) {
            book = new Book();
            book.setAuthor("ACB");
            book.setName("BBBB");
            book.setPages(50);
            book.setPress("2001版");
            book.setPrice(88.02);
            book.save();
        }

        book.setPrice(8.25);
        book.save();

    }

    private void createLitePal() {
        Connector.getDatabase();
    }
}
