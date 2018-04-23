package myapplication.t.example.com.weixin;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by zsl on 2018/4/22.
 */

public class CyFragment extends Fragment {
    private EditText tinput;
    private View view;
    private Button send;
    private boolean isVisible = true;
    private LinearLayout layout1;
    private TextView tresponse;
    private TextView tresponse1;
    private TextView tresponse2;
    private String baseurl="http://v.juhe.cn/chengyu/query";
    private String basekey="0f9a433ac8b0c35009db0e6c362d4d4d";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view= inflater.inflate(R.layout.fragment_cy, container, false);
        this.view= inflater.inflate(R.layout.fragment_cy, container, false);
        send = (Button) view.findViewById(R.id.send_request);
        tinput = (EditText) view.findViewById(R.id.input1);
        layout1=(LinearLayout)view.findViewById(R.id.layout);
        layout1.setVisibility(View.GONE);
        tresponse = (TextView) view.findViewById(R.id.response);
        tresponse1=(TextView)view.findViewById(R.id.response_1);
        tresponse2=(TextView)view.findViewById(R.id.response_2);
        send.setOnClickListener(new searchListener());
        return  view;
    }
    private class searchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isVisible) {
                isVisible = false;
                layout1.setVisibility(View.VISIBLE);//这一句显示布局LinearLayout区域
            } else {
                layout1.setVisibility(View.GONE);//这一句即隐藏布局LinearLayout区域
                isVisible = true;
            }
            String content = tinput.getText().toString().trim();
            if (content == null || "".equals(content)) {
                Toast.makeText(getActivity().getApplicationContext(), "请输入要翻译的内容", Toast.LENGTH_SHORT).show();
                return;
            }
            final String YouDaoUrl =baseurl+"?key="+basekey+"&word="+content;
            new Thread() {
                public void run() {
                    try {
                        reloadData(YouDaoUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
            }.start();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void reloadData(final String url){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "utf-8"));
                    String line=null;
                    StringBuffer content=new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    reader.close();
                    return content.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s!=null){
                    try {
                        JSONObject jobj=new JSONObject(s);

                        JSONObject jObj1 = jobj.getJSONObject("result");
                        tresponse.setText(String.format("%s",jObj1.getString("chengyujs")));
                        tresponse1.setText(String.format("%s",jObj1.getString("from_")));
                        tresponse2.setText(String.format("%s",jObj1.getString("example")));
                        try {

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
