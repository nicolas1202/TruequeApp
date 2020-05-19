package com.example.truequeapp.inicio;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.truequeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import link.fls.swipestack.SwipeStack;


public class InicioFragment extends Fragment {

    private InicioViewModel inicioViewModel;


    private RequestQueue requestQueue;
    private String FK_idUser;
    private int  count;
    private String emailFireBase;
    private String emailPreferencia;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private Boolean LogedInFacebook = false;
    private SwipeStack cardStack;
    private CardsAdapter cardsAdapter;
    private ArrayList<CardItem> cardItems;
    private View btnCancel;
    private View btnLove;
    private int currentPosition;
    private Spinner spinner;
    List<String> lista;
    private String emailguardado;
    private int idStack;
    private String nombreProductoSpinner;
    private String idFaceb;
    private String idUserFacebook;
    private String idUserLocal;
    private String idGeneral;
    private String idProductoPref;
    private String imagenProductoPref;
    private String imagenProductoStack;
    private String idProductoSpinner;
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        inicioViewModel =
                ViewModelProviders.of(this).get(InicioViewModel.class);
        final View root = inflater.inflate(R.layout.mi_inicio, container, false);
        spinner =  root.findViewById(R.id.SpinerMisProductos);
        lista = new ArrayList<String>();

        inicioViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });




        cardStack = root.findViewById(R.id.container);
        btnCancel = root.findViewById(R.id.cancel);
        btnLove = root.findViewById(R.id.love);
        currentPosition = 0;

