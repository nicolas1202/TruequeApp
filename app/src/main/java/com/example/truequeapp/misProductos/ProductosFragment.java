package com.example.truequeapp.misProductos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truequeapp.R;
import com.google.android.material.textfield.TextInputLayout;
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
    private RequestQueue requestQueue;
    private static final String TAG = "DATOS FACEBOOK";
    private String emailPreferencia;
    private String emailFireBase;
    private RecyclerView rvListaProductos;
    private List<Producto> listaProductos;
    private AdaptadorProductos adaptador;
    private Boolean LogedInFacebook = false;
    private TextView tvID;

    //DECLARAR VARIABLES PARA TOMAR FOTO!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private ImageView mimageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    //variables productos cardview
    int idProducto;
    private String nombreProducto;
    private String fotoProducto;
    private String descripcionProducto;
    private String precioProducto;
    private boolean bandera = false;
    String name ;
    String desc ;
    String precio ;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        productosViewModel =
                ViewModelProviders.of(this).get(ProductosViewModel.class);
        final View root = inflater.inflate(R.layout.mis_productos, container, false);
        final Button btnAgregarProducto = root.findViewById(R.id.btnAgregarProducto);

        rvListaProductos = root.findViewById(R.id.rvProductosI);
        rvListaProductos.setLayoutManager(new GridLayoutManager(getContext(), 1));

        listaProductos = new ArrayList<>();
        adaptador = new AdaptadorProductos(getContext(), listaProductos);

        //INICIALIZAR VIEW DEL FRAGMENT Y VARIABLE DE IMAGEVIEW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final View viewAddProduct = inflater.inflate(R.layout.add_product, container, false);
        mimageView = (ImageView) viewAddProduct.findViewById(R.id.imageView2);
        if(null == mimageView) {
            Log.e("Error", "Ouh! there is no child view with R.id.imageView ID within my parent view View.");
        }

        productosViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {


            }
        });

        //TRAER EMAIL Y PRODUCTOS USUARIO SEGUN COMO SE HAYA LOGUEADO//
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

        btnAgregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_product, null);

                // Specify alert dialog is not cancelable/not ignorable
                builder.setCancelable(false);

                // Set the custom layout as alert dialog view
                builder.setView(dialogView);

                // Get the custom alert dialog view widgets reference
                Button btn_seleccionarFoto = (Button) dialogView.findViewById(R.id.btnAgregarFoto);
                Button btn_positive = (Button) dialogView.findViewById(R.id.btnBuscar);
                Button btn_negative = (Button) dialogView.findViewById(R.id.btnWapp);
                //final EditText etNombre = dialogView.findViewById(R.id.etNombreProd);
                //final EditText etDescrip = dialogView.findViewById(R.id.etDescrProd);
                //final EditText etPrecio = dialogView.findViewById(R.id.etPrecioProd);

                final TextInputLayout et_Nombre = dialogView.findViewById(R.id.etNombreProd);;
                final TextInputLayout et_Descrip = dialogView.findViewById(R.id.etDescrProd);;
                final TextInputLayout et_Precio = dialogView.findViewById(R.id.etPrecioProd);;

                // Create the alert dialog
                final AlertDialog dialog = builder.create();

                //LISTENER DEL BOTÓN LLAMAR FOTO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                btn_seleccionarFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        if(imageTakeIntent.resolveActivity(getActivity().getPackageManager())!=null){ //Hay que agregar getActivity() porque se encuentra en un fragment (No es buena práctica)
                            startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                });

                // Set positive/yes button click listener
                btn_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //FALTA VALIDAR FOTO
                        if(!validateTitle(et_Nombre) | !validateDescription(et_Descrip) | !validatePrice(et_Precio) ) { //Debe ir una sola barra para que muestre ambos mensajes
                            return;
                        }

                        //name = etNombre.getText().toString();
                        //desc = etDescrip.getText().toString();
                        //precio = etPrecio.getText().toString();
                        name = et_Nombre.getEditText().getText().toString().trim();
                        desc = et_Descrip.getEditText().getText().toString().trim();
                        precio = et_Precio.getEditText().getText().toString().trim();

                        Toast.makeText(getActivity(),
                                "Submitted name : " + name, Toast.LENGTH_SHORT).show();
                        // Say hello to the submitter
                        if (LogedInFacebook) {
                            ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php", false);
                            listaProductos.clear();
                            getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + user.getEmail() + "");
                        } else {
                            //Insertar producto logeado con email
                            recuperarPreferencias();
                            ejecutarServicio("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/InsertProducto.php", false);
                            listaProductos.clear();
                            getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + emailPreferencia + "");
                        }
                        // Dismiss the alert dialog
                        dialog.cancel();
                    }
                });

                // Set negative/no button click listener
             btn_negative.setOnClickListener(new View.OnClickListener() {
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
                alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //SI
                        int id;
                        id = listaProductos.get(rvListaProductos.getChildAdapterPosition(v)).getId();
                        EliminarProducto("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/deleteProduct.php?idProduct=" + id + "", true);
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //código java si se ha pulsado no
                    }
                });
                alertDialog.show();

            }
        });
        return root;
    }

    //FUNCIÓN PARA COLOCAR FOTO EN EL IMAGEVIEW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==Activity.RESULT_OK) {
            try {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                mimageView.setImageBitmap(imageBitmap);
                Log.d("YESSS!!!!!!!!!!", "La imagen se debería haber cargado en el imageView");
            } catch (Exception e){
                Log.d("ERROR IMAGE!!!!!!!!!!", e.getMessage());
            }
        }
    }

    //Funciones de validacion
    private boolean validateTitle(TextInputLayout et_Nombre) {
        name = et_Nombre.getEditText().getText().toString().trim();
        Log.d("myTag", name);
        if (name.isEmpty()) {
            et_Nombre.setError("El campo no puede estar vacío");
            return false;
        } else {
            et_Nombre.setError(null);
            return true;
        }
    }

    private boolean validateDescription(TextInputLayout et_Descrip) {
        desc = et_Descrip.getEditText().getText().toString().trim();

        if (desc.isEmpty()) {
            et_Descrip.setError("El campo no puede estar vacío");
            return false;
        } else {
            et_Descrip.setError(null);
            return true;
        }
    }

    private boolean validatePrice(TextInputLayout et_Precio) {
        precio = et_Precio.getEditText().getText().toString().trim();

        if (precio.isEmpty()) {
            et_Precio.setError("El campo no puede estar vacío");
            return false;
        } else {
            et_Precio.setError(null);
            return true;
        }
    }
    /*
    private boolean validatePhoto() {

    }*/
    //Funciones de validacion /

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
                                        idProducto = Integer.parseInt(jsonObject.getString("idProduct")),
                                        nombreProducto = jsonObject.getString("nombre"),
                                        descripcionProducto = jsonObject.getString("descripcion"),
                                        precioProducto = jsonObject.getString("precio"),
                                        fotoProducto = jsonObject.getString("imagen")

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
                Log.i(TAG, "onErrorResponse: " + error.getMessage());
            }
        }
        );

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonArrayRequest);
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
                        Log.i(TAG, "onResponse: " + e.getMessage());
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


    private void ejecutarServicio(String URL, final boolean bandera) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {




            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("nombre", name);
                parametros.put("descripcion", desc);
                parametros.put("imagen", "https://static.mercadoshops.com/guitarra-criolla-admira-paloma-palermo_iZ1075530178XvZgrandeXpZ3XfZ246744778-799583688-3XsZ246744778xIM.jpg");
                parametros.put("precio", precio);
                parametros.put("FK_idUser", FK_idUser);
                Log.i(TAG, "getParams: " + FK_idUser);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private void EliminarProducto(String URL, final boolean bandera) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (bandera) {
                    Toast.makeText(getActivity(), "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    if (LogedInFacebook) {
                        listaProductos.clear();
                        getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + user.getEmail() + "");

                    } else {
                        recuperarPreferencias();
                        listaProductos.clear();
                        getInfoUser("https://truequeapp.000webhostapp.com/WebServiceTruequeApp/getIdUser.php?email=" + emailPreferencia + "");
                    }

                } else {
                    Toast.makeText(getActivity(), "Operación Exitosa", Toast.LENGTH_SHORT).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private void recuperarPreferencias() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);

        emailPreferencia = preferences.getString("email", "micorreo@gmail.com");

    }

    public void clear() {
        int size = listaProductos.size();
        listaProductos.clear();
        adaptador.notifyItemRangeRemoved(0, size);
    }
}
