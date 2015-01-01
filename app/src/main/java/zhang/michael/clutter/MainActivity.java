package zhang.michael.clutter;



import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.drawable.Drawable;

import android.net.Uri;



import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;


import android.widget.ImageView;

import android.widget.TextView;



import com.lorentzos.flingswipe.SwipeFlingAdapterView;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private List<ApplicationInfo> allPackages;
    private ArrayList<ApplicationInfo> packages;
    private IconNameAdapter appsAdapter;
    private Context mContext = this;
    private ApplicationInfo lastSeen;
    private SwipeFlingAdapterView cardsContainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardsContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
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
        appsAdapter.updateAppsList(packages);
        cardsContainer.setAdapter(appsAdapter);



        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        boolean showProgress = preferences.getBoolean("pref_progress", true);


        TextView progView = (TextView) findViewById(R.id.progress);



        if (!showProgress) {
            progView.setVisibility(View.GONE);
        } else {
            progView.setVisibility(View.VISIBLE);
        }



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

                                                cardExitAction();
                                            }

                                            @Override
                                            public void onRightCardExit(Object dataObject) {
                                                cardExitAction();
                                                lastSeen = ((ApplicationInfo) dataObject);

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

                                            public void cardExitAction() {

                                                TextView progView = (TextView) findViewById(R.id.progress);
                                                int remainingApps = packages.size();
                                                String updateProgress = remainingApps + " left";
                                                if (remainingApps <= 5 && remainingApps > 0) {
                                                    updateProgress += ", almost there!";
                                                }
                                                if (remainingApps == 0) {
                                                    updateProgress = "You're done!";
                                                }
                                                progView.setText(updateProgress);
                                            }
                                        }


        );






    }

    public void swipeRight(View view) {
        cardsContainer.getSelectedView().findViewById(R.id.item_swipe_right_indicator).setAlpha((float) 0.6);
        cardsContainer.getTopCardListener().selectRight();
    }

    public void swipeLeft(View view) {
        cardsContainer.getSelectedView().findViewById(R.id.item_swipe_left_indicator).setAlpha((float) 0.6);
        cardsContainer.getTopCardListener().selectLeft();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean showProgress = preferences.getBoolean("pref_progress", true);

        TextView progressView = (TextView) findViewById(R.id.progress);

        if (!showProgress) {
            progressView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.VISIBLE);
        }
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
            case R.id.action_restart:
                restartActivity();
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

    public void launchCurrentApp(View view) {
        Intent appIntent = getPackageManager().getLaunchIntentForPackage(packages.get(0).packageName);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivityForResult(appIntent,0);
    }

    public void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public Drawable getAppIcon(ApplicationInfo app) {
        Drawable appIcon;
        PackageManager pm = mContext.getPackageManager();
        try {
            Resources resourcesForApplication = pm.getResourcesForApplication(app);
            Configuration config = resourcesForApplication.getConfiguration();
            Configuration originalConfig = new Configuration(config);

            DisplayMetrics displayMetrics = resourcesForApplication.getDisplayMetrics();
            DisplayMetrics originalDisplayMetrics =  resourcesForApplication.getDisplayMetrics();
            displayMetrics.densityDpi =  DisplayMetrics.DENSITY_XXHIGH;
            resourcesForApplication.updateConfiguration(config, displayMetrics);

            appIcon = resourcesForApplication.getDrawable(app.icon);
        } catch (PackageManager.NameNotFoundException e) {
            appIcon = app.loadIcon(pm);
        } catch (Resources.NotFoundException e) {
            appIcon = app.loadIcon(pm);
        }
        return appIcon;
    }


    public class IconNameAdapter extends BaseAdapter {

        private ArrayList<ApplicationInfo> installedApps = new ArrayList();
        private PackageManager pm;

        private final Context context;

        public IconNameAdapter(Context context) {
            this.context = context;
            this.pm = context.getPackageManager();
        }

        public void updateAppsList(ArrayList<ApplicationInfo> updatedApps) {
            this.installedApps = updatedApps;
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
            appNameView.setText(pm.getApplicationLabel(currApp)+"?");

            iconView.setImageDrawable(getAppIcon(currApp));
            return convertView;
        }

    }



}
