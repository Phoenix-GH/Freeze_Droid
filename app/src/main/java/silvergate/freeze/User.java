package silvergate.freeze;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.Serializable;

@DynamoDBTable(tableName = "Users")
public class User implements Serializable {
    private String user_id;

    private String user_name;
    private String user_email;
    private String password;
    private String device_token;
    private String status;
    private double update_time;
    @DynamoDBHashKey(attributeName = "user_id")
    public String getUserId() {
        return user_id;
    }
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    @DynamoDBAttribute(attributeName = "user_name")
    public String getUsername() {
        return user_name;
    }

    public void setUsername(String user_name) {
        this.user_name = user_name;
    }

    @DynamoDBAttribute(attributeName = "user_email")
    public String getUseremail() {
        return user_email;
    }

    public void setUseremail(String user_email) {
        this.user_email = user_email;
    }

    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    @DynamoDBAttribute(attributeName = "device_token")
    public String getDevicetoken() {
        return device_token;
    }
    public void setDevicetoken(String device_token) {
        this.device_token = device_token;
    }

    @DynamoDBAttribute(attributeName = "update_time")
    public double getUpdateTime() {
        return update_time;
    }
    public void setUpdateTime(double update_time) {
        this.update_time = update_time;
    }

}

