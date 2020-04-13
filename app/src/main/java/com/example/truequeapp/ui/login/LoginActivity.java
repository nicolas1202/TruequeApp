package com.example.truequeapp.ui.login;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.MainActivity;
import com.example.truequeapp.R;
import com.example.truequeapp.Registro;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    EditText et_username;
    EditText et_password;
    Button loginButton ;
    Button btnRegistro;
    String email;
    String pass;
    String nombre;
    String apellido;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);


        et_password = findViewById(R.id.etPassword);
        et_username = findViewById(R.id.etUsername);
        loginButton = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
        recuperarPreferencias();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = et_username.getText().toString();
                pass = et_password.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty()){
                    validarUsuario("http://192.168.54.187:80/WebServiceTruequeApp/loginUsuario.php");
                }else{
                    Toast.makeText(getApplicationContext(), "Ingrese usuario o contraseña", Toast.LENGTH_LONG).show();
                }


            }
        });

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Registro.class);
                startActivity(i);
            }
        });
    }



    private void validarUsuario(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //vemos que la respuesta no venga vacia
                if (!response.isEmpty()){
                    guardarPreferencias();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("email", email);
                parametros.put("password", pass);

                return parametros;
            }
        };
        //procesa las peticiones hechas por la app
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Proceso para guardar email y contraseña para que no tenga que ingresarlo cada vez que abre la app
    private void guardarPreferencias(){
        SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("email", email);
        editor.putString("password", pass);
        editor.putBoolean("sesion", true);
        editor.commit();
    }

    //Proceso para leer email y contraseña para que no tenga que ingresarlo cada vez que abre la app
    private void recuperarPreferencias(){
        SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);

        et_username.setText(preferences.getString("email", "micorreo@gmail.com"));
        et_password.setText(preferences.getString("password", "micontraseña"));
    }
}
