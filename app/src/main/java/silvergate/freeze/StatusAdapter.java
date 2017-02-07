package silvergate.freeze;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;


public class StatusAdapter extends BaseAdapter {
    List<User> listUser;

    Context mContext;
    String userId;
    boolean is_kudoed = false;
    int kudo_count;


    //constructor
    public StatusAdapter(Context mContext, List<User> userList) {
        this.listUser = userList;
        this.mContext = mContext;

    }

    public int getCount() {
        if (listUser != null)
            return listUser.size();
        else
            return 0;
    }

    public Object getItem(int arg0) {
        return listUser.get(arg0);
    }

    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getView(final int position, final View arg1, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_row, viewGroup, false);
        User user = listUser.get(position);
        TextView name = (TextView) row.findViewById(R.id.name);
        TextView status = (TextView) row.findViewById(R.id.status);

        name.setText(user.getUsername());
        String statusText  = "Away";
        statusText = user.getStatus();
        if(timeDifference(user.getUpdateTime())>2)
            statusText = "Unknown";
        status.setText(statusText);
        return row;
    }
    public static long timeDifference(double activityDate) {
        Date date = new Date((long) activityDate*1000);
        Date current_date = new Date();
        long secondsBetween = (current_date.getTime() - date.getTime()) / 1000;

        return secondsBetween/3600;
    }

}
