package com.example.truequeapp.ui.misProductos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductosFragment extends Fragment {

    private ProductosViewModel productosViewModel;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private String FK_idUser;
    private  RequestQueue requestQueue;
    private static final String TAG = "DATOS FACEBOOK";
    private String emailPreferencia;
    private String emailFireBase;
    private RecyclerView rvListaProductos;
    private List<Producto> listaProductos;
    private AdaptadorProductos adaptador;
    private Boolean LogedInFacebook = false;
    private TextView tvID;

    //variables productos cardview
    int idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private String precioProducto;
    private boolean bandera = false;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        productosViewModel =
                ViewModelProviders.of(this).get(ProductosViewModel.class);
        View root = inflater.inflate(R.layout.mis_productos, container, false);
        final Button btnAgregarProducto = root.findViewById(R.id.btnAgregarProducto);


        rvListaProductos = root.findViewById(R.id.rvProductos);
        rvListaProductos.setLayoutManager(new GridLayoutManager(getContext(), 1));

        listaProductos = new ArrayList<>();
        adaptador = new AdaptadorProductos(getContext(), listaProductos);


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
                LogedInFacebook = true;

                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+user.getEmail()+"");


           }
        }catch (Exception e){

        }

        recuperarPreferencias();
            if (!LogedInFacebook) {
                //lOGIN EMAIL Y CONTRASEÑA
                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+emailPreferencia+"");

            }





        btnAgregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Insertar producto logeado con facebook
                if (LogedInFacebook){

                   ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php", false);
                    listaProductos.clear();
                    getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+user.getEmail()+"");

                }else{
                    //Insertar producto logeado con email
                    recuperarPreferencias();
                    ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php", false);
                    listaProductos.clear();
                    getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+emailPreferencia+"");
                }

            }
        });


            adaptador.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    //Mostrar un mensaje de confirmación antes de realizar el test
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setMessage("¿Desea eliminar el producto?");
                    alertDialog.setTitle("Eliminar Producto");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setCancelable(false);
                    alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //SI
                            int id;
                            id = listaProductos.get(rvListaProductos.getChildAdapterPosition(v)).getId();
                            ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/deleteProduct.php?idProduct="+id+"", true);
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //código java si se ha pulsado no
                        }
                    });
                    alertDialog.show();

                }
            });
        return root;
    }

    public void ObtenerProductos(String URL){
        JsonArrayRequest jsonArrayRequest =  new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++)  {
                    try {

                        jsonObject = response.getJSONObject(i);
                        listaProductos.add(
                                new Producto(
                                    idProducto = Integer.parseInt(jsonObject.getString("idProduct")),
                                    nombreProducto = jsonObject.getString("nombre"),
                                    descripcionProducto = jsonObject.getString("descripcion"),
                                   // fotoProducto = jsonObject.getString("imagen"),
                                    precioProducto = jsonObject.getString("precio")
                                    )
                        );



                        rvListaProductos.setAdapter(adaptador);


                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Productos", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: "+ error.getMessage());
            }
        }
        );

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonArrayRequest);
    }


    private void getInfoUser(String URL){

        JsonArrayRequest jsonArrayRequest =  new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i <=response.length(); i++)  {
                    try {

                      jsonObject = response.getJSONObject(i);
                      FK_idUser=  jsonObject.getString("idUsuario");


                    } catch (JSONException e) {
                        Log.i(TAG, "onResponse: "+ e.getMessage());
                    }
                }
                ObtenerProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProductUser.php?FK_idUser=" + FK_idUser + "");
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


    private void ejecutarServicio(String URL, final boolean bandera){


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (bandera){
                    Toast.makeText(getActivity(), "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    if (LogedInFacebook){
                        listaProductos.clear();
                        getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+user.getEmail()+"");

                    }else{
                        recuperarPreferencias();
                        listaProductos.clear();
                        getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email="+emailPreferencia+"");
                    }

                }else{
                    Toast.makeText(getActivity(), "Operación Exitosa", Toast.LENGTH_SHORT).show();
                }



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
                parametros.put("FK_idUser", FK_idUser);
                Log.i(TAG, "getParams: "+ FK_idUser);
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
    public void clear() {
        int size = listaProductos.size();
        listaProductos.clear();
        adaptador.notifyItemRangeRemoved(0, size);
    }
}
