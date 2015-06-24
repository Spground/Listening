package cn.edu.dlut.listening.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import cn.edu.dlut.listening.R;
import cn.edu.dlut.listening.adapter.RecyclerViewAdapter;
import cn.edu.dlut.listening.event.PauseEvent;
import cn.edu.dlut.listening.event.PlayEvent;
import cn.edu.dlut.listening.global.Global;
import cn.edu.dlut.listening.service.AudioPlayService;
import de.greenrobot.event.EventBus;

public class ListeningActivity extends ActionBarActivity implements View.OnClickListener{

    private long audioDuration;//单位毫秒
    private long currentPostion;//单位毫秒

    private final int REFRESH = 1;
    private String title;
    private TextView titleTextView;

    private SeekBar seekBar;
    private TextView palyingtimeTV,endtimeTV;
    private MyReiver receiver;

    private RecyclerView recyclerView;
    private  RecyclerViewAdapter recyclerViewAdapter;

    private Button playBtn;

    private String fileNamePrefix;

    private AudioPlayServiceInterface mPlayControler;
    /*更新视图播放时间的handler*/
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REFRESH:
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public interface AudioPlayServiceInterface{
        public void seekTo(int sec);
        public int getAudioDuration();
        public int getCurrentPosition();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);
        title = getIntent().getStringExtra("CET_CLASS");
        fileNamePrefix = getIntent().getStringExtra("PLAY_FILE_NAME_PREFIX");
        startSerice();
        initView();

    }


    /*初始化视图*/
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_listening_activity_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayService();
                ListeningActivity.this.onBackPressed();

            }
        });

        titleTextView = (TextView)toolbar.findViewById(R.id.id_listening_activity_title_textview);
        titleTextView.setText(title);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView)findViewById(R.id.id_listening_activity_recycle_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        playBtn = (Button)findViewById(R.id.id_listening_activity_btn_play);
        playBtn.setOnClickListener(this);

        palyingtimeTV = (TextView)findViewById(R.id.id_listening_activity_playing_time_textview);
        endtimeTV = (TextView)findViewById(R.id.id_listening_activity_end_time_textview);
        seekBar = (SeekBar)findViewById(R.id.id_listening_activity_seekbar);
        //seekBar的注册延迟到服务绑定完成后
        receiver = new MyReiver();
        ListeningActivity.this.registerReceiver(receiver,new IntentFilter());

    }


/*开启服务*/
    private void startSerice(){

        final String audioRootPath = Global.USER_DIR;
        Intent intent = new Intent(ListeningActivity.this, AudioPlayService.class);
        intent.putExtra("AUDIO_PATH", audioRootPath);
        intent.putExtra("FILE_NAME_PREFIX", fileNamePrefix);

        ServiceConnection con = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.v("TAG","Service connected!");
                mPlayControler = (AudioPlayServiceInterface)iBinder;
                if(mPlayControler != null){

                    audioDuration = mPlayControler.getAudioDuration();//获得即将播放的音频的总长度
                    Log.v("TAG","File duration is " + audioDuration / 1000 + "second!");

                    endtimeTV.setText(getTimeStringByMsec(audioDuration));
                    //seekBar的事件监听延迟到这里，为了使得mediaPlayer可能因为服务连不上而为null
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            Log.v("TAG ","onProgressChanged is invoked "+i);
                            updatePlayTimeProgress(audioDuration * i / seekBar.getMax());
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            Log.v("TAG","progress is " + seekBar.getProgress());
                                mPlayControler.seekTo((int)(seekBar.getProgress() * audioDuration / seekBar.getMax()) );
                        }
                    });

                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.v("TAG","Service disconnected!");
            }
        };
        this.bindService(intent,con, Context.BIND_AUTO_CREATE);
    }

    /*入下一次刷新的消息的队列*/
    private void queueNextRefresh(long delay) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);

    }
/*刷新显示当前的播放时间*/
    private long refreshNow(){
        if(mPlayControler == null)
            return 500;
        else{
            long pos = mPlayControler.getCurrentPosition();
            long remaining = 1000 - pos % 1000;
            updatePlayTimeProgress(pos);
            seekBar.setProgress((int)(pos * seekBar.getMax()/ audioDuration));
            return remaining;
        }
    }
    /*更新播放进度的时间视图*/
    private void updatePlayTimeProgress(long msec){
        Log.v("TAG","seekBar progress is :" + msec * seekBar.getMax()/ audioDuration);
        String str = getTimeStringByMsec(msec);
        palyingtimeTV.setText(str);
    }

    /*根据毫秒数得到格式化的时间字符串*/
    private String getTimeStringByMsec(long msec){
        long minutes = msec / 1000 /60;
        long seconds = msec / 1000 % 60;
        String minStr = minutes >=10 ? (minutes + "") : ("0" + minutes);
        String secStr = seconds >= 10 ? (seconds + "") : ("0" + seconds);
        return minStr + ":" + secStr;
    }
    /*停止一个服务*/
    private void stopPlayService(){
        stopService(new Intent(ListeningActivity.this,AudioPlayService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listening, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Log.v("TAG", "btn pressed!");
        if(view.getId() == R.id.id_listening_activity_btn_play){
            Button btn = (Button)view;
            if(btn != null && btn.getText().equals("播放")){
                //开始播放
                Log.v("TAG", "btn play pressed!");
                btn.setText("暂停");
                //开始刷新时间
                long next = refreshNow();
                queueNextRefresh(next);
                EventBus.getDefault().post(new PlayEvent(fileNamePrefix));
            }
            else if(btn != null && btn.getText().equals("暂停")){
                //暂停播放
                Log.v("TAG", "btn pause pressed!");
                EventBus.getDefault().post(new PauseEvent(fileNamePrefix));
                mHandler.removeMessages(REFRESH);
                btn.setText("播放");
            }
            else if(btn == null)
            {
                Log.v("TAG", "btn is null");
                return;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //广播接受者
    class MyReiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int msec = intent.getIntExtra("PLAYING_TIME",0);
            updatePlayTimeProgress(msec);//更新进度
        }
    }
}
