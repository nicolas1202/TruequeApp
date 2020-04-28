package com.example.truequeapp;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.ui.login.LoginActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    NavigationView navigationView;
    String emailDB="";
    String emailapp;
    RequestQueue requestQueue;
    TextView tvemail;
    TextView tvenombre;
    ImageView imagenPerfil;
    int bandera2=0;
    final static String TAG = "BUTTON FACEBOOOOOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        //seteo de icono salir boton flotante
        fab.setImageResource(R.drawable.logout);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                    Toast.makeText(getApplicationContext(),"FB", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    bandera2 =0;
                    SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
                    preferences.edit().clear().commit();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                    finish();


            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        tvemail = (TextView) headerView.findViewById(R.id.et_EmailUsuario);
        tvenombre = (TextView) headerView.findViewById(R.id.et_NombreUsuario);
        imagenPerfil = headerView.findViewById(R.id.idFotoPerfil);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.idInicio, R.id.idMisProductos, R.id.idMatches, R.id.idCerrarSesion)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



        Bundle informacionUsuario = getIntent().getExtras();

        int bandera = informacionUsuario != null ? informacionUsuario.getInt("bandera", 2) : 2;
        RecibirDatosLogin(bandera);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void getInfoUser(String URL){

        JsonArrayRequest jsonArrayRequest =  new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++)  {
                    try {

                        jsonObject = response.getJSONObject(i);
                        tvemail.setText(jsonObject.getString("email"));
                        tvenombre.setText(jsonObject.getString("nombre") );
                        Log.i("email000",jsonObject.getString("email"));
                        Log.i("nombre000",jsonObject.getString("nombre"));
                        //apellidoDB = (jsonObject.getString("apellido"));


                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        );

            requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonArrayRequest);
    }

    public void RecibirDatosLogin(int bandera){

        if (bandera == 1){
            Bundle informacionUsuario = getIntent().getExtras();
            String setNombreFB = informacionUsuario.getString("NombreFB");
            String setEmailFB = informacionUsuario.getString("EmailFB");
            String setImagenFB = informacionUsuario.getString("ImagenPerfil");

            tvemail.setText(setEmailFB);
            tvenombre.setText(setNombreFB);
            Picasso.get().load(setImagenFB).into(imagenPerfil);


        }else{
            SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
            emailapp = (preferences.getString("email", "micorreo@gmail.com"));

            getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+emailapp+"");

        }
    }

}
