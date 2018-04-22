package myapplication.t.example.com.weixin;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import myapplication.t.example.com.weixin.beans.AssoBean;
import myapplication.t.example.com.weixin.beans.Content;
import myapplication.t.example.com.weixin.beans.TranslateBean;
import myapplication.t.example.com.weixin.utils.OkHttpUtils;


/**
 * Created by zsl on 2018/4/22.
 */

public class JzActivity extends  AppCompatActivity implements TextWatcher, View.OnClickListener {
    private static final String TAG = "MainActivity";
    //    联想词汇url
    private static String ASSOCIATE_URL = "http://dict-mobile.iciba.com/interface/index.php?c=word&m=getsuggest&nums=10&client=6&is_need_mean=1&word=";
    //    翻译单词或句子url
    private static String TRANSLATE_URL = "http://fy.iciba.com/ajax.php?a=fy&f=auto&t=auto&w=";

    private EditText etInput;

    private LinearLayout mBotToolBar;
    private Button btnClean, btnTranslate;

    private RelativeLayout mTranView;
    private TextView tvPhEn, tvPhAm;
    private ImageView ivPhEnMp3, ivPhAmMp3;
    private TextView tvMeans;

    //    展示联想词汇列表
    private ListView mListView;

    private OkHttpUtils mOkHttpUtils = OkHttpUtils.getInstance();

    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    //    单词和短语发音url
    private String phEnMp3Url, phAmMp3Url, phTtsMp3Url;

    private List<myapplication.t.example.com.weixin.beans.Message> mMessageList;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //                设置联想词汇列表数据
                case 0:
                    AssoBean assoBean = (AssoBean) msg.obj;
                    mMessageList = assoBean.getMessage();
                    WordListAdapter adapter = new WordListAdapter(JzActivity.this,mMessageList);
                    mTranView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    mListView.setAdapter(adapter);
                    break;
                //                    翻译
                case 1:
                    TranslateBean translateBean = (TranslateBean) msg.obj;
                    Content content = translateBean.getContent();
                    int status = translateBean.getStatus();
                    if (status == 0){       //翻译单词
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String s : content.getWord_mean()) {
                            stringBuilder.append(s).append("\n");
                        }
                        phEnMp3Url = content.getPh_en_mp3();
                        phAmMp3Url = content.getPh_am_mp3();
                        phTtsMp3Url = content.getPh_tts_mp3();
                        tvPhEn.setText("英 [" + content.getPh_en() + "]");
                        tvPhAm.setText("美 [" + content.getPh_am() + "]");
                        tvMeans.setText(stringBuilder.toString());

