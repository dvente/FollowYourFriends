package pt.findyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button mapsButton = (Button) findViewById(R.id.buttonMaps);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, MapsActivity.class));
            }
        });

        Button addFriendsButton = (Button) findViewById(R.id.buttonFriendsAdd);
        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, AddFriendsActivity.class));
            }
        });

        Button deleteFriendsButton = (Button) findViewById(R.id.buttonDeleteFriends);
        deleteFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, DeleteFriendsActivity.class));
            }
        });

        Button requestsButton = (Button) findViewById(R.id.buttonRequests);
        requestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, RequestsActivity.class));
            }
        });


    }
}
