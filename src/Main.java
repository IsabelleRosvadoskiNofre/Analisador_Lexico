import java.util.ArrayList;
import java.util.List;

/*
    Analisador Léxico e Sintático
    Maria Eduarda Mafra e Isabelle Rosvadowski
    RA: 2553120 e 2553031
 */
public class Main {
    public static void main(String[] args){
        try{
            //recebe o arquivo de leitura
            Leitor ler = new Leitor("testes/sintatico/programa1.gyh");
            Parser ps = new Parser(ler);

            ps.Programa();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}