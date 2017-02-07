package silvergate.freeze;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button login_button=(Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        Button signup_button=(Button)findViewById(R.id.signup_button);
        signup_button.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch(view.getId())
        {
            case R.id.login_button:
                intent = new Intent(this,LoginActivity.class);
                break;
            case R.id.signup_button:
                intent = new Intent(this,SignupActivity.class);
                break;
        }
        startActivity(intent);
    }
}
