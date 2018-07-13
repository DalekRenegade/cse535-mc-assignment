package com.cse535.assignments.testapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onRun(View v) {
        Button btnRun = (Button)findViewById(R.id.buttonRun);
        Button btnStop = (Button)findViewById(R.id.buttonStop);
        btnRun.setEnabled(false);
        btnStop.setEnabled(true);
        //TODO: Change contents below as required
        Toast.makeText(this, "Running...", Toast.LENGTH_SHORT).show();
    }

    public void onStop(View v) {
        Button btnRun = (Button)findViewById(R.id.buttonRun);
        Button btnStop = (Button)findViewById(R.id.buttonStop);
        btnStop.setEnabled(false);
        btnRun.setEnabled(true);
        //TODO: Change contents below as required
        Toast.makeText(this, "Stopped...!!!", Toast.LENGTH_SHORT).show();
    }
}
