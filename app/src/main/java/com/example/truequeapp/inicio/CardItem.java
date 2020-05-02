package com.example.truequeapp.inicio;

public class CardItem {

   private int Id;
    private String name;
    private String descripcion;
    private String precio;
    private String imagen;

    public CardItem(int Id, String name, String descripcion, String precio, String imagen) {
        this.Id = Id;
        this.name = name;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
    }

    public CardItem(int Id,String name, String descripcion,  String imagen) {
        this.Id = Id;
        this.name = name;
        this.descripcion = descripcion;
        this.imagen = imagen;
    }

    public CardItem( String name) {

        this.name = name;

    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public int getId() {
        return Id;
    }

    public void setId(int drawableId) {
        this.Id = drawableId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }


        public int Id() {
            return Id;
        }

        public void Id(int Id) {
            this.Id = Id;
        }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}