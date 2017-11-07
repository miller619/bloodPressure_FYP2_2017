package com.example.virus.bloodpressure;

import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import static java.security.AccessController.getContext;

public class Home extends Activity {

    EditText height, weight, age;
    RadioButton gender;
    RadioGroup userGender;
    Button nextPage;

    String m = "Male";
    double cardiac_output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0 & getIntent().getExtras() == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_home);

        height = findViewById(R.id.user_height);
        weight = findViewById(R.id.user_weight);
        age = findViewById(R.id.user_age);
        userGender = findViewById(R.id.radioGroup);
        nextPage = findViewById(R.id.goto_bpReader_instruction);


        /**go to next page with the user information
         * @param user_height
         *          height of user centimeter
         * @param user_weight
         *          weight of user in Kilogram
         * @param user_age
         *          age in years
         * @param user_gender
         *          male or female
         * @param qurdiac_output
         *          The amount of blood flow/min for male it is approximately 5 L/min for female its 4.5 L/min
         *          as stated in "Measure Vital Sign Using Smart Phone by Vikram Chandrasekaran"**/
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user_height = height.getText().toString();
                String user_weight = weight.getText().toString();
                String user_age = age.getText().toString();

                int selected_id = userGender.getCheckedRadioButtonId();
                gender = findViewById(selected_id);

                String user_gender = gender.getText().toString();

                if (user_gender.equals(m)){
                    cardiac_output = 5;
                }else{
                    cardiac_output = 4.5;
                }

                Intent goToNext = new Intent(view.getContext(), Instruction.class);
                Bundle data = new Bundle();
                data.putString("user_height", user_height);
                data.putString("user_weight", user_weight);
                data.putString("user_age", user_age);
                data.putDouble("user_cardiac_output", cardiac_output);
                if (!user_height.equals("") && !user_weight.equals("") && !user_age.equals("")){
                    goToNext.putExtras(data);
                    startActivity(goToNext);
                }else {
                    Toast.makeText(Home.this, "Fill in all the details", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
