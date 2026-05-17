import java.nio.file.Files;
import java.nio.file.Paths;

public class Leitor {
    private final char[] conteudo;
    private int pos;
    private int linha = 1;
    private boolean parentese = false;
    private boolean aspas = false;

    //leitura completa do arquivo em um vetor de caracteres
    public Leitor(String file) {
        try{
            String txtConteudo = new String(Files.readAllBytes(Paths.get(file)));
            conteudo = txtConteudo.toCharArray();
            pos = 0;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Leitor(Leitor outro) {
        this.conteudo = outro.conteudo;
        this.pos = outro.pos;
        this.linha = outro.linha;
        this.parentese = outro.parentese;
        this.aspas = outro.aspas;
    }

    public Token nextToken(){
        char atual;
        String buffer = "";
        Token token;

        if (isEOF()) {
            return null;
        }

        //enquanto não chegar no final do arquivo, lê o próximo caractere
        int estado = 0;
        while(!isEOF()){
            atual = nextChar();
            buffer += atual;

            switch (estado){
                case 0: //estado inicial
                    if(isLowerChar(atual))
                        estado = 1;
                    else if (isDigit(atual))
                        estado = 2;
                    else if (isSpace(atual)) {//retira o caractere lido e continua
                        buffer = buffer.substring(0, buffer.length() - 1);
                    }
                    else if (isOperator(atual))
                        switch (atual) {
                            case '<' -> estado = 7;
                            case '>' -> estado = 10;
                            case '!' -> estado = 13;
                            case '=' -> estado = 15;
                            case ':' -> estado = 17;
                            case '+' -> {
                                token = new Token();
                                token.setTipo(TipoToken.OpAritSoma);
                                token.setTxt(buffer);
                                return token;
                            }
                            case '-' -> {
                                token = new Token();
                                token.setTipo(TipoToken.OpAritSub);
                                token.setTxt(buffer);
                                return token;
                            }
                            case '*' -> {
                                token = new Token();
                                token.setTipo(TipoToken.OpAritMult);
                                token.setTxt(buffer);
                                return token;
                            }
                            case '/' -> {
                                token = new Token();
                                token.setTipo(TipoToken.OpAritDiv);
                                token.setTxt(buffer);
                                return token;
                            }
                            case '"' -> {
                                aspas = true;   //enquanto não receber aspas novamente, não sai do true
                                estado = 24;
                            }
                            case '(' -> {
                                parentese = true;   //enquanto não receber fecha parenteses, não sai do true
                                token = new Token();
                                token.setTipo(TipoToken.AbrePar);
                                token.setTxt(buffer);
                                return token;
                            }
                            case ')' -> {   //recebeu o fechar parenteses, volta para false
                                parentese = false;
                                token = new Token();
                                token.setTipo(TipoToken.FechaPar);
                                token.setTxt(buffer);
                                return token;
                            }
                            case '#' -> estado = 43;
                            default -> throw Expection.operadorInvalido(buffer, linha);
                        }
                    else if (isUpperChar(atual))
                        estado = switch (atual) {   //letras iniciais das palavras chave
                            case 'D' -> 28;
                            case 'P' -> 31;
                            case 'L' -> 40;
                            case 'R' -> 32;
                            case 'S' -> 44;
                            case 'E' -> 29;
                            case 'I' -> 35;
                            case 'F' -> 45;
                            case 'O' -> 33;
                            default -> throw Expection.inicioPalavraInvalida(atual, linha);
                        };
                    else
                        throw Expection.caractereInvalido(atual, linha);
                    break;

                case 1: //se for letra minuscula, possível variável
                    if(isLowerChar(atual) || isDigit(atual)) {
                        continue;
                    }
                    else{
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.Var);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }

                case 2: //se for dígito
                    if(isDigit(atual)) {
                        continue;
                    }
                    else if(atual == '.')
                        estado = 3;
                    else if(!(isLowerChar(atual)||isUpperChar(atual))){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.NumInt);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }
                    else
                        throw Expection.numeroInvalido(buffer, linha);
                    break;

                case 3: //se receber um ponto, pode ser número real
                    if (isDigit(atual))
                        continue;
                    else if(!(isLowerChar(atual)||isUpperChar(atual))){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.NumReal);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }
                    else
                        throw Expection.numeroInvalido(buffer, linha);

                case 7: // caso o primeiro caractere operador seja <
                    if(atual == '=') {
                        token = new Token();
                        token.setTipo(TipoToken.OpRelMenorIgual);
                        token.setTxt(buffer);
                        return token;
                    }
                    else {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.OpRelMenor);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }

                case 10: // caso o primeiro caractere operador seja >
                    if(atual == '='){
                        token = new Token();
                        token.setTipo(TipoToken.OpRelMaiorIgual);
                        token.setTxt(buffer);
                        return token;
                    }
                    else{
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.OpRelMaior);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }

                case 13: // caso o primeiro caractere operador seja !
                    if(atual == '='){
                        token = new Token();
                        token.setTipo(TipoToken.OpRelDif);
                        token.setTxt(buffer);
                        return token;
                    }
                    else
                        throw Expection.operadorInvalido(buffer, linha);

                case 15: // caso o primeiro caractere operador seja =
                    if(atual == '='){
                        token = new Token();
                        token.setTipo(TipoToken.OpRelIgual);
                        token.setTxt(buffer);
                        return token;
                    }
                    else
                        throw Expection.numeroInvalido(buffer, linha);

