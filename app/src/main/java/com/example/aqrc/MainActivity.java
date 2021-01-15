package com.example.aqrc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.aqrc.ui.generate.GeneratedCodeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_scan, R.id.navigation_generate, R.id.navigation_history)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy called");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                Log.e("Scan", "Scanned");

               // tv_qr_readTxt.setText(result.getContents());
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        System.out.println("back pressed");

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            System.out.println("back pressed 000");
            //additional code
        } else {
            System.out.println("back pressed 2x");
//            final NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
//            controller.popBackStack();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("generatedFragment");
            Log.d(TAG, "onBackPressed: " + fragment.getClass().getSimpleName());

            getSupportFragmentManager().popBackStack();

        }

    }

//    @Override
//    public void onBackPressed() {
//        final Fragment currentFragment = getSupportFragmentManager().getFragments().get(0);
//        final NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
//        if (currentFragment instanceof GeneratedCodeFragment.OnBackPressedListener)
//            ((GeneratedCodeFragment.OnBackPressedListener) currentFragment).onBackPressed();
//        else if (!controller.popBackStack())
//            finish();
//
//    }


}