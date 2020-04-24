package com.example.truequeapp.ui.inicio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.AdaptadorProductos;
import com.example.truequeapp.Producto;
import com.example.truequeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private InicioViewModel inicioViewModel;
    private RecyclerView rvListaProductos;
    private List<Producto> listaProductos;
    private AdaptadorProductos adaptador;
    private RequestQueue requestQueue;
    private String FK_idUser;
    private String emailFireBase;
    private String emailPreferencia;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private Boolean LogedInFacebook = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        inicioViewModel =
                ViewModelProviders.of(this).get(InicioViewModel.class);
        View root = inflater.inflate(R.layout.mi_inicio, container, false);


        //rvListaProductos = root.findViewById(R.id.rvProductosI);
        //rvListaProductos.setLayoutManager(new GridLayoutManager(getContext(), 1));

        listaProductos = new ArrayList<>();
        adaptador = new AdaptadorProductos(getContext(), listaProductos);



        inicioViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();
        try {

            emailFireBase = user.getEmail();
            if (emailFireBase != null) {
                //LOGIN FACEBOOK
                LogedInFacebook = true;

                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + user.getEmail() + "");


            }
        } catch (Exception e) {

        }

        recuperarPreferencias();
        if (!LogedInFacebook) {
            //lOGIN EMAIL Y CONTRASEÑA
            getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + emailPreferencia + "");

        }
        return root;
    }

    private void ObtenerProductos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                        listaProductos.add(
                                new Producto(
                                        Integer.parseInt(jsonObject.getString("idProduct")),
                                        jsonObject.getString("nombre"),
                                        jsonObject.getString("descripcion"),
                                        // fotoProducto = jsonObject.getString("imagen"),
                                        jsonObject.getString("precio")
                                )
                        );


                        //rvListaProductos.setAdapter(adaptador);


                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Productos", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", "onErrorResponse: " + error.getMessage());
            }
        }
        );

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonArrayRequest);
    }

    private void recuperarPreferencias() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);

        emailPreferencia = preferences.getString("email", "micorreo@gmail.com");

    }

    private void getInfoUser(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i <= response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                        FK_idUser = jsonObject.getString("idUsuario");


                    } catch (JSONException e) {
                        Log.i("TAG", "onResponse: " + e.getMessage());
                    }
                }
                ObtenerProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProducts.php?FK_idUser=" + FK_idUser + "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        );
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);


    }
}
