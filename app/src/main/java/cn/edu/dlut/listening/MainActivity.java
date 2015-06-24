package cn.edu.dlut.listening;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import cn.edu.dlut.listening.activity.ChooseActivity;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    final String INTENT_TAG="CET_CLASS";

    ImageButton cet4IBtn,cet6IBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cet4IBtn = (ImageButton)findViewById(R.id.id_main_activity_imagebtn_CET_4);
        cet6IBtn = (ImageButton)findViewById(R.id.id_main_activity_imagebtn_CET_6);
        cet4IBtn.setOnClickListener(this);
        cet6IBtn.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_main_activity_toolbar);
        setSupportActionBar(toolbar);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        Intent intent = new Intent(MainActivity.this, ChooseActivity.class);

        switch (view.getId()){
            case R.id.id_main_activity_imagebtn_CET_4:
                intent.putExtra(INTENT_TAG,"选择CET-4听力试卷");
                startActivity(intent);
                break;
            case R.id.id_main_activity_imagebtn_CET_6:
                intent.putExtra(INTENT_TAG,"选择CET-6听力试卷");
                startActivity(intent);
                break;
        }
    }
}
