package com.example.truequeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorProductos extends RecyclerView.Adapter<AdaptadorProductos.ProductoViewHolder> implements View.OnClickListener {

    Context context;
    List<Producto> listaProductos;
    private Button btnEliminarProductoCardview;
    private View.OnClickListener listener;

    public AdaptadorProductos(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;

    }

    @NonNull
    @Override


    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_rv, parent, false);
        v.setOnClickListener(this);
        return new ProductoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {

        holder.tvNombre.setText(listaProductos.get(position).getNombre());
        holder.tvDescripcion.setText(listaProductos.get(position).getDescripcion());
        holder.tvPrecio.setText("$ " + listaProductos.get(position).getPrecio());
        //holder.imFoto.setImageResource(listaProductos.get(position).getFoto());

    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    @Override
    public void onClick(View v) {
        if (listener != null){
            listener.onClick(v);
        }
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {

      //  TextView tvIdProducto;
        TextView tvNombre;
        TextView tvDescripcion;
      //  ImageView imFoto;
        TextView tvPrecio;
        ImageView mDelete;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            mDelete = itemView.findViewById(R.id.ImageDelete);
            tvNombre = itemView.findViewById(R.id.tvNombreCV);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionCV);
          //  imFoto = itemView.findViewById(R.id.ivFotoCV);
            tvPrecio = itemView.findViewById(R.id.tvPrecioCV);

        }
    }




}
