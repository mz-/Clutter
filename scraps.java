/*public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            int iconDpi = activityManager.getLauncherLargeIconDensity();
            d = resources.getDrawableForDensity(iconId, iconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        Resources resources;
        try {
            resources = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info) {
        Resources resources;
        try {
            resources = mContext.getPackageManager().getResourcesForApplication(info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    private Drawable getAppIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }*/

    public static Drawable getIconFromPackageName(String packageName, Context context)
    {
        PackageManager pm = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        {
            try
            {
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                Context otherAppCtx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);

                int displayMetrics[] = {DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_HIGH, DisplayMetrics.DENSITY_TV};

                for (int displayMetric : displayMetrics)
                {
                    try
                    {
                        Drawable d = otherAppCtx.getResources().getDrawableForDensity(pi.applicationInfo.icon, displayMetric);
                        if (d != null)
                        {
                            return d;
                        }
                    }
                    catch (Resources.NotFoundException e)
                    {
//                      Log.d(TAG, "NameNotFound for" + packageName + " @ density: " + displayMetric);
                        continue;
                    }
                }

            }
            catch (Exception e)
            {
                // Handle Error here
            }
        }

        ApplicationInfo appInfo = null;
        try
        {
            appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return null;
        }

        return appInfo.loadIcon(pm);
    }




    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int getDominantColor (Bitmap bitmap) {
        Bitmap onePixel = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        int pixel = onePixel.getPixel(0,0);
        return pixel;
    }

    int color = getDominantColor(drawableToBitmap(appIcon));
            /*appNameView.setBackgroundColor(color);*/

            /*LayerDrawable bgDrawable = (LayerDrawable)appNameView.getBackground();
            final GradientDrawable shape = (GradientDrawable) (bgDrawable.findDrawableByLayerId(R.id.nametag));
            shape.setColor(color);*/



    public static long totalMemory() {
        StatFs mStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long totalBytes;
        int sdkNum = Build.VERSION.SDK_INT;
        if (sdkNum > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            totalBytes = (mStatFs.getTotalBytes());
        } else {
            totalBytes = mStatFs.getBlockCount() * mStatFs.getBlockSize();
        }
        return totalBytes;
    }

    public static long availableMemory() {
        StatFs mStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long freeBytes;
        int sdkNum = Build.VERSION.SDK_INT;
        if (sdkNum > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeBytes = (mStatFs.getAvailableBytes());
        } else {
            freeBytes = (mStatFs.getAvailableBlocks() * mStatFs.getBlockSize());
        }
        return freeBytes;
    }

    public static String memPrettyPrint(long value) {
        return new DecimalFormat("#.##").format((value/1048576));
    }