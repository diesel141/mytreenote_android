package jp.co.rjc.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import jp.co.rjc.mytreenote_android.R;
import jp.co.rjc.provider.MytreenoteDatabase;

/**
 * トピック情報のデータアクセスオブジェクトを提供.
 */
public final class TopicDao implements MytreenoteDatabase.Tables, MytreenoteDatabase.TopicInfoColumns {

    private Context mContext;
    private ContentResolver mContentResolver;

    public TopicDao(final Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    /**
     * 指定したレコードのトピックを削除.
     *
     * @param topicRecord
     */
    public void deleteTopic(final int topicRecord) {
        MytreenoteDatabase db = new MytreenoteDatabase(mContext);
        String whereClause = RECORD + " = ?";
        String[] whereArgs = new String[]{Integer.toString(topicRecord)};

        final SQLiteDatabase writable = db.getWritableDatabase();
        try {
            writable.delete(TOPIC_INFO, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public void bulkInsert(final List<ContentValues> valuesList) {
        MytreenoteDatabase db = new MytreenoteDatabase(mContext);
        final SQLiteDatabase writable = db.getWritableDatabase();
        writable.beginTransaction();
        try {
            writable.delete(TOPIC_INFO, null, null);
            for (ContentValues values : valuesList) {
                writable.insert(TOPIC_INFO, null, values);
            }
            writable.setTransactionSuccessful();
        } finally {
            writable.endTransaction();
            writable.close();
        }
    }

    public List<ContentValues> getTopics() {
        final MytreenoteDatabase db = new MytreenoteDatabase(mContext);
        final SQLiteDatabase readable = db.getReadableDatabase();
        try {
            final Cursor cursor = readable.query(TOPIC_INFO, null, null, null, null, null, RECORD);

            final List<ContentValues> valuesList = new ArrayList<>();
            while (cursor.moveToNext()) {
                final ContentValues values = new ContentValues();
                values.put(RECORD, cursor.getInt(cursor.getColumnIndex(RECORD)));
                values.put(NEST, cursor.getInt(cursor.getColumnIndex(NEST)));
                final String topicText = cursor.getString(cursor.getColumnIndex(TOPIC_TEXT));
                values.put(TOPIC_TEXT, TextUtils.isEmpty(topicText) ? mContext.getResources().getString(R.string.empty_param) : topicText);
                values.put(OPEN_FLG, cursor.getString(cursor.getColumnIndex(OPEN_FLG)));
                values.put(CURRENT_TOPIC_FLG, cursor.getString(cursor.getColumnIndex(CURRENT_TOPIC_FLG)));
                valuesList.add(values);
            }
            return valuesList;
        } finally {
            db.close();
        }
    }
}
