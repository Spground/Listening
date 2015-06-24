package cn.edu.dlut.listening.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import cn.edu.dlut.listening.R;
import cn.edu.dlut.listening.global.Global;
import cn.edu.dlut.listening.network.CheckNetworkState;

/*一个imageButon对应一个tag 即文件的名字（不含格式后缀）*/
public class ChooseActivity extends ActionBarActivity implements  View.OnClickListener{

    TextView titleTextView ;
    String title;
    String[] titles = new String[]{"2014年6月听力试卷","2014年12月听力试卷","2015年6月听力试卷","2015年12月听力试卷"};
    String[] fileNamePrefix = new String[]{"201406cet4","201412cet4","201506cet4","201512cet4"};
    String URLDir = Global.URLDIR;//服务的上的目录
    ListView mListView;
    ListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        initView();
        title = getIntent().getStringExtra("CET_CLASS");
        if(title != null){
            titleTextView.setText(title);
        }
    }



    private void initView(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_choose_activity_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseActivity.this.onBackPressed();

            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_action_back);

        titleTextView = (TextView)findViewById(R.id.id_choose_activity_title_textview);
        mListView = (ListView)findViewById(R.id.id_choose_activity_listview);
        mListViewAdapter = new ListViewAdapter();
        mListView.setAdapter(new ListViewAdapter());

    }

    /*检测是否这套试卷是否已经下载完成*/
    private boolean checkValid(String fileName){

        File fileMP3 = new File(Global.USER_DIR + "/" + fileName + ".mp3");
        File fileJson = new File(Global.USER_DIR + "/" + fileName + ".json");

        if(fileJson.exists() && fileMP3.exists())
            return true;
        else
        return false;
    }
    /*给定url得出最后的文件名*/
    private String getFileNameFomURL(String url) {

        String result = null;
        result = url.substring(url.lastIndexOf("/"));
        Log.v("TAG getFileName",result);
        return result;

    }

    @Override
    public void onClick(View view) {
        String fileNamePrefix = view.getTag().toString();
        Intent intent = new Intent(ChooseActivity.this,ListeningActivity.class);
        intent.putExtra("PLAY_FILE_NAME_PREFIX",fileNamePrefix);
        intent.putExtra("CET_CLASS",title);
        startActivity(intent);
    }


    /*下面是适配器*/
    class ListViewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Object getItem(int i) {
            return titles[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            Holder holder = null;
            if(convertview == null){
                holder = new Holder();
                convertview = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listview_simple_text_item,null);
                holder.tv = (TextView)convertview.findViewById(R.id.id_listview_simple_text_item_textView);
                holder.imageButton =(ImageButton)convertview.findViewById(R.id.id_choose_activity_download_imagebutton);
                convertview.setTag(holder);
            }
            else{
                holder = (Holder)convertview.getTag();
            }
            holder.tv.setText(titles[i]);
            holder.imageButton.setTag(fileNamePrefix[i]);
            /*如果已经存在文件所以更改imgbtn*/
            if(checkValid(holder.imageButton.getTag().toString())){
                Log.v("TAG validFileName",fileNamePrefix[i]);
                holder.imageButton.setBackgroundResource(R.drawable.btn_go_to_listening_defalut);
                holder.imageButton.setImageResource(R.drawable.btn_listening);
                holder.imageButton.setOnClickListener(ChooseActivity.this);
            }
            else{
                holder.imageButton.setImageResource(R.drawable.ic_action_download);
                holder.imageButton.setBackgroundResource(R.drawable.btn_download_default);
                holder.imageButton.setOnClickListener(new DownLoadListener());
            }

            return convertview;
        }
    }


    class Holder{
        public TextView tv;
        public ImageButton imageButton;
    }

    /*下面是监听下载按钮的类*/
    class DownLoadListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            String jsonURL = URLDir + "/" +view.getTag().toString()+".json";
            Toast.makeText(ChooseActivity.this,jsonURL,Toast.LENGTH_LONG).show();
            //开始json的下载任务
            new DownLoadJsonTask().execute(jsonURL);
        }
    }


    /*下面是异步下载mp3文件到目录下面*/
    class DownLoadTask extends AsyncTask<String,Integer,Boolean>{

        OkHttpClient mClient;
        InputStream in;
        BufferedInputStream bufferIn;
        OutputStream out;
        BufferedOutputStream bufferOut;

        ProgressDialog mPDialog;

        public DownLoadTask(){
            mPDialog = new ProgressDialog(ChooseActivity.this);
            mPDialog.setMax(100);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mPDialog.setMessage("正在下载请稍候......");
            mPDialog.setIndeterminate(false);
            mPDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... strings) {

            String mp3URL = strings[0];

            mClient = new OkHttpClient();
            mClient.setConnectTimeout(5, TimeUnit.SECONDS);
            mClient.setReadTimeout(60,TimeUnit.SECONDS);

            Request req = new Request.Builder().url(mp3URL).build();

            try {
                Response resp = mClient.newCall(req).execute();
                int flength = Integer.valueOf(resp.header("Content-Length"));

                Log.v("TAG--filength",flength+"bytes");
                in = resp.body().byteStream();
                bufferIn = new BufferedInputStream(in);

                File file = new File(Global.USER_DIR+"/"+getFileNameFomURL(mp3URL));
                out = new FileOutputStream(file);
                bufferOut = new BufferedOutputStream(out);

                byte[] buffer = new byte[1024];
                int len = 0;
                int sum=0;

                while((len = bufferIn.read(buffer)) != -1){
                    bufferOut.write(buffer,0,len);
                    sum += len;
                    Log.v("TAG--read",sum+"bytes");
                    onProgressUpdate((sum * 100/ flength));
                    Log.v("TAG---progress",(sum * 100/ flength)+"%");
                }
                bufferOut.flush();


            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            finally{
                if(bufferOut != null){
                    try {
                        bufferOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(bufferIn != null){
                    try {
                        bufferIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0] >= 100){
                mPDialog.dismiss();

                return;
            }
            mPDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if(bool){
                if(mPDialog.isShowing())
                        mPDialog.dismiss();
                Toast.makeText(ChooseActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
                //刷新listview
                mListViewAdapter.notifyDataSetChanged();
                mListView.setAdapter(mListViewAdapter);
            }
            else{
                if(mPDialog.isShowing())
                          mPDialog.dismiss();
                /*检查是否网络断开了*/
                if(!CheckNetworkState.isConn(ChooseActivity.this)){
                    CheckNetworkState.setNetworkMethod(ChooseActivity.this);
                    return;
                }
                else{
                    Toast.makeText(ChooseActivity.this,"下载失败!请重试！",Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    /*下面是下载json文件的*/

    class DownLoadJsonTask extends AsyncTask<String,Integer,Boolean>{

        OkHttpClient mClient;
        InputStream in;
        BufferedInputStream bufferIn;
        OutputStream out;
        BufferedOutputStream bufferOut;

        String jsonURL;

        @Override
        protected Boolean doInBackground(String... strings) {
            jsonURL = strings[0];

            mClient = new OkHttpClient();
            mClient.setConnectTimeout(5, TimeUnit.SECONDS);
            mClient.setReadTimeout(60,TimeUnit.SECONDS);

            Request req = new Request.Builder().url(jsonURL).build();

            try {
                Response resp = mClient.newCall(req).execute();
                int flength = Integer.valueOf(resp.header("Content-Length"));

                Log.v("TAG--filength",flength+"bytes");
                in = resp.body().byteStream();
                bufferIn = new BufferedInputStream(in);

                File file = new File(Global.USER_DIR+"/"+getFileNameFomURL(jsonURL));
                out = new FileOutputStream(file);
                bufferOut = new BufferedOutputStream(out);

                byte[] buffer = new byte[512];
                int len = 0;
                int sum=0;

                while((len = bufferIn.read(buffer)) != -1){
                    bufferOut.write(buffer,0,len);
                    sum += len;
                    Log.v("TAG--read", sum + "bytes");
                    Log.v("TAG---progress",(sum * 100/ flength)+"%");
                }
                bufferOut.flush();


            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            finally{
                if(bufferOut != null){
                    try {
                        bufferOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(bufferIn != null){
                    try {
                        bufferIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if(bool){
                Toast.makeText(ChooseActivity.this,"下载Json文件完成！",Toast.LENGTH_SHORT).show();
                new DownLoadTask().execute(jsonURL.replace(".json",".mp3"));
            }
            else{
                /*检查是否网络断开了*/
                if(!CheckNetworkState.isConn(ChooseActivity.this)){
                    CheckNetworkState.setNetworkMethod(ChooseActivity.this);
                    return;
                }
                else{
                    Toast.makeText(ChooseActivity.this,"下载失败!请重试！",Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}
