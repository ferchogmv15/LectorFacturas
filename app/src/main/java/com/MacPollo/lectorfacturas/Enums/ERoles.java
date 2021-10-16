package com.MacPollo.lectorfacturas.Enums;

public enum ERoles {
    CONDUCTOR("Conductor", "CO"), REPRESENTANTE ("Representante Ventas", "RE");

    private String texto;
    private String codigo;

    private ERoles(String texto, String codigo) {
        this.texto = texto;
        this.codigo = codigo;
    }

    public String getTexto() { return this.texto; }

    public String getCodigo() { return  this.codigo; }
}
