package silvergate.freeze;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.model.Message;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    CognitoCachingCredentialsProvider credentialsProvider = null;
    StatusAdapter adapter = null;
    DynamoDBScanExpression scanExpression = null;
    PaginatedScanList<User> userList;
    SharedPreferences preferences;
    public static DynamoDBMapper mapper = null;
    AWSCredentials awsCredentials;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView)findViewById(R.id.mainList);
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                Constants.POOL_ID, // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        mapper = new DynamoDBMapper(ddbClient);
        preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        loadData();

        awsCredentials = new BasicAWSCredentials(Constants.access_key,Constants.secret_key);
        final AmazonSNSClient pushClient = new AmazonSNSClient(awsCredentials);
        pushClient.setRegion(Region.getRegion(Regions.US_WEST_2));

        Button btnRefresh = (Button)findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        final String user_id;
        user_id = preferences.getString("user_id","");
        Switch status = (Switch)findViewById(R.id.status);
        status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                User user = new User();
                user.setUserId(user_id);
                Date current_date = new Date();
                double time = current_date.getTime()/1000.0;
                String status ;
                if(isChecked)
                {
                    status = "Available";

                }
                else {
                    status = "Away";

                }
                user.setStatus(status);
                String notification = String.format("%s is %s",preferences.getString("user_name",""),status);
                user.setUpdateTime(time);
                mapper.save(user, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.APPEND_SET));
                if(userList!=null)
                {
                    for(User item:userList)
                    {
                        try{
                            //PublishRequest request = new PublishRequest();

                            if(item.getDevicetoken()!=null) {
                                PublishRequest request = new PublishRequest().withTargetArn(item.getDevicetoken()).withMessage(notification).withSubject("aegaega");

                                pushClient.publish(request);
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }

                        //pushClient.publish()
                    }
                }
                loadData();
            }
        });
    }
    private void loadData()
    {
        scanExpression = new DynamoDBScanExpression();
        new GetData().execute();
    }
    @Override
    public void onResume() {
        super.onResume();

    }

    private class GetData extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... inputs) {
            try {
                userList = mapper.scan(User.class, scanExpression);
                userList.loadAllResults();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //Setting the header data
            if(userList!=null) {

                try {
                    adapter = new StatusAdapter(getBaseContext(), userList);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
