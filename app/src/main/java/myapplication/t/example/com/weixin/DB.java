package myapplication.t.example.com.weixin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zsl on 2018/4/21.
 */

public class DB extends SQLiteOpenHelper {
    public DB(Context context) {
        super(context, "vocab", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE vocab(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "word TEXT DEFAULT \"\"," +
                "trans TEXT DEFAULT \"\")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
