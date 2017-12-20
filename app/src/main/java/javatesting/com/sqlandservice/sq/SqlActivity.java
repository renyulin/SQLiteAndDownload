package javatesting.com.sqlandservice.sq;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import javatesting.com.sqlandservice.R;

/**
 * Error
 * java.lang.IllegalStateException: Couldn't read row 1, col 0 from CursorWindow.
 * Make sure the Cursor is initialized correctly before accessing data from it.
 * <p>
 * 常见的错误原因解决：
 * <p>
 * 错误1：请求的字段在数据库的表中不存在，一般是大小写没写对。
 * <p>
 * 错误2：编程的中途改变表的字段，实际字段并没有改变，解决方法是卸载当前版本，再安装调试。
 * <p>
 * 错误3：查询语句中并没有查询该字段，使用的时候却要得到该字段的值。
 */
public class SqlActivity extends Activity {
    private MyDatabaseHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql);
        init();
    }

    private void init() {
        //第一次创建
//        helper = new MyDatabaseHelper(this, "BookStore.db", null, 1);
        //第二次创建
        helper = new MyDatabaseHelper(this, "BookStore.db", null, 4);
    }

    public void sqlClick(View view) {
        switch (view.getId()) {
            case R.id.sql_create:
                helper.getWritableDatabase();
                break;
            case R.id.sql_add:
                sqlAddData();
                break;
            case R.id.sql_update:
                sqlUpdateData();
                break;
            case R.id.sql_delete:
                sqlDeleteData();
                break;
            case R.id.sql_query:
                sqlQueryData();
                break;
            case R.id.query_all:
                queryAllData();
                break;
        }
    }

    /**
     * 查询所有
     * String table 表名
     * String[] columns 列名
     * String selection where的约束条件
     * String[] selectionArgs 为where中的占位符提供具体的值
     * String groupBy 置顶需要group by的列
     * String having 对group by的查询结果进一步约束
     * String orderBy 指定查询结果的排序方式
     * String limit
     */
    private void queryAllData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, null,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String author = cursor.getString(cursor.getColumnIndex("author"));
                int pages = cursor.getInt(cursor.getColumnIndex("pages"));
                double price = cursor.getDouble(cursor.getColumnIndex("price"));
                Log.e("query_data_all", name + " ; " + author + " ; " + pages + " ; " + price);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void sqlQueryData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        /*
        //等价：selectionArgs中的字符串就是对应selection中的问号所代表的变量
        //        Cursor cursor = db.query("Book", null, "pages=50",
//               null, null, null, null );
//        Cursor cursor = db.query("Book", null, "pages=50",
//                null, null, null, null );
         */
        /**
         * having 聚合函数：HAVING 子句允许指定条件来过滤将出现在最终结果中的分组结果。
         * WHERE 子句在所选列上设置条件，而 HAVING 子句则在由 GROUP BY 子句创建的分组上设置条件。
         * groupBy 分组，将需要查询的字段分组，相同的一组
         * orderBy 正序倒序排序
         */
        Cursor cursor = db.query("Book", new String[]{"name", "author", "pages", "price"}, "pages >= ?",
                new String[]{"50"}, "name", "price>60", "author", null);
        if (cursor.moveToFirst()) {
            do {
                int pages = 0;
                String author = null;
                String name = null;
                try {
                    author = cursor.getString(cursor.getColumnIndex("author"));
                } catch (Exception E) {
                }
                try {
                    pages = cursor.getInt(cursor.getColumnIndex("pages"));
                } catch (Exception E) {
                }
                try {
                    name = cursor.getString(cursor.getColumnIndex("name"));
                } catch (Exception E) {
                }
                double price = 0;
                try {
                    price = cursor.getDouble(cursor.getColumnIndex("price"));
                } catch (Exception E) {
                }
                Log.e("query_data_all", name + " ; " + author + " ; " + pages + " ; " + price);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    private void sqlDeleteData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("Book", "pages > ?", new String[]{"500"});
    }

    /**
     * 更新数据
     */
    private void sqlUpdateData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("price", 88);
        db.update("Book", values, "name = ?", new String[]{"The child code"});
    }

    /**
     * 给数据库添加数据
     */
    private void sqlAddData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", "The child code");
        values.put("author", "Frank");
        values.put("price", 46.96);
        values.put("pages", 50);
        db.insert("Book", null, values);
        values.clear();
        values.put("name", "The boy code");
        values.put("author", "Lin");
        values.put("price", 55.58);
        values.put("pages", 505);
        db.insert("Book", null, values);
        values.clear();
        values.put("name", "The girl code");
        values.put("author", "ging");
        values.put("price", 85.58);
        values.put("pages", 305);
        db.insert("Book", null, values);
    }
}
