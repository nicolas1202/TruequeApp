package com.example.truequeapp.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.Main.MainActivity;
import com.example.truequeapp.R;
import com.example.truequeapp.Registro.Registro;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private TextInputLayout et_username;
    private TextInputLayout et_password;
    private TextView email_pass_incorrect_message;
    Button btnLoginDB;
    Button btnRegistro;
    String email;
    String pass;
    String passDBDesencriptada;
    String passDBDEencriptada;
    int banderaFacebookOno= 2;
    int banderaActivityMain;
    RequestQueue requestQueue;
    //VARIABLES LOGIN FACEBOOK
    private CallbackManager mCallbackManager ;
    private FirebaseAuth mFirebaseAuth;
    private LoginButton loginButton;
    private static final String TAG = "FacebookAuthentication";
    private FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;
    String idlocal;

    String nombreFB;
    String emailFB;
    String imagenPerfil;
    String idFace;
    //FIN VARIABLES FACEBOOK


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        getSupportActionBar().hide(); //Quitar barra superior Sign in

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


        user = mFirebaseAuth.getCurrentUser();
        if (user != null){

        banderaFacebookOno = 1;

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

        et_username = findViewById(R.id.etUsername);
        et_password = findViewById(R.id.etPassword);
        email_pass_incorrect_message = findViewById(R.id.EmailPassIncorrectMessage);

        btnLoginDB = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
        recuperarPreferencias();

        btnLoginDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateEmail() | !validatePassword()) { //Debe ir una sola barra para que muestre ambos mensajes
                    email_pass_incorrect_message.setVisibility(View.INVISIBLE);
                    return;
                }
                email = et_username.getEditText().getText().toString().trim();
                pass = et_password.getEditText().getText().toString().trim();
                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+email+"");

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

    private boolean validateEmail() {
        String emailInput = et_username.getEditText().getText().toString().trim();

        if(emailInput.isEmpty()) {
            et_username.setError("El campo no puede estar vacío");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            et_username.setError("Por favor inserta un email");
            return false;
        } else {
            et_username.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = et_password.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            et_password.setError("El campo no puede estar vacío");
            return false;
        } else {
            et_password.setError(null);
            return true;
        }
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
                    getIdUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+emailFB+"");
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
                //Picasso.get().load(photoURL).into(mLogo);
            }
        }else{
          // Toast.makeText(getApplicationContext(), "Datos usuario vacios QUITAR MENSAJE", Toast.LENGTH_LONG).show();
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

    private void validarUsuario(String URL, final String id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //vemos que la respuesta no venga vacia
                if (!response.isEmpty()){
                    banderaFacebookOno = 2;
                    guardarPreferencias(id);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("bandera", banderaFacebookOno);
                    startActivity(i);
                    finish();
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
                parametros.put("password", passDBDesencriptada);

                return parametros;
            }
        };
        //procesa las peticiones hechas por la app
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Proceso para guardar email y contraseña para que no tenga que ingresarlo cada vez que abre la app
    private void guardarPreferencias(String id){
        SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("email", email);
        editor.putString("password", pass);
        editor.putString("idlocal", id);
        editor.putBoolean("sesion", true);
        editor.commit();
    }

    //Proceso para guardar email y contraseña para que no tenga que ingresarlo cada vez que abre la app
    private void guardarPreferenciasFacebook(String id){
        SharedPreferences preferences = getSharedPreferences("preferenciasLoginFacebook", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("email", emailFB);
        editor.putString("idf", id);
        editor.commit();
    }

    //Proceso para leer email y contraseña para que no tenga que ingresarlo cada vez que abre la app
    private void recuperarPreferencias(){
        SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);

        //et_username.setEditText(preferences.getString("email", "micorreo@gmail.com"));
        //et_password.setText(preferences.getString("password", "micontraseña"));
    }

    public static String Desencriptar(String textoEncriptado)  {

        String secretKey = "qualityinfosolutions"; //llave para desenciptar datos
        String base64EncryptedString = "";

        try {
            byte[] message = Base64.decodeBase64(textoEncriptado.getBytes("utf-8"));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");

            Cipher decipher = Cipher.getInstance("DESede");
            decipher.init(Cipher.DECRYPT_MODE, key);

            byte[] plainText = decipher.doFinal(message);

            base64EncryptedString = new String(plainText, "UTF-8");

        } catch (Exception ex) {
        }
        return base64EncryptedString;
    }
    public void getInfoUser(String URL){

        JsonArrayRequest jsonArrayRequest =  new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++)  {
                    try {

                        jsonObject = response.getJSONObject(i);
                        passDBDesencriptada = Encriptar(pass) ;
                        passDBDEencriptada = jsonObject.getString("password");
                        String id = jsonObject.getString("idUsuario");
                        Log.i(TAG, "PASS ENCRIPTADA TV " + passDBDesencriptada);
                        Log.i(TAG, "PASS ENCRIPTADA DB " + passDBDEencriptada);
                    if (passDBDesencriptada.equals(passDBDEencriptada)){
                        validarUsuario("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/loginUsuario.php", id);
                    }



                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "error getinfouser", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                email_pass_incorrect_message.setVisibility(View.VISIBLE); //Si no coinciden con la base de datos mostrar activando este mensaje
            }
        }
        );

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    public static String Encriptar(String texto) {

        String secretKey = "qualityinfosolutions"; //llave para encriptar datos
        String base64EncryptedString = "";

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] plainTextBytes = texto.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            base64EncryptedString = new String(base64Bytes);

        } catch (Exception ex) {
        }
        return base64EncryptedString;
    }
    private void getIdUser(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Log.i("TAG", "getInfoUser");
                        jsonObject = response.getJSONObject(i);
                        idFace = jsonObject.getString("idUsuario");
                        guardarPreferenciasFacebook(idFace);
                        Log.i("TAG", "RECUPERO PREFERENCIAS FACEBOOK ID:"+idFace);
                    } catch (JSONException e) {
                        Log.i("TAG", "error getinfouserINICIO ");
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        );
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);


    }
}
