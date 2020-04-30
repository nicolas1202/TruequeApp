package com.example.truequeapp.ui.inicio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.truequeapp.R;
import com.example.truequeapp.ui.CardItem;
import com.example.truequeapp.ui.CardsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
            setCardStackAdapter(idUserFacebook);
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
            setCardStackAdapter(idUserLocal);
            agregarProductoSpinner(idUserLocal);
            emailguardado =  emailPreferencia;
            idGeneral = idUserLocal;
        }




        //Handling swipe event of Cards stack
        cardStack.setListener(new SwipeStack.SwipeStackListener() {
            @Override
            public void onViewSwipedToLeft(int position) {

                //Obtener idProducto Stack
                idStack =  cardItems.get(currentPosition).getId();

                recuperarPreferenciasIdProducto();
                if (cardItems.get(currentPosition).getId() != 999999999){
                   // ejecutarServicio( "https://truequeapp.000webhostapp.com/WebServiceTruequeApp/insertMatch.php", idProductoPref );
                    getInfoMatch( "https://truequeapp.000webhostapp.com/WebServiceTruequeApp/verificarMatchs.php?FK_idMiProducto=" +  idProductoPref + "&FK_idProductoLike="+ idStack  +"");
                }
                currentPosition = position + 1;

            }

            @Override
            public void onViewSwipedToRight(int position) {
                //dislike
                currentPosition = position + 1;
                Toast.makeText(getActivity(), "NO LIKE", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStackEmpty() {
                cardItems.add(new CardItem(999999999,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
            }
        });

      btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStack.swipeTopViewToRight();
            }
        });

        btnLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cardStack.swipeTopViewToLeft();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {


                Log.i("TAG", "onItemSelected: " + spinner.getSelectedItem().toString());
            if (spinner.getSelectedItem().toString().equals("Seleccione un producto")){
                cardStack.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnLove.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Seleccione un producto para permutar", Toast.LENGTH_LONG).show();
            }else{
                //  Obtener ID producto Spinner
                nombreProductoSpinner = spinner.getSelectedItem().toString();
                ObtenerIdProducto("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdProductSpinner.php?FK_idUser=" + idGeneral + "&nombre="+nombreProductoSpinner +"");
                cardStack.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnLove.setVisibility(View.VISIBLE);
            }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });


        return root;

    }

    private void ObtenerProductos(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;

                for (int i = 0; i < response.length(); i++) {
                    try {
                        Log.i("TAG", "ObtenerProductos");
                        jsonObject = response.getJSONObject(i);

                        cardItems.add(new CardItem(Integer.parseInt(jsonObject.getString("idProduct")),jsonObject.getString("nombre"),jsonObject.getString("descripcion"), jsonObject.getString("precio"), jsonObject.getString("imagen")));

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Error en try catch obtener Productos", Toast.LENGTH_SHORT).show();
                    }

                }
                Log.i("TAG", "onResponse: Cargo los productos");
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

    private void setCardStackAdapter(String id) {


        Log.i("TAG", "SetCardAdapter");
        cardItems = new ArrayList<>();
        ObtenerProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProducts.php?FK_idUser=" + id + "");


        cardsAdapter = new CardsAdapter(getActivity(), cardItems);
        cardStack.setAdapter(cardsAdapter);

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
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Log.i("TAG", "ObtenerMisProductos");
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

    private void ObtenerIdProducto(String URL) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;

                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                      jsonObject.getString("idProduct");
                      guardarIdProducto(jsonObject.getString("idProduct"));



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

    private void ejecutarServicio(String URL, final String id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                    Toast.makeText(getActivity(), "Like guardado", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", "error al insertar match");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("FK_idMiProducto", id);
                parametros.put("FK_idProductoLike", String.valueOf(idStack) );
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

                        Log.i("TAG", " cc=" + Integer.parseInt(jsonObject.getString("cc")));
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

    private void guardarIdProducto(String id){
        SharedPreferences preferences = getActivity().getSharedPreferences("idProductoPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("idProducto", id);

        editor.commit();
    }

    private void recuperarPreferenciasIdProducto() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("idProductoPreference", Context.MODE_PRIVATE);

        idProductoPref = preferences.getString("idProducto", "-1");

    }

    public void MostrarVentanaMatch(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.match, null);
        // Specify alert dialog is not cancelable/not ignorable
        builder.setCancelable(false);
        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        Button btnWhatsapp = (Button) dialogView.findViewById(R.id.btnWapp);
        Button btnSeguirBuscando = (Button) dialogView.findViewById(R.id.btnBuscar);
        final EditText etNombre = dialogView.findViewById(R.id.etNombreProd);
        final EditText etDescrip = dialogView.findViewById(R.id.etDescrProd);
        final EditText etPrecio = dialogView.findViewById(R.id.etPrecioProd);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Set positive/yes button click listener
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.cancel();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String uri = "whatsapp://send?phone=" + "5492616176700" + "&text=" + "¡Hola! Hicimos match con nuestros productos en Trueque App.";
                intent.setData(Uri.parse(uri));
                startActivity(intent);

                //name = etNombre.getText().toString();
                //desc = etDescrip.getText().toString();
               // precio = etPrecio.getText().toString();


            }
        });

        // Set negative/no button click listener
        btnSeguirBuscando.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss/cancel the alert dialog
                //dialog.cancel();
                dialog.dismiss();

            }
        });

        // Display the custom alert dialog on interface
        dialog.show();

        //Insertar producto logeado con facebook

    }
}
