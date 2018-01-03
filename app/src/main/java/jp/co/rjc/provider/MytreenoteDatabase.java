package jp.co.rjc.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DB接続のヘルパクラス.
 */
final public class MytreenoteDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "mytreenote_android.db";
    private static final int DB_VERSION = 1;

    /**
     * テーブル名定義.
     */
    public interface Tables {

        /**
         * トピック情報のテーブル名です.
         */
        String TOPIC_INFO = "topic_info";
    }

    /**
     * 標準列名定義.
     */
    public interface BaseColumns {

        /**
         * 標準のユニークID.
         */
        String _ID = "_id";
    }

    /**
     * トピック情報の列定義.
     */
    public interface TopicInfoColumns {
        /**
         * レコード.
         */
        String RECORD = "topic_record";
        /**
         * ネスト.
         */
        String NEST = "nest";
        /**
         * 本文.
         */
        String TOPIC_TEXT = "topic_text";
        /**
         * 展開フラグ.
         */
        String OPEN_FLG = "open_flg";
        /**
         * カレントトピックフラグ.
         */
        String CURRENT_TOPIC_FLG = "current_topic_flg";
    }

    public MytreenoteDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTopicInfo(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    /**
     * トピック情報テーブルを作成します.
     *
     * @param db
     */
    private void createTopicInfo(final SQLiteDatabase db) {
        db.execSQL(
                "CREATE " +
                        "TABLE IF NOT EXISTS "
                        + Tables.TOPIC_INFO
                        + "("
                        + BaseColumns._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                        + TopicInfoColumns.RECORD + " INTEGER NOT NULL UNIQUE, "
                        + TopicInfoColumns.NEST + " INTEGER NOT NULL, "
                        + TopicInfoColumns.TOPIC_TEXT + " TEXT , "
                        + TopicInfoColumns.OPEN_FLG + " TEXT , "
                        + TopicInfoColumns.CURRENT_TOPIC_FLG + " TEXT"
                        + ");"
        );
    }
}