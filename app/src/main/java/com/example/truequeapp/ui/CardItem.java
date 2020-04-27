package com.example.truequeapp.ui;

public class CardItem {

   private int drawableId;
    private String name;
    private String descripcion;
    private String precio;

    public CardItem(int drawableId, String name, String descripcion, String precio) {
        this.drawableId = drawableId;
        this.name = name;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
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

    /*
        public int getDrawableId() {
            return drawableId;
        }

        public void setDrawableId(int drawableId) {
            this.drawableId = drawableId;
        }
    */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}