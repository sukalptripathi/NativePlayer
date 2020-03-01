package throwlink.appsters.sukalp.nativeplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // initializing the components

        list= findViewById(R.id.itemList);
        mBottomNavigationView=findViewById(R.id.bottom_navigation_view);






        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_app, new BrowseFragment()).commit();


        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();

                switch (item.getItemId())
                {

                    case R.id.bottom_browse:

                        fragmentTransaction.replace(R.id.frame_layout_app, new BrowseFragment());
                        fragmentTransaction.commit();
                        return true;

                    case R.id.bottom_upload:
                        fragmentTransaction.replace(R.id.frame_layout_app, new UploadFragment());
                        fragmentTransaction.commit();
                        return true;

                    case R.id.bottom_download:
                        fragmentTransaction.replace(R.id.frame_layout_app, new DownloadFragment());
                        fragmentTransaction.commit();
                        return true;
                }


                return true;
            }
        });


    }
}
