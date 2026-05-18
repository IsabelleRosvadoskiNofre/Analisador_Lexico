public class Parser {
    private Leitor scanner;
    private Token tk;

    public Parser(Leitor scanner){
        this.scanner = scanner;
    }

    // Programa -> ':' 'DEC' ListaDeclaracoes ':' 'PROG' ListaComandos
    public void Programa() {
        consumir(TipoToken.Delim, ":");
        consumir(TipoToken.PCDec, "DEC");
        ListaDeclaracoes();
        consumir(TipoToken.Delim, ":");
        consumir(TipoToken.PCProg, "PROG");
        ListaComandos();
        Token restante = olharProximoToken();

        if (restante != null) {
            throw erroSintatico("fim do arquivo", restante);
        }
    }

    // ListaDeclaracoes -> Declaracao ListaDeclaracoes'
    public void ListaDeclaracoes(){
        Declaracao();
        ListaDeclaracoesLinha();
    }

    // ListaDeclaracoes' -> ListaDeclaracoes | lambda
    public void ListaDeclaracoesLinha(){
        Token proximo = olharProximoToken();

        if (eh(proximo, TipoToken.Var)) {
            ListaDeclaracoes();
            return;
        }

        if (eh(proximo, TipoToken.Delim)) {
            return;
        }

        throw erroSintatico("VARIAVEL ou :", proximo);
    }

    // ListaComandos -> Comando ListaComandos'
    public void ListaComandos() {
        Comando();
        ListaComandosLinha();
    }

    // ListaComandos' -> ListaComandos | lambda
    public void ListaComandosLinha(){
        Token proximo = olharProximoToken();

        if (iniciaComando(proximo)) {
            ListaComandos();
            return;
        }

        if (proximo == null || eh(proximo, TipoToken.PCFim)) {
            return;
        }

        throw erroSintatico("COMANDO", proximo);
    }

    // Declaracao -> VARIAVEL ':' TipoVar
    public void Declaracao() {
        consumir(TipoToken.Var, "VARIAVEL");
        consumir(TipoToken.Delim, ":");
        TipoVar();
    }

    // TipoVar -> 'INT' | 'REAL'
    public void TipoVar(){
        tk = proximoToken();
        if(!eh(tk, TipoToken.PCInt) && !eh(tk, TipoToken.PCReal)){
            throw erroSintatico("INT / REAL", tk);
        }
    }

    // ExpressaoAritmetica -> TermoAritmetico ExpressaoAritmetica'
    public void ExpressaoAritmetica(){
        TermoAritmetico();
        ExpressaoAritmeticaLinha();
    }

    // ExpressaoAritmetica' -> '+' TermoAritmetico ExpressaoAritmetica'
    //                        | '-' TermoAritmetico ExpressaoAritmetica'
    //                        | lambda
    public void ExpressaoAritmeticaLinha() {
        Token proximo = olharProximoToken();

        if (eh(proximo, TipoToken.OpAritSoma) || eh(proximo, TipoToken.OpAritSub)) {
            proximoToken();
            TermoAritmetico();
            ExpressaoAritmeticaLinha();
            return;
        }

        if (estaNoFollowExpressaoAritmeticaLinha(proximo)) {
            return;
        }

        throw erroSintatico("expressao aritmetica'", proximo);
    }

    private boolean estaNoFollowExpressaoAritmeticaLinha(Token token) {
        if (token == null) {
            return true;
        }

        TipoToken tipo = token.getTipo();

        return tipo == TipoToken.OpRelMenor
                || tipo == TipoToken.OpRelMenorIgual
                || tipo == TipoToken.OpRelMaior
                || tipo == TipoToken.OpRelMaiorIgual
                || tipo == TipoToken.OpRelIgual
                || tipo == TipoToken.OpRelDif
                || tipo == TipoToken.FechaPar
                || tipo == TipoToken.OpBoolE
                || tipo == TipoToken.OpBoolOu
                || tipo == TipoToken.PCEntao
                || tipo == TipoToken.PCSenao
                || tipo == TipoToken.PCFim
                || iniciaComando(token);
    }

    // TermoAritmetico -> FatorAritmetico TermoAritmetico'
    public void TermoAritmetico(){
        FatorAritmetico();
        TermoAritmeticoLinha();
    }

    // TermoAritmetico' -> '*' FatorAritmetico TermoAritmetico'
    //                    | '/' FatorAritmetico TermoAritmetico'
    //                    | lambda
    public void TermoAritmeticoLinha() {
        Token proximo = olharProximoToken();

        if (eh(proximo, TipoToken.OpAritMult) || eh(proximo, TipoToken.OpAritDiv)) {
            proximoToken();
            FatorAritmetico();
            TermoAritmeticoLinha();
            return;
        }

        if (estaNoFollowTermoAritmeticoLinha(proximo)) {
            return;
        }

        throw erroSintatico("termo aritmetico'", proximo);
    }

    private boolean estaNoFollowTermoAritmeticoLinha(Token token) {
        return token == null
                || eh(token, TipoToken.OpAritSoma)
                || eh(token, TipoToken.OpAritSub)
                || estaNoFollowExpressaoAritmeticaLinha(token);
    }

    // FatorAritmetico -> NUMINT | NUMREAL | VARIAVEL | '(' ExpressaoAritmetica ')'
    public void FatorAritmetico(){
        tk = proximoToken();

        if (eh(tk, TipoToken.NumInt) || eh(tk, TipoToken.NumReal) || eh(tk, TipoToken.Var)) {
            return;
        }

        if(eh(tk, TipoToken.AbrePar)) {
            ExpressaoAritmetica();
            consumir(TipoToken.FechaPar, ")");
            return;
        }

        throw erroSintatico("NumInt / NumReal / VARIAVEL / (", tk);
    }

    // ExpressaoRelacional -> TermoRelacional ExpressaoRelacional'
    public void ExpressaoRelacional(){
        TermoRelacional();
        ExpressaoRelacionalLinha();
    }

    // Condicao -> '(' ExpressaoRelacional ')' | ExpressaoRelacional
    public void Condicao() {
        Token proximo = olharProximoToken();

        if (eh(proximo, TipoToken.AbrePar)) {
            consumir(TipoToken.AbrePar, "(");
            ExpressaoRelacional();
            consumir(TipoToken.FechaPar, ")");
            return;
        }

        ExpressaoRelacional();
    }

    // ExpressaoRelacional' -> OperadorBooleano TermoRelacional ExpressaoRelacional' | lambda
    public void ExpressaoRelacionalLinha() {
        Token proximo = olharProximoToken();

        if (eh(proximo, TipoToken.OpBoolE) || eh(proximo, TipoToken.OpBoolOu)) {
            proximoToken();
            TermoRelacional();
            ExpressaoRelacionalLinha();
            return;
        }

        if (estaNoFollowExpressaoRelacionalLinha(proximo)) {
            return;
        }

        throw erroSintatico("expressao relacional'", proximo);
    }

    private boolean estaNoFollowExpressaoRelacionalLinha(Token token) {
        return token == null
                || eh(token, TipoToken.PCEntao)
                || eh(token, TipoToken.FechaPar)
                || iniciaComando(token);
    }

    // TermoRelacional -> ExpressaoAritmetica TermoRelacional'
    public void TermoRelacional(){
        ExpressaoAritmetica();
        TermoRelacionalLinha();
    }

    // TermoRelacional' -> OP_REL ExpressaoAritmetica | lambda
    public void TermoRelacionalLinha(){
        Token proximo = olharProximoToken();

        if (ehOperadorRelacional(proximo)) {
            proximoToken();
            ExpressaoAritmetica();
            return;
        }

        if (estaNoFollowTermoRelacionalLinha(proximo)) {
            return;
        }

        throw erroSintatico("OP_REL", proximo);
    }

    private boolean estaNoFollowTermoRelacionalLinha(Token token) {
        return token == null
                || eh(token, TipoToken.OpBoolE)
                || eh(token, TipoToken.OpBoolOu)
                || estaNoFollowExpressaoRelacionalLinha(token);
    }

    public void OP_REL(){
        if(!ehOperadorRelacional(tk)){
            throw erroSintatico("OP_REL", tk);
        }
    }

    // OperadorBooleano -> 'E' | 'OU'
    public void OperadorBooleano(){
        if(!eh(tk, TipoToken.OpBoolE) && !eh(tk, TipoToken.OpBoolOu)){
            throw erroSintatico("E / OU", tk);
        }
    }

    // Comando -> ComandoAtribuicao | ComandoEntrada | ComandoSaida
    //          | ComandoCondicao | ComandoRepeticao | SubAlgoritmo
    public void Comando(){
        tk = proximoToken();

        if (eh(tk, TipoToken.Var)){
            ComandoAtribuicao();
        }else if (eh(tk, TipoToken.PCLer)){
            ComandoEntrada();
        }else if(eh(tk, TipoToken.PCImprimir)){
            ComandoSaida();
        }else if(eh(tk, TipoToken.PCSe)){
            ComandoCondicao();
        }else if(eh(tk, TipoToken.PCEnqto)){
            ComandoRepeticao();
        }else if(eh(tk, TipoToken.PCIni)){
            SubAlgoritmo();
        }else {
            throw erroSintatico("COMANDO", tk);
        }
    }

    // ComandoAtribuicao -> VARIAVEL ':=' ExpressaoAritmetica
    public void ComandoAtribuicao(){
        if(!eh(tk, TipoToken.Var)){
            throw erroSintatico("VARIAVEL", tk);
        }

        consumir(TipoToken.Atrib, ":=");
        ExpressaoAritmetica();
    }

    // ComandoEntrada -> 'LER' VARIAVEL
    public void ComandoEntrada(){
        if(!eh(tk, TipoToken.PCLer)){
            throw erroSintatico("LER", tk);
        }

        consumir(TipoToken.Var, "VARIAVEL");
    }

    // ComandoSaida -> 'IMPRIMIR' ComandoSaida'
    public void ComandoSaida(){
        if(!eh(tk, TipoToken.PCImprimir)){
            throw erroSintatico("IMPRIMIR", tk);
        }

        ComandoSaidaLinha();
    }

    // ComandoSaida' -> VARIAVEL | CADEIA
    public void ComandoSaidaLinha(){
        tk = proximoToken();

        if(!eh(tk, TipoToken.Var) && !eh(tk, TipoToken.Cadeia)){
            throw erroSintatico("VARIAVEL / CADEIA", tk);
        }
    }

    // ComandoCondicao -> 'SE' ExpressaoRelacional 'ENTAO' Comando ComandoCondicao'
    // ComandoCondicao -> 'SE' Condicao 'ENTAO' Comando ComandoCondicao'
    public void ComandoCondicao(){
        if(!eh(tk, TipoToken.PCSe)){
            throw erroSintatico("SE", tk);
        }

        Condicao();
        consumir(TipoToken.PCEntao, "ENTAO");
        Comando();
        ComandoCondicaoLinha();
    }

    // ComandoCondicao' -> 'SENAO' Comando | lambda
    public void ComandoCondicaoLinha(){
        Token proximo = olharProximoToken();

        if(eh(proximo, TipoToken.PCSenao)){
            proximoToken();
            Comando();
        }
    }

    // ComandoRepeticao -> 'ENQTO' ExpressaoRelacional Comando
// ComandoRepeticao -> 'ENQTO' Condicao Comando
    public void ComandoRepeticao(){
        if(!eh(tk, TipoToken.PCEnqto)){
            throw erroSintatico("ENQTO", tk);
        }

        Condicao();
        Comando();
    }
    // SubAlgoritmo -> 'INI' ListaComandos 'FIM'
    public void SubAlgoritmo(){
        if(!eh(tk, TipoToken.PCIni)){
            throw erroSintatico("INI", tk);
        }

        ListaComandos();
        consumir(TipoToken.PCFim, "FIM");
    }

    private Token proximoToken() {
        tk = scanner.nextToken();
        return tk;
    }

    private Token olharProximoToken() {
        Leitor.Estado estado = scanner.salvarEstado();
        Token token = scanner.nextToken();
        scanner.restaurarEstado(estado);
        return token;
    }

    private Token consumir(TipoToken tipo, String esperado) {
        Token token = proximoToken();

        if (!eh(token, tipo)) {
            throw erroSintatico(esperado, token);
        }

        return token;
    }

    private boolean eh(Token token, TipoToken tipo) {
        return token != null && token.getTipo() == tipo;
    }

    private boolean iniciaComando(Token token) {
        return eh(token, TipoToken.Var)
                || eh(token, TipoToken.PCLer)
                || eh(token, TipoToken.PCImprimir)
                || eh(token, TipoToken.PCSe)
                || eh(token, TipoToken.PCEnqto)
                || eh(token, TipoToken.PCIni);
    }

    private boolean ehOperadorRelacional(Token token) {
        return eh(token, TipoToken.OpRelMenor)
                || eh(token, TipoToken.OpRelMenorIgual)
                || eh(token, TipoToken.OpRelMaior)
                || eh(token, TipoToken.OpRelMaiorIgual)
                || eh(token, TipoToken.OpRelIgual)
                || eh(token, TipoToken.OpRelDif);
    }

    private Expection erroSintatico(String esperado, Token recebido) {
        String textoRecebido = recebido == null ? "fim do arquivo" : recebido.getTxt();
        return Expection.ErroSintatico(esperado, textoRecebido);
    }
}
