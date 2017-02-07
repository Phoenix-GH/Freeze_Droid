package silvergate.freeze;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    CognitoCachingCredentialsProvider credentialsProvider = null;
    DynamoDBMapper mapper;
    PaginatedScanList<User> userList;
    DynamoDBScanExpression scanExpression = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button signup_button=(Button)findViewById(R.id.signup_button);
        signup_button.setOnClickListener(this);
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.POOL_ID, // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        mapper = new DynamoDBMapper(ddbClient);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.signup_button:
                TextView txtUsername = (TextView)findViewById(R.id.user_name);
                TextView txtEmail = (TextView)findViewById(R.id.email);
                TextView txtPassword = (TextView)findViewById(R.id.password);
                TextView txtConfirm = (TextView)findViewById(R.id.confirm_passord);
                String userName = txtUsername.getText().toString();
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                String confirm  = txtConfirm.getText().toString();
                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if(password.compareTo(confirm)==0) {
                    Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
                    eav.put(":val1", new AttributeValue().withS(email));

                    scanExpression = new DynamoDBScanExpression()
                            .withFilterExpression("user_id = :val1")
                            .withExpressionAttributeValues(eav);
                    try {
                        userList = mapper.scan(User.class, scanExpression);
                        userList.loadAllResults();
                        if (userList.size() > 0) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
                        } else {

                            editor.putString("user_id", userList.get(0).getUserId());
                            editor.putString("user_name", userList.get(0).getUsername());
                            editor.commit();
                            User user = new User();
                            user.setPassword(password);
                            user.setUserId(userName);
                            user.setUseremail(email);
                            user.setUserId(email);
                            user.setStatus("Available");
                            user.setDevicetoken(preferences.getString("device_token",""));
                            mapper.save(user, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.APPEND_SET));
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);

                        }
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show();
                }
                break;

        }
    }
}
