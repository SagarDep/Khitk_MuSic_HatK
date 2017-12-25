package akki.dem;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class FeaturesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        //ActionBarLogo
        ActionBar ab = getSupportActionBar();
        ab.setLogo(R.drawable.logo);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_xml, menu);
        return super.onCreateOptionsMenu( menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                Intent intet=new Intent(this,About_Activity.class);
                startActivity(intet);
        }
        return super.onOptionsItemSelected(item);
    }

}
