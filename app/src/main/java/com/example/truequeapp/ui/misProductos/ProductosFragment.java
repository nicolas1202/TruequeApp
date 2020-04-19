package com.example.truequeapp.ui.misProductos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.R;
import com.example.truequeapp.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductosFragment extends Fragment {

    private ProductosViewModel productosViewModel;
    FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    String Fk_idUser;
    RequestQueue requestQueue;
    private static final String TAG = "DATOS FACEBOOK";
    String emailPreferencia;
    String emailFireBase;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        productosViewModel =
                ViewModelProviders.of(this).get(ProductosViewModel.class);
        View root = inflater.inflate(R.layout.mis_productos, container, false);
        final Button btnAgregarProducto = root.findViewById(R.id.btnAgregarProducto);
        productosViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {


            }
        });

        //TRAER EMAIL USUARIO SEGUN COMO SE HAYA LOGUEADO//



        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();
        try {
           emailFireBase=  user.getEmail();
            if (emailFireBase != null){
                //LOGIN FACEBOOK
                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+user.getEmail()+"");

            }else {
                //lOGIN EMAIL Y CONTRASEÑA
                recuperarPreferencias();
                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+emailPreferencia+"");

            }

        }catch (Exception e){

        }



        btnAgregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Insertar producto logeado con facebook
                if (emailFireBase != null){
                    //getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+user.getEmail()+"");
                    ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php");
                }else{
                    //Insertar producto logeado con email
                    recuperarPreferencias();
                    //getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getInfoUser.php?email="+emailPreferencia+"");
                    ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php");
                }

            }
        });

        return root;
    }

    public void getInfoUser(String URL){

        JsonArrayRequest jsonArrayRequest =  new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++)  {
                    try {

                        jsonObject = response.getJSONObject(i);
                        Fk_idUser = jsonObject.getString("idUsuario");

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch getinfouser", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        );

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonArrayRequest);
    }


    private void ejecutarServicio(String URL){


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), "Registro del producto exitoso", Toast.LENGTH_SHORT).show();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"Error en try catch ejecutar servicio", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("nombre", "Guitarra Criolla");
                parametros.put("descripcion", "Modelo 2015");
                parametros.put("imagen", "String de la imagen del producto");
                parametros.put("precio", "5400");
                parametros.put("FK_idUser", Fk_idUser);
                Log.i(TAG, "getParams: "+ Fk_idUser);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void recuperarPreferencias(){
        SharedPreferences preferences = this.getActivity().getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);

        emailPreferencia = preferences.getString("email", "micorreo@gmail.com");

    }
}
