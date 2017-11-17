package com.example.srujana.security;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity1 extends AppCompatActivity {
    private static final int REQUEST_CODE_CHEAT = 0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
     private Button mButton;
    private Button mButton4;
  // DatabaseHelper db1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
       mButton= (Button) findViewById(R.id.button);
        mButton4=(Button) findViewById(R.id.button4);

        //addData();

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_menu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem){
                switch (menuItem.getItemId()){
                    case(R.id.nav_security):
                        Intent accountActivity = new Intent(MainActivity1.this, MainActivity.class);
                        startActivity(accountActivity);
                }
                return true;
            }
        });
        mButton.setOnClickListener(    new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = new Intent(MainActivity1.this,MainActivity.class);
                startActivity(i);
                //startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });

        mButton4.setOnClickListener(    new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = new Intent(MainActivity1.this,MainActivity.class);
                startActivity(i);
                //startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });


    }



    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
    if (mToggle.onOptionsItemSelected(item)){
        return true;}
return  super.onOptionsItemSelected(item);
    }

}
