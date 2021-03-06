

package me.loc2.loc2me.ui;

import android.accounts.OperationCanceledException;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.Views;
import me.loc2.loc2me.Loc2meServiceProvider;
import me.loc2.loc2me.R;
import me.loc2.loc2me.core.ApiService;
import me.loc2.loc2me.core.dao.OfferPersistService;
import me.loc2.loc2me.core.events.ForceReloadEvent;
import me.loc2.loc2me.core.services.ImageLoaderService;
import me.loc2.loc2me.core.services.OfferCheckBackgroundService;
import me.loc2.loc2me.events.NavItemSelectedEvent;
import me.loc2.loc2me.settings.SettingsFragment;
import me.loc2.loc2me.ui.md.BackPressListener;
import me.loc2.loc2me.ui.md.OfferListFragment;
import me.loc2.loc2me.util.Ln;
import me.loc2.loc2me.util.SafeAsyncTask;
import me.loc2.loc2me.util.UIUtils;


/**
 * Initial activity for the application.
 */
public class MainActivity extends Loc2meFragmentActivity {

    public static final String LIST_FRAGMENT_TAG = "ListFragmentTag";
    public static final String SETTINGS_FRAGMENT_TAG = "SettingsFragmentTag";

    @Inject
    protected Loc2meServiceProvider serviceProvider;

    private boolean userHasAuthenticated = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private OfferListFragment offerListFragment;
    private SettingsFragment settingsFragment;
    private CharSequence drawerTitle;
    private CharSequence title;

    @Override
    protected int getLayoutResource() {
        if (isTablet()) {
            return R.layout.main_activity_tablet;
        } else {
            return R.layout.main_activity;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        // View injection with Butterknife
        Views.inject(this);

        // Set up navigation drawer
        title = drawerTitle = getTitle();

        if (!isTablet()) {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerToggle = new ActionBarDrawerToggle(
                    this,                    /* Host activity */
                    drawerLayout,           /* DrawerLayout object */
                    R.drawable.ic_drawer,    /* nav drawer icon to replace 'Up' caret */
                    R.string.navigation_drawer_open,    /* "open drawer" description */
                    R.string.navigation_drawer_close) { /* "close drawer" description */

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);
        }
        checkAuth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_force).getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                eventBus.post(new ForceReloadEvent());
                return false;
            }
        });
        return true;
    }

    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!isTablet()) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle.syncState();
        }
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isTablet()) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }


    private void initScreen() {
        if (userHasAuthenticated) {


            if (!isServiceRunning(OfferCheckBackgroundService.class)) {
                startService(new Intent(this, OfferCheckBackgroundService.class));
            }

            openFragment(getOfferListFragment(), LIST_FRAGMENT_TAG);
        }

    }

    public void openFragment(Fragment fragment, String fragmentTag) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        this.setOnBackPressListener((BackPressListener) fragment);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragmentTag)
                .commit();
    }

    public OfferListFragment getOfferListFragment() {
        if (null == offerListFragment) {
            offerListFragment = new OfferListFragment();
        }
        return offerListFragment;
    }

    public SettingsFragment getSettingsFragment() {
        if (null == settingsFragment) {
            settingsFragment = new SettingsFragment();
        }
        return settingsFragment;
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final ApiService svc = serviceProvider.getService(MainActivity.this);
                return svc != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                userHasAuthenticated = true;
                initScreen();
            }
        }.execute();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                onBackPressed();
                return true;
//            case R.id.menu_add:
//                Offer offer = new Offer();
//                Random random = new Random();
//                int index = random.nextInt(10);
//                String indexStr = String.valueOf(index);
//
//                offer.setId(new Integer(indexStr));
//                offer.setWifi_name("Item " + indexStr);
//                offer.setDescription(getString(R.string.lorem_ipsum_long));
//                offer.setCreated_at("2015-05-21T14:24:05+0000");
//                offer.setUpdated_at("2015-05-21T14:24:05+0000");
//                offer.setAdded_at(new Date().getTime());
//                offer.setAvatar("https://pbs.twimg.com/profile_images/478608982915821568/k9u7RJmk.png");
//                Calendar cal = Calendar.getInstance();
//                cal.add(Calendar.DATE, -1 * (index + 1));
//                int descriptionColor = getResources().getColor(ColorGenerator.getNextCardColor());
//                offer.setBackgroundColor(descriptionColor);
//                offerEventService.add(offer);
//
//                offerPersistService.saveReceived(offer);
//                List<Offer> all = offerPersistService.findAllReceived();
//                Ln.i("All: " + Arrays.toString(all.toArray()));
//                return true;
            case R.id.menu_settings:
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                openFragment(getSettingsFragment(), SETTINGS_FRAGMENT_TAG);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onNavigationItemSelected(NavItemSelectedEvent event) {

        switch (event.getItemPosition()) {
            case 0:
                // Home
                // do nothing as we're already on the home screen.
                break;
        }
    }

}
