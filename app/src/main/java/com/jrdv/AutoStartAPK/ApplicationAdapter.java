package com.jrdv.AutoStartAPK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ApplicationAdapter extends BaseAdapter {

    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public ApplicationAdapter(Context context, int textViewResourceId,
                              List<ApplicationInfo> appsList) {
        this.context = context;
        this.appsList = appsList;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((appsList == null) ? 0 : appsList.size());
    }

    @Override
    public Object getItem(int position) {
        return ((appsList == null) ? null : appsList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.row_item, null);
        }

        final ApplicationInfo applicationInfo = appsList.get(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //aqui guardamos el valor

                SharedPreferences.Editor editor = context.getSharedPreferences(StartupActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("apkname", applicationInfo.packageName);
                editor.apply();

                Log.i("INFO", "================ ==guardado el packagename:"+ applicationInfo.packageName);


                Toast.makeText(v.getContext(), "Re-open me and choose apk again!!!", Toast.LENGTH_SHORT).show();


                // y salimos de esta activity

                StartupActivity.Yaelegidoapkname(context);




               // Intent LaunchIntent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName);
                //context.startActivity(LaunchIntent);





            }
        });
        if (null != applicationInfo) {
            TextView appName = (TextView) view.findViewById(R.id.app_name);
            TextView packageName = (TextView) view.findViewById(R.id.app_paackage);
            ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);

            appName.setText(applicationInfo.loadLabel(packageManager));
            packageName.setText(applicationInfo.packageName);
            iconview.setImageDrawable(applicationInfo.loadIcon(packageManager));
        }
        return view;
    }
}