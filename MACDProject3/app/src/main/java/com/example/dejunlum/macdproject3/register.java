package com.example.dejunlum.macdproject3;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {
    EditText regEmail,regPass,userName;
    Button regBtn;
    FirebaseAuth mFirebaseAuth;
    ImageButton image;
    RadioButton gender;
    RadioGroup rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFirebaseAuth = FirebaseAuth.getInstance();
        userName=findViewById(R.id.nameReg);
        regEmail=findViewById(R.id.emailReg);
        regPass=findViewById(R.id.passReg);
        regBtn=findViewById(R.id.button1);
        image=findViewById(R.id.userImg);
        rg=findViewById(R.id.rg1);

        setTitle("Register");

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=regEmail.getText().toString();
                String pass=regPass.getText().toString();
                final String name=userName.getText().toString();

                if(email.isEmpty()){
                    regEmail.setError("Please enter email");
                }
                if(pass.isEmpty()){
                    regPass.setError("Please enter password");
                }

                if(email.isEmpty()&&pass.isEmpty()){
                    Toast.makeText(register.this,"Fields are empty!",Toast.LENGTH_SHORT).show();
                }
                if(!(email.isEmpty()&&pass.isEmpty())){
                    String userEmail = regEmail.getText().toString();
                    String userPass = regPass.getText().toString();

                    mFirebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                User user = new User(
                                        name,
                                        email
                                );
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);

                                startActivity(new Intent(register.this, MainActivity.class));
                                Toast.makeText(register.this, "SignUp successful!", Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(register.this, "SignUp Unsuccessful, Please Try Again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }
    public void onRadioButtonClicked(View v) {
        image.setImageResource(R.drawable.male);
    }
    public void onRadioButtonClicked2(View v) {
        image.setImageResource(R.drawable.female);
    }
}
