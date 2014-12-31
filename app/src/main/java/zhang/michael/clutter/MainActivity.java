package zhang.michael.clutter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
&& ((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP & pInfo.flags) != 0)
*/

public class MainActivity extends ActionBarActivity {
    private List<ApplicationInfo> allPackages;
    private ArrayList<ApplicationInfo> packages;
    private IconNameAdapter appsAdapter;
    private Context mContext = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*ImageView img = (ImageView)findViewById(R.id.ima);*/

        final SwipeFlingAdapterView cardsContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        final PackageManager pManager = mContext.getPackageManager();
        allPackages = pManager.getInstalledApplications(PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES |
                PackageManager.GET_UNINSTALLED_PACKAGES);
        packages = new ArrayList<ApplicationInfo>();

        for (ApplicationInfo app : allPackages) {
            if (isUserPackage(app)) {
                packages.add(app);
            }
        }

        appsAdapter = new IconNameAdapter(mContext);
        appsAdapter.updateAppsList(packages, pManager);
        cardsContainer.setAdapter(appsAdapter);

        final Button restartButton = (Button) findViewById(R.id.restart);
        restartButton.setVisibility(View.GONE);
        final Button keepButton = (Button) findViewById(R.id.keep);
        final Button deleteButton = (Button) findViewById(R.id.delete);





        TextView progView = (TextView) findViewById(R.id.progress);
        progView.setText(packages.size()+" left");

        cardsContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
                                            @Override
                                            public void removeFirstObjectInAdapter() {
                                                packages.remove(0);
                                                appsAdapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onLeftCardExit(Object dataObject) {

                                                Uri packageURI = Uri.parse("package:" + ((ApplicationInfo) dataObject).packageName);
                                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                                                startActivity(uninstallIntent);
                                                if (packages.size() == 0) {
                                                    keepButton.setVisibility(View.GONE);
                                                    deleteButton.setVisibility(View.GONE);
                                                    restartButton.setVisibility(View.VISIBLE);
                                                }

                                                TextView progView = (TextView) findViewById(R.id.progress);
                                                progView.setText(packages.size()+" left");

                                            }

                                            @Override
                                            public void onRightCardExit(Object dataObject) {
                                                if (packages.size() == 0) {
                                                    keepButton.setVisibility(View.GONE);
                                                    deleteButton.setVisibility(View.GONE);
                                                    restartButton.setVisibility(View.VISIBLE);
                                                }

                                            }

                                            @Override
                                            public void onAdapterAboutToEmpty(int itemsInAdapter) {

                                            }

                                            @Override
                                            public void onScroll(float v) {
                                                View view = cardsContainer.getSelectedView();
                                                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(v < 0 ? (float) (-0.6*v) : 0);
                                                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(v > 0 ? (float) (0.6*v) : 0);
                                            }
                                        }


        );

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cardsContainer.getTopCardListener().selectLeft();
            }
        });

        keepButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cardsContainer.getTopCardListener().selectRight();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean isUserPackage(ApplicationInfo pInfo) {
        return ((pInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1);
    }

    public void showAbout() {
        Intent aboutIntent = new Intent(this, About.class);
        startActivity(aboutIntent);
    }

    public void restartActivity(View v) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }




    public class IconNameAdapter extends BaseAdapter {

        private ArrayList<ApplicationInfo> installedApps = new ArrayList();
        private PackageManager pm;

        private final Context context;

        public IconNameAdapter(Context context) {
            this.context = context;
        }

        public void updateAppsList(ArrayList<ApplicationInfo> updatedApps, PackageManager pman) {
            this.installedApps = updatedApps;
            this.pm = pman;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return installedApps.size();
        }

        @Override
        public ApplicationInfo getItem(int position) {
            return installedApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iconView;
            TextView appNameView;
            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                  .inflate(R.layout.item, parent, false);
                iconView = (ImageView) convertView.findViewById(R.id.icon);
                appNameView = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(R.id.icon, iconView);
                convertView.setTag(R.id.name, appNameView);
            } else {
                iconView = (ImageView) convertView.getTag(R.id.icon);
                appNameView = (TextView) convertView.getTag(R.id.name);
            }

            ApplicationInfo currApp = getItem(position);
            /*Drawable icon = getIconFromPackageName(currApp.packageName, mContext);*/
            appNameView.setText(pm.getApplicationLabel(currApp)+"?");

            Drawable appIcon;
            try {
                Resources resourcesForApplication = pm.getResourcesForApplication(currApp);
                Configuration config = resourcesForApplication.getConfiguration();
                Configuration originalConfig = new Configuration(config);

                DisplayMetrics displayMetrics = resourcesForApplication.getDisplayMetrics();
                DisplayMetrics originalDisplayMetrics =  resourcesForApplication.getDisplayMetrics();
                displayMetrics.densityDpi =  DisplayMetrics.DENSITY_XXHIGH;
                resourcesForApplication.updateConfiguration(config, displayMetrics);

                appIcon = resourcesForApplication.getDrawable(currApp.icon);
            } catch (PackageManager.NameNotFoundException e) {
                appIcon = currApp.loadIcon(pm);
            }

            iconView.setImageDrawable(appIcon);
            return convertView;
        }

    }



}
