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
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    CognitoCachingCredentialsProvider credentialsProvider = null;
    DynamoDBMapper mapper;
    PaginatedScanList<User> userList;
    DynamoDBScanExpression scanExpression = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button login_button=(Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.POOL_ID, // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.login_button:
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
                mapper = new DynamoDBMapper(ddbClient);
                TextView txtUsername = (TextView)findViewById(R.id.user_name);
                TextView txtPassword = (TextView)findViewById(R.id.password);
                Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
                eav.put(":val1", new AttributeValue().withS(txtUsername.getText().toString()));
                eav.put(":val2", new AttributeValue().withS(txtPassword.getText().toString()));
                scanExpression = new DynamoDBScanExpression()
                        .withFilterExpression("user_id = :val1 and password = :val2")
                        .withExpressionAttributeValues(eav);
                try {
                    userList = mapper.scan(User.class, scanExpression);
                    userList.loadAllResults();
                    if(userList.size()>0)
                    {
                        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("user_id", userList.get(0).getUserId());
                        editor.putString("user_name", userList.get(0).getUsername());
                        editor.commit();
                        /*******************/
                        User user = new User();
                        user.setUserId(preferences.getString("user_id", ""));
                        user.setDevicetoken(preferences.getString("device_token", ""));
                        mapper.save(user, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.APPEND_SET));
                        /****************/

                        Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.login_invalid),Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }

                break;

        }
    }
}
