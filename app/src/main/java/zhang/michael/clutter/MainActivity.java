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


import android.preference.Preference;
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
import android.widget.Button;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean showButtons = preferences.getBoolean("pref_buttons", true);
        boolean showProgress = preferences.getBoolean("pref_progress", true);


        TextView progView = (TextView) findViewById(R.id.progress);
        progView.setText(packages.size()+" left");

        if (!showButtons) {
            keepButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else {
            keepButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
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
                                                if (packages.size() == 0) {
                                                    keepButton.setVisibility(View.GONE);
                                                    deleteButton.setVisibility(View.GONE);
                                                    restartButton.setVisibility(View.VISIBLE);
                                                }

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
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showButtons = preferences.getBoolean("pref_buttons", true);
        boolean showProgress = preferences.getBoolean("pref_progress", true);
        Button keepButton = (Button) findViewById(R.id.keep);
        Button deleteButton = (Button) findViewById(R.id.delete);
        TextView progressView = (TextView) findViewById(R.id.progress);
        if (!showButtons) {
            keepButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else {
            keepButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
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
