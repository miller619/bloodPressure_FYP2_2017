package com.example.virus.bloodpressure;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

public class Instruction extends Activity {


    ImageButton startReading;
    ScrollView userInstruction;

    double h, w, ag;
    double Q, car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        startReading = findViewById(R.id.read_bp);
        userInstruction = findViewById(R.id.user_instructions);


        final String height = getIntent().getExtras().getString("user_height");
        h = Double.parseDouble(height);
        final String weight = getIntent().getExtras().getString("user_weight");
        w = Double.parseDouble(weight);
        final String age = getIntent().getExtras().getString("user_age");
        ag = Double.parseDouble(age);
        car = getIntent().getExtras().getDouble("user_cardiac_output");
        Q=car;
        Log.d("Data", "Height "+h+" Weight "+w+" Age "+ag+" Cardiac Output "+Q);

        //On click start camera activity
        startReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToCamera = new Intent(Instruction.this, BloodPressureRecorder.class);
                Bundle data = new Bundle();
                data.putString("user_height", height);
                data.putString("user_weight", weight);
                data.putString("user_age", age);
                data.putDouble("user_cardiac_output", car);
                if (!height.equals("") && !weight.equals("") && !age.equals("")){
                    goToCamera.putExtras(data);
                    startActivity(goToCamera);
                }else {
                    Toast.makeText(Instruction.this, "Fill in all the details", Toast.LENGTH_SHORT).show();
                }

                startActivity(goToCamera);
            }
        });
    }
}
