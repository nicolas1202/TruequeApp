package com.example.truequeapp.ui.inicio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.truequeapp.MainActivity;
import com.example.truequeapp.Producto;
import com.example.truequeapp.R;
import com.example.truequeapp.ui.CardItem;
import com.example.truequeapp.ui.CardsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import link.fls.swipestack.SwipeStack;


public class InicioFragment extends Fragment {

    private InicioViewModel inicioViewModel;


    private RequestQueue requestQueue;
    private String FK_idUser;
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

        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        try {

            emailFireBase = user.getEmail();
            if (emailFireBase != null) {
                //LOGIN FACEBOOK
                LogedInFacebook = true;
                Log.i("TAG", "setCardStackAdapter: se llama desde fb");
                getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + user.getEmail() + "");


            }
        } catch (Exception e) {

        }

        recuperarPreferencias();
        if (!LogedInFacebook) {
            //lOGIN EMAIL Y CONTRASEÑA
            Log.i("TAG", "setCardStackAdapter: se llama desde local");
            getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + emailPreferencia + "");

        }




        //Handling swipe event of Cards stack
        cardStack.setListener(new SwipeStack.SwipeStackListener() {
            @Override
            public void onViewSwipedToLeft(int position) {
                //like
                Toast.makeText(getActivity(), "You liked " + cardItems.get(currentPosition).getId(),
                        Toast.LENGTH_SHORT).show();
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
                cardItems.add(new CardItem(123232,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
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
                Toast.makeText(getActivity(), "You liked " + cardItems.get(currentPosition).getName(),
                        Toast.LENGTH_SHORT).show();
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

    }

    private void getInfoUser(String URL) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {

                        jsonObject = response.getJSONObject(i);
                        FK_idUser = jsonObject.getString("idUsuario");
                        setCardStackAdapter();
                        agregarProductoSpinner();
                    } catch (JSONException e) {
                        Log.i("TAG", "onResponse: " + e.getMessage());
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

    private void setCardStackAdapter() {


        Log.i("TAG", "setCardStackAdapter: Entro");
        cardItems = new ArrayList<>();
        ObtenerProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProducts.php?FK_idUser=" + FK_idUser + "");


        cardsAdapter = new CardsAdapter(getActivity(), cardItems);
        cardStack.setAdapter(cardsAdapter);
    /*
        cardItems = new ArrayList<>();

        cardItems.add(new CardItem(R.drawable.muestra, "¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));


        cardItems.add(new CardItem(R.drawable.triste,"¡No hay mas productos!","Vuelve pronto", "", "https://dam.ngenespanol.com/wp-content/uploads/2020/01/blue-monday.jpg"));
        cardsAdapter = new CardsAdapter(getActivity(), cardItems);
        cardStack.setAdapter(cardsAdapter);
*/
 }

    public void agregarProductoSpinner() {


        lista.add("Seleccione un producto");

        ObtenerMisProductos("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getProductUser.php?FK_idUser=" + FK_idUser + "");

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



}
