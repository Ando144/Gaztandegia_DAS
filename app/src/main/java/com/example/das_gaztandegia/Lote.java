package com.example.das_gaztandegia;

public class Lote {
    private int idLote;
    private String fecha;
    private String nota;

    // Constructor
    public Lote(int idLote, String fecha, String nota) {
        this.idLote = idLote;
        this.fecha = fecha;
        this.nota = nota;
    }

    // Getters para sacar la información
    public int getIdLote() { return idLote; }
    public String getFecha() { return fecha; }
    public String getNota() { return nota; }
}