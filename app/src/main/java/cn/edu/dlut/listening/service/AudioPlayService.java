package cn.edu.dlut.listening.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import cn.edu.dlut.listening.activity.ListeningActivity;
import cn.edu.dlut.listening.event.PauseEvent;
import cn.edu.dlut.listening.event.PlayEvent;
import cn.edu.dlut.listening.event.ResumeEvent;
import cn.edu.dlut.listening.global.Global;
import de.greenrobot.event.EventBus;

/**
 * Created by asus on 2015/6/19.
 */
public class AudioPlayService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    private String audioRootPath;
    private final String AUDIO_PATH = "AUDIO_PATH";
    private final String FIlE_NAME_PREFIX = "FILE_NAME_PREFIX";
    private String audioPath;
    private String fileNamePrefix;
    private AudioServiceBinder aServiceBinder;
    public AudioPlayService(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        aServiceBinder = new AudioServiceBinder();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        audioRootPath = intent.getStringExtra(AUDIO_PATH);
        fileNamePrefix = intent.getStringExtra(FIlE_NAME_PREFIX);

        audioPath = audioRootPath + "/" + fileNamePrefix + ".mp3";
        Global.CURRENT_PLAY_FILENAME = audioPath;
        Log.v("TAG AUDIO_PATH",audioPath == null ? "null":audioPath);
        Log.v("TAG PlayService","Service is bound successfully!");
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(audioPath));
        return aServiceBinder;
    }

    @Override
    public void onDestroy() {
        if(mMediaPlayer != null){
            mMediaPlayer.release();//停止时要release
        }
        Log.v("TAG","PlayAudioService is destroyed！");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(PlayEvent event){
        Toast.makeText(this,event.getMsg(),Toast.LENGTH_SHORT).show();
        Log.v("TAG PlayEvent","Recv PlayEvent!");
         if(mMediaPlayer != null){
             mMediaPlayer.start();
             Log.v("TAG PlayEvent","mMdeiPlayer is palying " + Global.CURRENT_PLAY_FILENAME);
            }
    }

    public void onEventMainThread(PauseEvent event){
        Log.v("TAG PauseEvent","PauseEvent RECV!");
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            Log.v("TAG PauseEvent","mMediaPlayer.pause();!");
        };
    }

    public void onEventMainThread(ResumeEvent event){
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    //service提供给activity调用的接口
    class AudioServiceBinder extends Binder implements ListeningActivity.AudioPlayServiceInterface{

        @Override
        public void seekTo(int msec) {
            if(mMediaPlayer != null){
                Log.v("TAG","Recv seekTo invoke "+msec);
                mMediaPlayer.seekTo(msec);
            }
        }

        @Override
        public int getAudioDuration() {
            if(mMediaPlayer != null)
                return mMediaPlayer.getDuration();
            else
                return -1;
        }

        @Override
        public int getCurrentPosition() {
            if(mMediaPlayer !=null)
                return mMediaPlayer.getCurrentPosition();
            return -1;
        }
    }
}
