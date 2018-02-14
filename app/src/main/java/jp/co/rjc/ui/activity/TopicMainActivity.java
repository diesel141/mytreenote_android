package jp.co.rjc.ui.activity;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.rjc.dao.TopicDao;
import jp.co.rjc.mytreenote_android.R;
import jp.co.rjc.provider.MytreenoteDatabase;

public class TopicMainActivity extends AppCompatActivity implements MytreenoteDatabase.TopicInfoColumns {

    private Context mContext;
    private TopicDao mTopicDao;
    private List<ContentValues> mValuesList;
    private TopicItemView mCurrentTopic;

    @BindView(R.id.topic_unnest)
    TextView mTopicUnnest;
    @BindView(R.id.topic_nest)
    TextView mTopicNest;
    @BindView(R.id.root_bg)
    ScrollView mRootBackGround;
    @BindView(R.id.topic_path_rect)
    LinearLayout mTopicPathRect;
    @BindView(R.id.topics_rect)
    LinearLayout mTopicsRect;
    @BindView(R.id.create_topic)
    TextView mTopicPlusView;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ライフサイクル

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.R.style.Theme_Black_NoTitleBar);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.title_bar_topic);

        mContext = getApplicationContext();
        mTopicDao = new TopicDao(mContext);
        initView();
        initParams();
    }

    @Override
    protected void onPause() {
        previusUpdateTopics();
        mTopicDao.bulkInsert(mValuesList);
        postUpdateTopics(mCurrentTopic);
        super.onPause();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initView() {
        ButterKnife.bind(this);
        mRootBackGround.setOnTouchListener((v, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                previusUpdateTopics();
                mTopicDao.bulkInsert(mValuesList);
                postUpdateTopics(null);
            }
            return false;
        });
        mTopicUnnest.setOnClickListener(v -> {
            final int currentNest = mCurrentTopic.mNest;
            int currentRecord = mCurrentTopic.mTopicRecord;
            int nextRecordNest = mCurrentTopic.mNest;

            final boolean isMoveUnderHierarky = mTopicsRect.getChildAt(currentRecord + 1) != null
                    && currentNest == ((TopicItemView) mTopicsRect.getChildAt(currentRecord + 1)).mNest;

            while (nextRecordNest >= currentNest && mTopicsRect.getChildCount() > currentRecord) {
                TopicItemView item = ((TopicItemView) mTopicsRect.getChildAt(currentRecord));

                if (mTopicsRect.getChildAt(currentRecord + 1) != null) {
                    final int tmpNextRecordNest = ((TopicItemView) mTopicsRect.getChildAt(currentRecord + 1)).mNest;
                    if (isMoveUnderHierarky) {
                        nextRecordNest = tmpNextRecordNest;
                        currentRecord++;

                        if (currentNest > tmpNextRecordNest) {
                            mCurrentTopic.mNest--;
                            mTopicsRect.removeView(mCurrentTopic);
                            mTopicsRect.addView(mCurrentTopic, item.mTopicRecord);
                            break;
                        }
                    } else {
                        item.mNest--;
                        if (currentNest < tmpNextRecordNest) {

                            nextRecordNest = tmpNextRecordNest;
                            currentRecord++;
                        } else {
                            break;

                        }
                    }
                } else {
                    item.mNest--;
                    break;
                }
            }
            previusUpdateTopics();
            mTopicDao.bulkInsert(mValuesList);
            postUpdateTopics(mCurrentTopic);
        });
        mTopicNest.setOnClickListener(v -> {
            final int currentNest = mCurrentTopic.mNest;
            int currentRecord = mCurrentTopic.mTopicRecord;
            int nextRecordNest = mCurrentTopic.mNest;

            while (nextRecordNest >= currentNest && mTopicsRect.getChildCount() > currentRecord) {
                ((TopicItemView) mTopicsRect.getChildAt(currentRecord)).mNest++;

                if (mTopicsRect.getChildAt(currentRecord + 1) != null) {
                    final int tmpNextRecordNest = ((TopicItemView) mTopicsRect.getChildAt(currentRecord + 1)).mNest;
                    if (tmpNextRecordNest <= currentNest) {
                        break;
                    } else {
                        nextRecordNest = tmpNextRecordNest;
                        currentRecord++;
                    }
                } else {
                    break;
                }
            }
            previusUpdateTopics();
            mTopicDao.bulkInsert(mValuesList);
            postUpdateTopics(mCurrentTopic);
        });
    }

    private void initParams() {
        mValuesList = new ArrayList<>();
        mValuesList = mTopicDao.getTopics();
        for (ContentValues values : mValuesList) {
            TopicItemView item = new TopicItemView(values);
            mTopicsRect.addView(item);
        }
        postUpdateTopics(null);

        mTopicPlusView.setOnClickListener(v -> {
            final int topicRecord;
            if (mTopicPathRect.getChildCount() > 0) {
                topicRecord = ((TopicItemView) mTopicPathRect.getChildAt(0)).mTopicRecord;
            } else {
                topicRecord = mValuesList.size();
            }
            ContentValues values = new ContentValues();
            values.put(RECORD, topicRecord);
            values.put(NEST, topicRecord > 0 ? mValuesList.get(topicRecord - 1).getAsInteger(NEST) : 0);
            values.put(TOPIC_TEXT, getResources().getString(R.string.empty_param));
            values.put(OPEN_FLG, getResources().getString(R.string.flag_on));
            values.put(CURRENT_TOPIC_FLG, getResources().getString(R.string.flag_off));
            mValuesList.add(values);
            mTopicDao.bulkInsert(mValuesList);

            TopicItemView item = new TopicItemView(values);
            mTopicsRect.addView(item);
            postUpdateTopics(item);
        });
    }

    /**
     * トピック更新前の処理.
     */
    private void previusUpdateTopics() {
        List<ContentValues> valuesList = new ArrayList<>();
        for (int i = 0; i < mTopicsRect.getChildCount(); i++) {

            TopicItemView item = (TopicItemView) mTopicsRect.getChildAt(i);
            final String topicText = item.mTopicEdit.getText().toString();

            final ContentValues values = mValuesList.get(i);
            values.put(RECORD, i);
            values.put(NEST, item.mNest);
            values.put(TOPIC_TEXT, topicText);
            values.put(OPEN_FLG, item.mOpenFlg);
            valuesList.add(values);
        }
        mValuesList.clear();
        mValuesList = valuesList;
    }

    /**
     * トピック更新後の処理
     *
     * @param editTopic
     */
    private void postUpdateTopics(TopicItemView editTopic) {

        mCurrentTopic = null;
        mTopicUnnest.setTextColor(getResources().getColor(R.color.disabled_gray));
        mTopicUnnest.setEnabled(false);
        mTopicNest.setTextColor(getResources().getColor(R.color.disabled_gray));
        mTopicNest.setEnabled(false);

        final int topicCount = mTopicsRect.getChildCount();
        final float indentWidth = getResources().getDimension(R.dimen.indent_width);
        for (int i = 0; i < topicCount; i++) {
            TopicItemView item = (TopicItemView) mTopicsRect.getChildAt(i);
            item.mTopicRecord = i;
            item.mTopicLabel.setVisibility(View.VISIBLE);
            item.mTopicLabel.setBackgroundColor(getResources().getColor(R.color.transparent));
            item.mTopicEdit.setVisibility(View.GONE);
            item.mIndentSpace.removeAllViews();
            item.mIndentSpace.setVisibility(View.GONE);
            if (item.mNest > 0) {
                item.mIndentSpace.setVisibility(View.VISIBLE);
                for (int j = mTopicPathRect.getChildCount() > 0 ? mTopicPathRect.getChildCount() - 1 : 0; j < item.mNest; j++) {
                    LinearLayout indentSpaceView = (LinearLayout) getLayoutInflater().inflate(R.layout.pipe, null);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            (int) indentWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                    indentSpaceView.setLayoutParams(params);
                    item.mIndentSpace.addView(indentSpaceView);
                }
            }
            if (i < topicCount - 1 && item.mNest < ((TopicItemView) mTopicsRect.getChildAt(i + 1)).mNest) {
                item.mBtnExpand.setVisibility(View.VISIBLE);
            } else {
                item.mBtnExpand.setVisibility(View.INVISIBLE);
            }
        }

        // フォーカス対象があれば更新
        if (editTopic != null) {
            TopicItemView item = (TopicItemView) mTopicsRect.getChildAt(editTopic.mTopicRecord);
            mCurrentTopic = item;
            if (item.mNest > 0) {
                mTopicUnnest.setTextColor(getResources().getColor(android.R.color.white));
                mTopicUnnest.setEnabled(true);
            }
            if (item.mTopicRecord > 0 && item.mNest <= ((TopicItemView) mTopicsRect.getChildAt(item.mTopicRecord - 1)).mNest) {
                mTopicNest.setTextColor(getResources().getColor(android.R.color.white));
                mTopicNest.setEnabled(true);
            }
            item.mTopicLabel.setVisibility(View.GONE);
            item.mTopicEdit.setVisibility(View.VISIBLE);
            item.mTopicEdit.requestFocus();
        }
    }

    public class TopicItemView extends LinearLayout implements MytreenoteDatabase.TopicInfoColumns {
        int mTopicRecord;
        int mNest;
        public LinearLayout mIndentSpace;
        public ImageView mBtnCurrent;
        public TextView mTopicLabel;
        public EditText mTopicEdit;
        public TextView mBtnExpand;
        String mOpenFlg;
        String mCurrentTopicFlg;

        public TopicItemView(final ContentValues values) {
            super(getApplicationContext());
            final View view = LayoutInflater.from(mContext).inflate(R.layout.topic_item, this);
            mIndentSpace = (LinearLayout) view.findViewById(R.id.indent_space);
            mBtnCurrent = (ImageView) view.findViewById(R.id.btn_current);
            mTopicLabel = (TextView) view.findViewById(R.id.topic_label);
            mTopicEdit = (EditText) view.findViewById(R.id.topic_edit);
            mBtnExpand = (TextView) view.findViewById(R.id.btn_expand);

            mTopicRecord = values.getAsInteger(RECORD);
            mNest = values.getAsInteger(NEST);
            mTopicLabel.setText(values.getAsString(TOPIC_TEXT));
            mTopicEdit.setText(values.getAsString(TOPIC_TEXT));
            mOpenFlg = values.getAsString(OPEN_FLG);
            mCurrentTopicFlg = values.getAsString(CURRENT_TOPIC_FLG);
            initTopicEdit();
        }

        private void initTopicEdit() {
            mTopicLabel.setOnTouchListener((v, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mTopicLabel.setBackgroundColor(getResources().getColor(R.color.select_bg_gray));
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    mTopicLabel.setBackgroundColor(getResources().getColor(R.color.transparent));
                }
                return false;
            });
            mTopicLabel.setOnClickListener(v -> postUpdateTopics(this));
            mTopicEdit.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && mTopicEdit.getVisibility() != View.VISIBLE) {
                    previusUpdateTopics();
                    mTopicDao.bulkInsert(mValuesList);
                    postUpdateTopics(this);
                }
            });
            mTopicEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // ignore
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mTopicLabel.setText(mTopicEdit.getText());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // ignore
                }
            });
            mBtnCurrent.setOnClickListener(v -> {
                onChangeCurrent(mTopicRecord);
                previusUpdateTopics();
                mTopicDao.bulkInsert(mValuesList);
                postUpdateTopics(null);
            });
            mTopicEdit.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && TextUtils.isEmpty(mTopicEdit.getText())) {
                        TopicItemView nextItem = (TopicItemView) mTopicsRect.getChildAt(mTopicRecord + 1);
                        if (nextItem != null && nextItem.mNest > mNest) {
                            return false;
                        } else {
                            deleteTopic();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        final String inputTopicText = mTopicEdit.getText().toString();
                        mTopicEdit.setText(inputTopicText.replaceAll("\n", ""));

                        final int newTopicRecord = mTopicRecord + 1;
                        ContentValues values = new ContentValues();
                        values.put(RECORD, newTopicRecord);
                        values.put(NEST, mTopicRecord > 0 ? mValuesList.get(mTopicRecord).getAsInteger(NEST) : 0);
                        values.put(TOPIC_TEXT, getResources().getString(R.string.empty_param));
                        values.put(OPEN_FLG, getResources().getString(R.string.flag_on));
                        values.put(CURRENT_TOPIC_FLG, getResources().getString(R.string.flag_off));
                        mValuesList.add(newTopicRecord, values);

                        TopicItemView item = new TopicItemView(values);
                        mTopicsRect.addView(item, newTopicRecord);

                        previusUpdateTopics();
                        mTopicDao.bulkInsert(mValuesList);
                        postUpdateTopics(item);
                        return true;
                    }
                }
                return false;
            });
            mTopicLabel.setOnLongClickListener(v -> {
                TopicItemView nextItem = (TopicItemView) mTopicsRect.getChildAt(mTopicRecord + 1);
                if (nextItem == null || nextItem.mNest > mNest) {
                    return false;
                } else {
                    new AlertDialog.Builder(TopicMainActivity.this)
                            .setMessage(getResources().getString(R.string.delete_message))
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                deleteTopic();
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                    return false;
                }
            });
            mBtnExpand.setOnClickListener(v -> {
                mOpenFlg = getResources().getString(R.string.flag_on).equals(mOpenFlg) ?
                        getResources().getString(R.string.flag_off)
                        : getResources().getString(R.string.flag_on);
                onChangeExpandTopic(mOpenFlg);
                previusUpdateTopics();
                mTopicDao.bulkInsert(mValuesList);
                postUpdateTopics(null);
            });
        }

        /**
         * カレントトピックを変更.
         *
         * @param topicRecord
         */
        private void onChangeCurrent(final int topicRecord) {
            mCurrentTopic = this;
            mTopicPathRect.removeAllViews();
            int previousTopicNest = 0;
            boolean hasNext = true;
            final float currentTopicTextSize = getResources().getDimension(R.dimen.current_topic_text_size);
            final float topicTextSize = getResources().getDimension(R.dimen.topic_text_size);
            final List<TopicItemView> topicList = new ArrayList<>();

            for (int i = 0; i < mTopicsRect.getChildCount(); i++) {
                TopicItemView item = ((TopicItemView) mTopicsRect.getChildAt(i));
                item.mBtnCurrent.setVisibility(View.VISIBLE);
                item.mTopicLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, topicTextSize);
                item.mTopicEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, topicTextSize);

                if (getResources().getInteger(R.integer.unspecified) == topicRecord) {
                    item.setVisibility(View.VISIBLE);
                    item.mBtnCurrent.setVisibility(View.VISIBLE);

                } else {
                    if (i < topicRecord) {
                        item.setVisibility(View.GONE);

                    } else if (hasNext) {
                        if (i == topicRecord) {
                            previousTopicNest = 0;
                            item.mBtnCurrent.setVisibility(View.GONE);
                            item.mTopicEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTopicTextSize);
                            item.mTopicLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTopicTextSize);
                        }
                        if ((i == topicRecord
                                || (i - 1 == topicRecord && previousTopicNest < item.mNest))
                                || (i - 1 != topicRecord && previousTopicNest <= item.mNest)) {
                            topicList.add(this);
                        } else {
                            hasNext = false;
                        }
                        item.setVisibility(hasNext ? View.VISIBLE : View.GONE);
                    } else {
                        item.setVisibility(View.GONE);
                    }
                }
                previousTopicNest = item.mNest;
            }
            previousTopicNest = getResources().getInteger(R.integer.unspecified);
            for (int i = topicRecord; i >= 0; i--) {
                TopicItemView item = ((TopicItemView) mTopicsRect.getChildAt(i));
                if (item.mNest == 0) {
                    break;
                }
                if (previousTopicNest != getResources().getInteger(R.integer.unspecified) && previousTopicNest != item.mNest) {
                    TextView pathUnit = new TextView(TopicMainActivity.this);
                    pathUnit.setText(item.mTopicEdit.getText() + getResources().getString(R.string.path_divider));
                    final int referenceTopicRecord = i;
                    pathUnit.setOnClickListener(v -> {
                        onChangeCurrent(referenceTopicRecord);
                        previusUpdateTopics();
                        mTopicDao.bulkInsert(mValuesList);
                        postUpdateTopics(null);
                    });
                    mTopicPathRect.addView(pathUnit, 0);
                }
                previousTopicNest = item.mNest;
            }
            TextView pathUnit = new TextView(TopicMainActivity.this);
            pathUnit.setText(getResources().getString(R.string.home) + getResources().getString(R.string.path_divider));
            pathUnit.setOnClickListener(v -> {
                onChangeCurrent(getResources().getInteger(R.integer.unspecified));
                mTopicPathRect.removeAllViews();
                previusUpdateTopics();
                mTopicDao.bulkInsert(mValuesList);
                postUpdateTopics(null);
            });
            mTopicPathRect.addView(pathUnit, 0);
        }

        /**
         * トピックエクスパンドを変更.
         *
         * @param flg
         */
        private void onChangeExpandTopic(final String flg) {
            final int currentNest = mNest;
            int currentRecord = mTopicRecord;
            int nextRecordNest = mNest;

            while (nextRecordNest >= currentNest && mTopicsRect.getChildCount() > currentRecord) {
                TopicItemView item = ((TopicItemView) mTopicsRect.getChildAt(currentRecord));
                item.mOpenFlg = flg;
                if (getResources().getString(R.string.flag_on).equals(flg)) {
                    if (currentRecord == mTopicRecord) {
                        item.mBtnExpand.setText(getResources().getString(R.string.plus));
                    } else {
                        item.setVisibility(View.GONE);
                    }
                } else {
                    item.mBtnExpand.setText(getResources().getString(R.string.minus));
                    if (currentRecord != mTopicRecord) {
                        item.mBtnExpand.setText(getResources().getString(R.string.minus));
                        item.setVisibility(View.VISIBLE);
                    }
                }
                if (mTopicsRect.getChildAt(currentRecord + 1) != null) {
                    final int tmpNextRecordNest = ((TopicItemView) mTopicsRect.getChildAt(currentRecord + 1)).mNest;
                    if (tmpNextRecordNest <= currentNest) {
                        break;
                    } else {
                        nextRecordNest = tmpNextRecordNest;
                        currentRecord++;
                    }
                } else {
                    break;
                }
            }
        }

        /**
         * トピックを削除.
         */
        private void deleteTopic() {
            mCurrentTopic = null;
            mValuesList.remove(mTopicRecord);
            mTopicsRect.removeViewAt(mTopicRecord);
            previusUpdateTopics();
            mTopicDao.bulkInsert(mValuesList);
            postUpdateTopics(null);
        }
    }
}
