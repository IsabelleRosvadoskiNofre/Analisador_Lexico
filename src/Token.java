

public class Token {
    private TipoToken tipo;
    private String txt;

    public Token(TipoToken tp, String texto){
        super();
        this.tipo = tp;
        this.txt = texto;
    }

    public Token(){
        super();
    }

    //getters e setters
    public TipoToken getTipo() {
        return tipo;
    }

    public void setTipo(TipoToken tipo) {
        this.tipo = tipo;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    //retorna o token em forma de string
    @Override
    public String toString() {
        return "<" + tipo + "," + txt + ">";
    }
}
