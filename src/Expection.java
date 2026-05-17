public class Expection extends RuntimeException{
    public Expection(String message){
        super(message);
    }

    /*--------------------------- LÉXICA -----------------------------------------------*/
    public static Expection caractereInvalido(char carac, int linha) {
        return new Expection("Erro Léxico na linha " + linha + ": caractere " + carac + " não reconehcido");
    }

    public static Expection palavraInvalida(String palavra, int linha) {
        return new Expection("Erro Léxico na linha " + linha + ": palavra chave " + palavra + " não reconhecida");
    }

    public static Expection operadorInvalido(String carac, int linha){
        return new Expection("Erro léxico na linha " + linha + ": Operador " + carac + " não reconhecido");
    }

    public static Expection inicioPalavraInvalida(char carac, int linha){
        return new Expection("Erro léxico na linha " + linha + ": não existe palavra chave iniciada com " + carac);
    }

    public static Expection numeroInvalido(String carac, int linha){
        return new Expection("Erro léxico na linha " + linha + ": número " + carac + " não reconhecido");
    }

    public static Expection faltaParenteses(){
        return new Expection("Erro léxico: Desconhecido caractere (");
    }

    public static Expection faltaAspas(){
        return new Expection("Erro léxico: Desconecido caractere \"");
    }
    /*--------------------------- SINTÁTICA -----------------------------------------------*/
    public static Expection ErroSintatico(String erro, String recebido){
        return new Expection("Erro Sintático: esperado \"" + erro + "\", veio " + recebido);
    }
}