try {


        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        emailFireBase = user.getEmail();
        if (emailFireBase != null){
            recuperarPreferenciasFacebook();
            idUserFacebook = idFaceb;
            Log.i("TAG", "idUserFacebook "+idUserFacebook);
            Log.i("TAG", "Se llama desde fb");
            LogedInFacebook = true;
          //  setCardStackAdapter(idUserFacebook);
            agregarProductoSpinner(idUserFacebook);
            emailguardado =  user.getEmail();
            idGeneral = idUserFacebook;
        }
}catch (Exception e){

}

        if (!LogedInFacebook) {
            recuperarPreferencias();
            //lOGIN EMAIL Y CONTRASEÑA
            Log.i("TAG", "Se llama desde local");
            Log.i("TAG", "idUserLocal"+ idUserLocal);
          //  setCardStackAdapter(idUserLocal);
            agregarProductoSpinner(idUserLocal);
            emailguardado =  emailPreferencia;
            idGeneral = idUserLocal;
        }




        //Handling swipe event of Cards stack
        cardStack.setListener(new SwipeStack.SwipeStackListener() {
            @Override
            public void onViewSwipedToLeft(int position) {
                //dislike
                currentPosition = position + 1;
              //  Toast.makeText(getActivity(), "NO LIKE", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onViewSwipedToRight(int position) {

                //LIKE

                //Obtener idProducto Stack
                idStack =  cardItems.get(currentPosition).getId();
                imagenProductoStack =  cardItems.get(currentPosition).getImagen();
                recuperarPreferenciasDatosProducto();
                Log.i("TAG", "id producto antes de insertar match: " + idProductoPref);
                if (cardItems.get(currentPosition).getId() != 999999999){
                    getInfoMatch( "https://truequeapp.000webhostapp.com/WebServiceTruequeApp/verificarMatchs.php?FK_idMiProducto=" +
                            idProductoPref + "&FK_idProductoLike="+ idStack  +"");
                  // ejecutarServicio( "https://truequeapp.000webhostapp.com/WebServiceTruequeApp/insertLike.php?", idProductoPref, idGeneral);
                }
                currentPosition = position + 1;

            }

            @Override
            public void onStackEmpty() {
                cardItems.add(new CardItem(999999999,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
            }
        });

         btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { cardStack.swipeTopViewToLeft();
            }
        });

        btnLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStack.swipeTopViewToRight();

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

            if (spinner.getSelectedItem().toString().equals("Seleccione un producto")){
                cardStack.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnLove.setVisibility(View.INVISIBLE);
            } else {
                //  Obtener ID producto Spinner

                btnCancel.setVisibility(View.VISIBLE);
                btnLove.setVisibility(View.VISIBLE);
                nombreProductoSpinner = spinner.getSelectedItem().toString();
                Log.i("TAG", "Item seleccionado del Spinner: " + spinner.getSelectedItem().toString());
                Log.i("TAG", "idGeneral " + idGeneral);
                ObtenerDatosProducto("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdProductSpinner.php?FK_idUser=" + idGeneral + "&nombre="+spinner.getSelectedItem().toString() +"");
                recuperarPreferenciasDatosProducto();
               // setCardStackAdapter(idGeneral);
                cardStack.setVisibility(View.VISIBLE);

            }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
              //  cardItems.add(new CardItem(999999999,"¡Seleccione su producto!","", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));

            }
        });

        root.clearFocus();
        return root;

    }

    private void ObtenerProductos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                Log.i("TAG", "Limpio la lista");
                cardItems.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);

                        cardItems.add(new CardItem(Integer.parseInt(jsonObject.getString("idProduct")),jsonObject.getString("nombre"),jsonObject.getString("descripcion"), jsonObject.getString("precio"), jsonObject.getString("imagen")));

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Productos", Toast.LENGTH_SHORT).show();
                    }

                }
                Log.i("TAG", "Agrego Productos a la lista");
               // cardItems.add(new CardItem(R.drawable.triste,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
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
        idUserLocal = preferences.getString("idlocal", "-4");
        Log.i("TAG", "email referencia: " + emailPreferencia);
    }

    private void recuperarPreferenciasFacebook() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("preferenciasLoginFacebook", Context.MODE_PRIVATE);

        idFaceb = preferences.getString("idf", "-2");
        Log.i("TAG", "email referencia: " + idFaceb);
    }

    private void getInfoUser(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Log.i("TAG", "getInfoUser");
                        jsonObject = response.getJSONObject(i);
                        FK_idUser = jsonObject.getString("idUsuario");

                    } catch (JSONException e) {
                        Log.i("TAG", "error getinfouserINICIO ");
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
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);


    }

    private void setCardStackAdapter(String idUser, String idProducto) {

        cardItems = new ArrayList<>();
        Log.i("TAG", "SetCardAdapter");


        Log.i("TAG", "IDUSER" + idUser);
        Log.i("TAG", "IDPRODUCTO - " + idProducto);

        ObtenerProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProducts.php?FK_idUser=" + idUser +
                        "&FK_idProducto="+idProducto +"");

        recuperarPreferenciasDatosProducto();
        Log.i("TAG", "IMAGENPRODUCTO - " + imagenProductoPref);


        cardsAdapter = new CardsAdapter(getActivity(), cardItems);
        cardStack.setAdapter(cardsAdapter);
        cardsAdapter.notifyDataSetChanged();
 }

    public void agregarProductoSpinner(String id) {

        Log.i("TAG", "AgregarProductoSpinner");
        lista.add("Seleccione un producto");

        ObtenerMisProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProductUser.php?FK_idUser=" + id + "");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, lista);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    private void ObtenerMisProductos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                Log.i("TAG", "Agrego mis productos al spinner");
                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                        lista.add(jsonObject.getString("nombre"));

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Mis Productos", Toast.LENGTH_SHORT).show();
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

    private void ObtenerDatosProducto(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;

                for (int i = 0; i < response.length(); i++) {
                    try {
                        Log.i("TAG", "Guardo id e imagen del producto del spinner");
                        jsonObject = response.getJSONObject(i);
                      guardarDatosProducto(jsonObject.getString("idProduct"),jsonObject.getString("imagen"));
                        setCardStackAdapter(idGeneral, jsonObject.getString("idProduct"));


                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Productos", Toast.LENGTH_SHORT).show();
                    }

                }

                // cardItems.add(new CardItem(R.drawable.triste,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", "Error obtener id producto " + error.getMessage());
            }
        }
        );

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonArrayRequest);
    }

    private void getIdUser(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                        FK_idUser = jsonObject.getString("idUsuario");


                    } catch (JSONException e) {
                        Log.i("TAG", "ErrorGetiidUserINICIO");
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
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);


    }

    private void ejecutarServicio(String URL, final String FK_idMiProducto, final String FK_idUsuario){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                    Toast.makeText(getActivity(), "Like guardado", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", "error al insertar like");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("FK_idProductoLike", String.valueOf(idStack) );
                parametros.put("FK_idMiProducto", String.valueOf(FK_idMiProducto) );
                parametros.put("FK_idUsuario", String.valueOf(FK_idUsuario) );
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void getInfoMatch(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;

                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);

                        Log.i("TAG", " cc=" +(jsonObject.getString("cc")));
                        if (jsonObject.getString("cc").equals("2")){
                            MostrarVentanaMatch();
                        }
                    } catch (JSONException e) {
                        Log.i("TAG", "Error getinfomatchINICIO");
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
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);


    }

    private void guardarDatosProducto(String id, String imagen){
        SharedPreferences preferences = getActivity().getSharedPreferences("idProductoPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("idProducto", id);
        editor.putString("imagenProducto", imagen);
        editor.commit();
    }

    private void recuperarPreferenciasDatosProducto() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("idProductoPreference", Context.MODE_PRIVATE);

        idProductoPref = preferences.getString("idProducto", "-1");
        imagenProductoPref = preferences.getString("imagenProducto", "-1");
        Log.i("TAG", "recuperarPreferenciasDatosProducto: " + idProductoPref);
        Log.i("TAG", "recuperarPreferenciasDatosProducto: " + imagenProductoPref);
    }

    @SuppressLint("ResourceAsColor")
    public void MostrarVentanaMatch(){

        String miProductoImagen;
        String productoLikeImagen;

        miProductoImagen = imagenProductoPref;
        productoLikeImagen = imagenProductoStack;
        Log.i("TAG", "String imagen MI producto: "+ miProductoImagen);
        Log.i("TAG", "String imagen  producto stack: "+ productoLikeImagen);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Transparent);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.match, null);
        dialogView.setBackgroundColor(Color.TRANSPARENT);


        // Specify alert dialog is not cancelable/not ignorable
        builder.setCancelable(false);
        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        Button btnWhatsapp = (Button) dialogView.findViewById(R.id.btnWapp);
        Button btnSeguirBuscando = (Button) dialogView.findViewById(R.id.btnBuscar);
        ImageView miProducto = dialogView.findViewById(R.id.ivMiproducto);
        ImageView ProductoLike = dialogView.findViewById(R.id.ivProductoLike);

        //Picasso.get().load(miProductoImagen).into(miProducto);
        //Picasso.get().load(productoLikeImagen).into(ProductoLike);

        Glide.with(getContext()).load(miProductoImagen).apply(new RequestOptions().circleCrop()).into(miProducto);
        Glide.with(getContext()).load(productoLikeImagen).apply(new RequestOptions().circleCrop()).into(ProductoLike);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Set positive/yes button click listener
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

            }
        });

        // Set negative/no button click listener
        btnSeguirBuscando.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });
        dialog.show();
    }

    }