                        loadPhMp3();
                    }else if (status == 1){         //翻译句子
                        tvMeans.setText(translateBean.getContent().getOut());
                    }
                    mListView.setVisibility(View.GONE);
                    mTranView.setVisibility(View.VISIBLE);
                    break;
                //                    加载英式发音
                case 2:
                    try {
                        FileInputStream enIS = openFileInput(ph_en_file_name);
                        enSoundId = mSoundPool.load(enIS.getFD(),0,enIS.available(),1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                //                    加载美式发音
                case 3:
                    try {
                        FileInputStream amIS = openFileInput(ph_am_file_name);
                        amSoundId = mSoundPool.load(amIS.getFD(),0,amIS.available(),1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jz);

        initView();
        initEvent();
    }

    private void initView() {
        etInput = (EditText) findViewById(R.id.et_enter);

        mBotToolBar = (LinearLayout)findViewById(R.id.bot_tool_bar);
        btnClean =(Button) findViewById(R.id.btn_clean);
        btnTranslate = (Button) findViewById(R.id.btn_translate);

        mTranView = (RelativeLayout)findViewById(R.id.rl_tran);
        tvMeans = (TextView) findViewById(R.id.tv_means);
        tvPhEn = (TextView) findViewById(R.id.tv_ph_en);
        tvPhAm = (TextView) findViewById(R.id.tv_ph_am);
        ivPhEnMp3 =(ImageView) findViewById(R.id.iv_ph_en_mp3);
        ivPhAmMp3 = (ImageView)findViewById(R.id.iv_ph_am_mp3);

        mListView =(ListView) findViewById(R.id.lv_words);

        mBotToolBar.setVisibility(View.GONE);
    }

    private void initEvent(){
        etInput.addTextChangedListener(this);

        btnClean.setOnClickListener(this);
        btnTranslate.setOnClickListener(this);

        ivPhEnMp3.setOnClickListener(this);
        ivPhAmMp3.setOnClickListener(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                etInput.removeTextChangedListener(JzActivity.this);
                myapplication.t.example.com.weixin.beans.Message message = mMessageList.get(i);
                etInput.setText(message.getKey());
                etInput.setSelection(message.getKey().length());    //光标移到最后
                translate();
                etInput.addTextChangedListener(JzActivity.this);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    //    输入框中文本变化，加载联想词汇
    @Override
    public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
        if (TextUtils.isEmpty(charSequence))    return;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StringBuilder builder = new StringBuilder(ASSOCIATE_URL);
                String url = builder.append(charSequence).toString().split(" ")[0];
                mOkHttpUtils.get(url).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String json = response.body().string();
                        if (response.code() == 200 && json.split(":")[1].contains("1")){
                            try {
                                Gson gson = new Gson();
                                AssoBean assoBean = gson.fromJson(json, AssoBean.class);
                                Message msg = new Message();
                                msg.obj = assoBean;
                                msg.what = 0;
                                mHandler.sendMessage(msg);
                            }catch (JsonSyntaxException e){
                                e.printStackTrace();
                            }

                        }
                    }
                });

            }
        };

        mThreadPool.execute(runnable);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(etInput.getText().toString().trim()))   mBotToolBar.setVisibility(View.GONE);
        else    mBotToolBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_clean:
                etInput.setText("");
                mTranView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                break;
            case R.id.btn_translate:
                translate();
                break;
            case R.id.iv_ph_en_mp3:
                if (mSoundPool != null)
                    mSoundPool.play(enSoundId,1,1,1,0,1);
                break;
            case R.id.iv_ph_am_mp3:
                if (mSoundPool != null)
                    mSoundPool.play(amSoundId,1,1,1,0,1);
                break;
        }
    }

    private final static String ph_en_file_name = "ph_en.mp3";
    private final static String ph_am_file_name = "ph_am.mp3";
    private int enSoundId = 1;
    private int amSoundId = 2;
    private SoundPool mSoundPool;

    private void loadPhMp3(){
        if (TextUtils.isEmpty(phEnMp3Url) || TextUtils.isEmpty(phAmMp3Url)){
            if (TextUtils.isEmpty(phTtsMp3Url))     return;
            else{
                phEnMp3Url = phTtsMp3Url;
                phAmMp3Url = phTtsMp3Url;
            }
        }

        mSoundPool = new SoundPool(2,AudioManager.STREAM_MUSIC,5);
        //        加载音频并保存到本地
        mOkHttpUtils.get(phEnMp3Url).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200){
                    byte[] data = response.body().bytes();
                    FileOutputStream os = openFileOutput(ph_en_file_name,MODE_PRIVATE);
                    BufferedOutputStream bufferOs = new BufferedOutputStream(os);
                    bufferOs.write(data);

                    bufferOs.close();
                    mHandler.sendEmptyMessage(2);
                }
            }
        });
        mOkHttpUtils.get(phAmMp3Url).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.code() == 200){
                    byte[] data = response.body().bytes();
                    FileOutputStream os = openFileOutput(ph_am_file_name,MODE_PRIVATE);
                    BufferedOutputStream bufferOs = new BufferedOutputStream(os);
                    bufferOs.write(data);

                    bufferOs.close();
                    mHandler.sendEmptyMessage(3);
                }
            }
        });
    }

    //    加载翻译结果，显示翻译详情页
    private void translate() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StringBuilder builder = new StringBuilder(TRANSLATE_URL);
                String url = builder.append(etInput.getText().toString().trim()).toString();
                mOkHttpUtils.get(url).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String json = response.body().string();
                        if (response.code() == 200){
                            Gson gson = new Gson();
                            TranslateBean translateBean = gson.fromJson(json,TranslateBean.class);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = translateBean;
                            mHandler.sendMessage(msg);
                        }
                    }
                });
            }
        };
        mThreadPool.execute(runnable);
    }

}

