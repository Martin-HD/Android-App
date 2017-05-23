package com.example.mada.nordicearthquake;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class aboutMe extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutme);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed()
    {

        //handles the backbutton presses.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_control)
        {
            Intent controlActivitynav = new Intent(this, MainActivity.class);
            startActivity(controlActivitynav);

        }
        else if (id == R.id.nav_aboutme)
        {
            Intent aboutMeIntent = new Intent(this, aboutMe.class);
            startActivity(aboutMeIntent);

        }
        else if (id == R.id.nav_aboutnordic)
        {
            Intent aboutnordicIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.nordicsemi.com/eng/About-us"));
            startActivity(aboutnordicIntent);

            //webview.loadUrl("https://www.nordicsemi.com/eng/About-us"); //Need to implement webview in the code for the xml in the activity.
            // TODO: Possible solution, more streamlined, but takes more implementation, as we need to add to backstack and design the activity and link it, also the navigation drawer and overflow menu, should be copy and paste, but errors may occur.


        }
        else if (id == R.id.aboutntnu)
        {
            Intent aboutntnuIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.ntnu.edu/about"));
            startActivity(aboutntnuIntent);

        }
        else if (id == R.id.nav_devzone)
        {
            Intent devzoneIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://devzone.nordicsemi.com"));
            startActivity(devzoneIntent);
        }
        else if (id == R.id.nav_github)
        {
            Intent githubIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.github.com/NordicSemiconductor"));
            startActivity(githubIntent);
        }
        else if(id == R.id.nav_physweb)
        {
            Intent physwebIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://andreln.github.io"));
            startActivity(physwebIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
