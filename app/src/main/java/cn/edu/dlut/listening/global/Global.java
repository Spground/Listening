package cn.edu.dlut.listening.global;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by asus on 2015/6/19.
 */
public class Global extends Application {
   public static String  USER_DIR;
   public static String UNIQUE_NAME="cn.edu.dlut.listening/files";
   public static String CURRENT_PLAY_FILENAME = "";
   public static final  String URLDIR = "http://192.168.0.105/listening/cet4";
   public static enum PLAY_SATTE{
       PLAYING,
       STOP,
       PAUSE,
   };

    public static PLAY_SATTE CURRNET_PLAY_STATE = PLAY_SATTE.STOP;
    @Override
    public void onCreate() {
        initDir();
        super.onCreate();
    }

    /*初始化存放下载文件的目录*/
    private void initDir(){
//        外部存储可用
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String externalRooot = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(externalRooot+"/"+UNIQUE_NAME);
            if(!file.exists()){
                file.mkdirs();
            }
            USER_DIR = file.getAbsolutePath();
            Log.v("TAG","外部存储的状态"+Environment.getExternalStorageState() );
            Log.v("TAG","存储目录为外部存储"+USER_DIR );
            return;
        }
        //外部存储不可用
        else{
            File innerRoot = getFilesDir();
            Log.v("TAG","存储目录为内部存储目录"+innerRoot.getAbsolutePath() );
            if(!innerRoot.exists()){
                innerRoot.mkdirs();
            }
            USER_DIR = innerRoot.getAbsolutePath();
            return;
        }

    }


}