                case 17: // caso o primeiro caractere operador seja :
                    if(atual == '='){
                        token = new Token();
                        token.setTipo(TipoToken.Atrib);
                        token.setTxt(buffer);
                        return token;
                    }
                    else{
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.Delim);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }

                case 24: // caso o primeiro caractere operador seja "
                    if(atual == '"') { //fecha caso recceba o segundo "
                        aspas = false;
                        token = new Token();
                        token.setTipo(TipoToken.Cadeia);
                        token.setTxt(buffer);
                        return token;
                    }else
                        continue;

                case 28: // caso o caractere recebido seja D
                    if(atual == 'E')
                        estado = 29;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 29: // caso o caractere recebido seja E
                    if(atual == 'C')
                        estado = 30;
                    else if(atual == 'R')
                        estado = 32;
                    else if(atual == 'A')
                        estado = 41;
                    else if(atual == 'N')
                        estado = 36;
                    else if(buffer.substring(0, buffer.length() - 1).equals("SE") && !isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCSe);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else if(buffer.substring(0, buffer.length() - 1).equals("E") && !isUpperChar(atual)){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.OpBoolE);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 30: // caso o caractere recebido seja C
                    if(!isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCDec);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);

                case 31: // atual for P
                    if(atual == 'R')
                        estado = 32;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 32: // atual for R
                    if(atual == 'O')
                        estado = 33;
                    else if(atual == 'I')
                        estado = 35;
                    else if(atual == 'E')
                        estado = 29;
                    else if(buffer.substring(0, buffer.length() - 1).equals("IMPRIMIR") && !isUpperChar(atual)){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCImprimir);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }
                    else if(buffer.substring(0, buffer.length() - 1).equals("LER") && !isUpperChar(atual)){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCLer);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 33: // atual for O
                    if(atual == 'G')
                        estado = 34;
                    else if(atual == 'U')
                        estado = 46;
                    else if(!isUpperChar(atual)){
                        buffer = buffer.substring(0, buffer.length() - 1);
                        switch (buffer) {
                            case "SENAO" -> {
                                backtracking();
                                token = new Token();
                                token.setTipo(TipoToken.PCSenao);
                                token.setTxt(buffer);
                                return token;
                            }
                            case "ENTAO" -> {
                                backtracking();
                                token = new Token();
                                token.setTipo(TipoToken.PCEntao);
                                token.setTxt(buffer);
                                return token;
                            }
                            case "ENQTO" -> {
                                backtracking();
                                token = new Token();
                                token.setTipo(TipoToken.PCEnqto);
                                token.setTxt(buffer);
                                return token;
                            }
                        }
                    }
                    else {
                        throw Expection.palavraInvalida(buffer.substring(0, buffer.length() - 1), linha);
                    }
                    break;

                case 34: // atual for G
                    if(!isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCProg);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);

                case 35: // atual for I
                    if(atual == 'N')
                        estado = 36;
                    else if(atual == 'M')
                        estado = 39;
                    else if(atual == 'R')
                        estado = 32;
                    else if(buffer.substring(0, buffer.length() - 1).equals("INI") && !isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCIni);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 36: // atual for N
                    if(atual == 'T')
                        estado = 37;
                    else if(atual == 'A')
                        estado = 41;
                    else if(atual == 'Q')
                        estado = 42;
                    else if(atual == 'I')
                        estado = 35;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 37: // atual for T
                    if(!isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCInt);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else if(atual == 'A')
                        estado = 41;
                    else if(atual == 'O')
                        estado = 33;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 39: // atual for M
                    if(atual == 'P')
                        estado = 31;
                    else if(atual == 'I')
                        estado = 35;
                    else if(buffer.substring(0, buffer.length() - 1).equals("FIM") && !isUpperChar(atual)) {
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCFim);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 40: // atual for L
                    if(atual == 'E')
                        estado = 29;
                    else if(buffer.substring(0, buffer.length() - 1).equals("REAL") && !isUpperChar(atual)){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.PCReal);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 41: // atual for A
                    if(atual == 'L')
                        estado = 40;
                    else if(atual == 'O')
                        estado = 33;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 42: // atual for Q
                    if(atual == 'T')
                        estado = 37;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 43: // atual for #
                    if(atual != '\n')
                        continue;
                    else {
                        buffer = "";
                        estado = 0;
                    }
                    break;

                case 44: //atual for S
                    if(atual == 'E')
                        estado = 29;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 45:
                    if(atual == 'I')
                        estado = 35;
                    else
                        throw Expection.palavraInvalida(buffer, linha);
                    break;

                case 46:
                    if(buffer.substring(0, buffer.length() - 1).equals("OU") && !isUpperChar(atual)){
                        backtracking();
                        token = new Token();
                        token.setTipo(TipoToken.OpBoolOu);
                        token.setTxt(buffer.substring(0, buffer.length() - 1));
                        return token;
                    }else
                        throw Expection.operadorInvalido(buffer, linha);
            }
            if(atual == '\n')
                linha++;
        }
        if(parentese)
            throw Expection.faltaParenteses();
        if(aspas)
            throw Expection.faltaAspas();
        return null;
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }
    private boolean isUpperChar(char c){
        return (c >= 'A' && c <= 'Z');
    }
    private boolean isLowerChar(char c){
        return (c >= 'a' && c <= 'z') ;
    }
    private boolean isOperator(char c){
        return c == '>' || c == '<' || c == '=' || c == '!' ||
               c == '+' || c == '-' || c == '*' || c == '/' ||
               c == ':' || c == '(' || c == ')' || c == '"' || c == '#';
    }
    private boolean isSpace(char c){
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
    private char nextChar(){
        char cont = conteudo[pos];
        pos++;
        return cont;
    }
    private boolean isEOF(){
        return pos == conteudo.length;
    }
    private void backtracking(){ pos--; }
}
