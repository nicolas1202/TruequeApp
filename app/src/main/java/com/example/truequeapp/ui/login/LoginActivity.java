package com.example.truequeapp.ui.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    EditText et_username;
    EditText et_password;
    Button btnLoginDB;
    Button btnRegistro;
    String email;
    String pass;
    int banderaFacebookOno= 2;

    //VARIABLES LOGIN FACEBOOK
    private CallbackManager mCallbackManager ;
    private FirebaseAuth mFirebaseAuth;
    private LoginButton loginButton;
    private static final String TAG = "FacebookAuthentication";
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    String nombreFB;
    String emailFB;
    String imagenPerfil;
    //FIN VARIABLES FACEBOOK


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);


    //-----------------------------------------------------//
        //FACEBOOK
        mFirebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<com.facebook.login.LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG,"onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"onError" + error);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    UpdateUI(user);
                }else{
                    UpdateUI(null);
                }
            }
        };



        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null){
                    mFirebaseAuth.signOut();
                }
            }
        };

        if (accessTokenTracker != null){
            banderaFacebookOno = 1;
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            UpdateUI(user);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("NombreFB", nombreFB);
            intent.putExtra("EmailFB", emailFB);
            intent.putExtra("ImagenPerfil", imagenPerfil);
            intent.putExtra("bandera", banderaFacebookOno);
            startActivity(intent);
        }

        // FIN FACEBOOK
        //-----------------------------------------------------//




        et_password = findViewById(R.id.etPassword);
        et_username = findViewById(R.id.etUsername);
        btnLoginDB = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
        recuperarPreferencias();


        btnLoginDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = et_username.getText().toString();
                pass = et_password.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty()){
                    validarUsuario("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/loginUsuario.php");
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

    private void handleFacebookToken(AccessToken token){
        Log.d(TAG, "handleFacebookToken" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    banderaFacebookOno = 1;
                    Log.d(TAG, "Sign in with credentials: Successfull");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    UpdateUI(user);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("NombreFB", nombreFB);
                    intent.putExtra("EmailFB", emailFB);
                    intent.putExtra("ImagenPerfil", imagenPerfil);
                    intent.putExtra("bandera", banderaFacebookOno);
                    RegistrarUsuarioFacebook("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/insertUsuarioFB.php", nombreFB, emailFB, imagenPerfil, intent);
                    //startActivity(intent);
                }else{
                    Log.d(TAG, "Sign in with credentials: Failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Autentication failed", Toast.LENGTH_LONG).show();
                    UpdateUI(null);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void RegistrarUsuarioFacebook(String URL, final String nombrep, final String emailpa, final String fotoPerfilp, final Intent intentp){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(getApplicationContext(), "USUARIO FB GUARDADO", Toast.LENGTH_SHORT).show();
               startActivity(intentp);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("nombre", nombrep);
                parametros.put("email", emailpa);
                parametros.put("fotoPerfil", fotoPerfilp);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);




    }

    private void UpdateUI(FirebaseUser user){

        if (user != null){
            nombreFB = user.getDisplayName();
            emailFB = user.getEmail();
            if (user.getPhotoUrl() != null){
                String photoURL = user.getPhotoUrl().toString();
                photoURL = photoURL + "?type=large";
                imagenPerfil = photoURL;
               // Picasso.get().load(photoURL).into(mLogo);
            }
        }else{
           Toast.makeText(getApplicationContext(), "Datos usuario vacios QUITAR MENSAJE", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }


    }

    private void validarUsuario(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //vemos que la respuesta no venga vacia
                if (!response.isEmpty()){
                    banderaFacebookOno = 2;
                    guardarPreferencias();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("bandera", banderaFacebookOno);
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
