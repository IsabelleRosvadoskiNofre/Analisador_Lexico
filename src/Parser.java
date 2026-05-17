public class Parser {
    private Leitor scanner;
    private Token tk;

    public Parser(Leitor scanner){
        this.scanner = scanner;
    }

    //Programa → ':' 'DEC' ListaDeclaracoes ':' 'PROG' ListaComandos;
    public void Programa() {
        tk = scanner.nextToken();
        if (tk.getTipo() != TipoToken.Delim){
            throw Expection.ErroSintatico(":", tk.getTxt());
        }
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCDec){
            throw Expection.ErroSintatico("DEC", tk.getTxt());
        }
        ListaDeclaracoes();
        //Se retornou de ListaDeclarações() então é :PROG
        //tk = scanner.nextToken();
        //if(tk.getTipo() != TipoToken.Delim){
        //    throw Expection.ErroSintatico(":", tk.getTxt());
        //}
        //tk = scanner.nextToken();
        //if(tk.getTipo() != TipoToken.PCProg){
        //    throw Expection.ErroSintatico("PROG", tk.getTxt());
        //}
        ListaComandos();
    }

    //ListaDeclaracoes → Declaracao ListaDeclaracoes’;
    public void ListaDeclaracoes(){
        Declaracao();
        ListaDeclaracoesLinha(); //ListaDeclaracoes'
    }

    //ListaDeclaracoes’ → ListaDeclaracoes | lambda;
    public void ListaDeclaracoesLinha(){
        Leitor scanner_atual = new Leitor(scanner);
        tk = scanner.nextToken();
        if (tk.getTipo() == TipoToken.Delim){
            tk = scanner.nextToken();
            if (tk.getTipo() == TipoToken.PCProg){
                return;
            }
        }
        if(tk.getTipo() != null) {
            scanner = scanner_atual;
            ListaDeclaracoes();
        }
    }

    //ListaComandos → Comando ListaComandos’;
    public void ListaComandos() {
        Comando();
        ListaComandosLinha();
    }

    //ListaComandos’ →  ListaComandos | lambda;
    public void ListaComandosLinha(){
        Leitor scanner_atual = new Leitor(scanner);
        tk = scanner.nextToken();
        scanner = scanner_atual;
        if(tk.getTipo() != null){
            ListaComandos();
        }
    }

    //Declaracao → VARIAVEL ':' TipoVar;
    public void Declaracao() {
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Var){
            throw Expection.ErroSintatico("VARIAVEL", tk.getTxt());
        }
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Delim){
            throw Expection.ErroSintatico(":", tk.getTxt());
        }
        TipoVar();
    }

    //TipoVar → 'INT' | 'REAL';
    public void TipoVar(){
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCInt && tk.getTipo() != TipoToken.PCReal){
            throw Expection.ErroSintatico("INT / REAL", tk.getTxt());
        }
    }

    //ExpressaoAritmetica → TermoAritmetico ExpressaoAritmetica’
    public void ExpressaoAritmetica(){
        TermoAritmetico();
        ExpressaoAritmeticaLinha();
    }

    //ExpressaoAritmetica’ → ‘+’ TermoAritmetico ExpressaoAritmetica’ | ‘-’ TermoAritmetico ExpressaoAritmetica’ | lambda;
    public void ExpressaoAritmeticaLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() != null){
            if(tk.getTipo() != TipoToken.OpAritSoma && tk.getTipo() != TipoToken.OpAritSub){
                throw Expection.ErroSintatico("+ / -", tk.getTxt());
            }
            TermoAritmetico();
            ExpressaoAritmeticaLinha();
        }
    }

    //TermoAritmetico → FatorAritmetico TermoAritmetico’
    public void TermoAritmetico(){
        FatorAritmetico();
        TermoAritmeticoLinha();
    }

    //TermoAritmetico’ →  ‘*’ FatorAritmetico TermoAritmetico’ | ‘/’ FatorAritmetico TermoAritmetico’ | lambda;
    public void TermoAritmeticoLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() != null){
            if(tk.getTipo() != TipoToken.OpAritMult && tk.getTipo() != TipoToken.OpAritDiv){
                throw Expection.ErroSintatico("* / /", tk.getTxt());
            }
            FatorAritmetico();
            TermoAritmeticoLinha();
        }
    }

    //FatorAritmetico → NUMINT| NUMREAL | VARIAVEL | '(' ExpressaoAritmetica ')'
    public void FatorAritmetico(){
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.NumInt && tk.getTipo() != TipoToken.NumReal && tk.getTipo() != TipoToken.Var && tk.getTipo() != TipoToken.AbrePar){
            throw Expection.ErroSintatico("NumInt / NumReal / Variável / (", tk.getTxt());
        }
        if(tk.getTipo() == TipoToken.AbrePar) {
            ExpressaoAritmetica();
            tk = scanner.nextToken();
            if(tk.getTipo() != TipoToken.FechaPar) {
                throw Expection.ErroSintatico(")", tk.getTxt());
            }
        }//else{
        //    throw Expection.ErroSintatico("(", tk.getTxt());
        //}
    }

    //ExpressaoRelacional → TermoRelacional ExpressaoRelacional’
    public void ExpressaoRelacional(){
        TermoRelacional();
        ExpressaoRelacionalLinha();
    }

    //ExpressaoRelacional’ →  OperadorBooleano TermoRelacional ExpressaoRelacional’ | lambda;
    public void ExpressaoRelacionalLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() != null){
            OperadorBooleano();
            TermoRelacional();
            ExpressaoRelacionalLinha();
        }
    }

    //TermoRelacional → FatorAritmetico TermoRelacional’
    public void TermoRelacional(){
        FatorAritmetico();
        TermoRelacionalLinha();
    }

    //TermoRelacional’ → OP_REL ExpressaoAritmetica | lambda
    public void TermoRelacionalLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() != null){
            OP_REL();
            ExpressaoAritmetica();
        }
    }

    public void OP_REL(){
        if(tk.getTipo() != TipoToken.OpRelMenor &&
           tk.getTipo() != TipoToken.OpRelMenorIgual &&
           tk.getTipo() != TipoToken.OpRelMaior &&
           tk.getTipo() != TipoToken.OpRelMaiorIgual &&
           tk.getTipo() != TipoToken.OpRelIgual &&
           tk.getTipo() != TipoToken.OpRelDif){
            throw Expection.ErroSintatico("OP_REL", tk.getTxt());
        }
    }

    //OperadorBooleano → 'E' | 'OU';
    public void OperadorBooleano(){
        if(tk.getTipo() != TipoToken.OpBoolE && tk.getTipo() != TipoToken.OpBoolOu){
            throw Expection.ErroSintatico("E / OU", tk.getTxt());
        }
    }

    //Comando → ComandoAtribuicao | ComandoEntrada | ComandoSaida | ComandoCondicao | ComandoRepeticao | SubAlgoritmo;
    public void Comando(){
        //Leitor scanner_atual = new Leitor(scanner);
        tk = scanner.nextToken();
        //scanner = scanner_atual; //To-do: mudar isso, repetindo a mesma lógica várias vezes

        if (tk.getTipo() == TipoToken.Var){
            ComandoAtribuicao();
        }else if (tk.getTipo() == TipoToken.PCLer){
            ComandoEntrada();
        }else if(tk.getTipo() == TipoToken.PCImprimir){
            ComandoSaida();
        }else if(tk.getTipo() == TipoToken.PCSe){
            ComandoCondicao();
        }else if(tk.getTipo() == TipoToken.PCEnqto){
            ComandoRepeticao();
        }else if(tk.getTipo() == TipoToken.PCIni){
            SubAlgoritmo();
        }
    }

    //ComandoAtribuicao → VARIAVEL ':=' ExpressaoAritmetica;
    public void ComandoAtribuicao(){
        //tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Var){
            throw Expection.ErroSintatico("VARIAVEL", tk.getTxt());
        }
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Atrib){
            throw Expection.ErroSintatico(":=", tk.getTxt());
        }
        ExpressaoAritmetica();
    }

    //ComandoEntrada → 'LER' VARIAVEL;
    public void ComandoEntrada(){
        //tk = scanner.nextToken(); //ele já chega em LER
        if(tk.getTipo() != TipoToken.PCLer){
            throw Expection.ErroSintatico("LER", tk.getTxt());
        }//To-Do: tirar, já foi verificado antes de vir
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Var){
            throw Expection.ErroSintatico("VARIAVEL", tk.getTxt());
        }
    }

    //ComandoSaida → 'IMPRIMIR' ComandoSaida’;
    public void ComandoSaida(){
        //tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCImprimir){
            throw Expection.ErroSintatico("IMPRIMIR", tk.getTxt());
        }
        ComandoSaidaLinha();
    }

    //ComandoSaida’ → VARIAVEL | CADEIA;
    public void ComandoSaidaLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.Var && tk.getTipo() != TipoToken.Cadeia){
            throw Expection.ErroSintatico("VARIAVEL / CADEIA", tk.getTxt());
        }
    }

    //ComandoCondicao → 'SE' ExpressaoRelacional 'ENTAO' Comando ComandoCondicao’
    public void ComandoCondicao(){
        //tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCSe){
            throw Expection.ErroSintatico("SE", tk.getTxt());
        }
        ExpressaoRelacional();
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCEntao){
            throw Expection.ErroSintatico("ENTAO", tk.getTxt());
        }
        Comando();
        ComandoCondicaoLinha();
    }

    //ComandoCondicao’ → ‘SENAO’ Comando | lambda;
    public void ComandoCondicaoLinha(){
        tk = scanner.nextToken();
        if(tk.getTipo() == TipoToken.PCSenao){
            Comando();
        }
    }

    //ComandoRepeticao → 'ENQTO' ExpressaoRelacional Comando;
    public void ComandoRepeticao(){
        //tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCEnqto){
            throw Expection.ErroSintatico("ENQTO", tk.getTxt());
        }
        ExpressaoRelacional();
        Comando();
    }

    //SubAlgoritmo → 'INI' ListaComandos 'FIM';
    public void SubAlgoritmo(){
        //tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCIni){
            throw Expection.ErroSintatico("INI", tk.getTxt());
        }
        ListaComandos();
        tk = scanner.nextToken();
        if(tk.getTipo() != TipoToken.PCFim){
            throw Expection.ErroSintatico("FIM", tk.getTxt());
        }
    }
}