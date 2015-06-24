package cn.edu.dlut.listening.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicIntentReceiver extends BroadcastReceiver {
    public MusicIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        /*下面是当用户拔掉耳机时，应该，触发一个暂停的事件*/
         if(intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
            Log.v("TAG","user takes off headset!" );
         }
    }
}
