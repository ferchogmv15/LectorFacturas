package com.MacPollo.lectorfacturas.General;

public class Formatos {

    public static String formatoValor(String input){
        StringBuilder sb = new StringBuilder();
        int i = input.length();
        while (i - 3 > 0) {
            sb.insert(0, input.substring(i-3, i));
            i -= 3;
            if (i > 0) {
                sb.insert(0, ".");
            }
        }
        if (i > 0) {
            sb.insert(0, input.substring(0, i));
        }
        sb.insert(0, "$");
        return sb.toString();
    }
}
