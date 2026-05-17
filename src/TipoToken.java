//variaveis pré definidas para não gerar conflito de identificadores
public enum TipoToken {
    PCDec, PCProg, PCInt, PCReal, PCLer, PCImprimir, PCSe, PCEntao, PCSenao, PCEnqto, PCIni, PCFim,

    OpAritMult, OpAritDiv, OpAritSoma, OpAritSub,

    OpRelMenor, OpRelMenorIgual, OpRelMaior, OpRelMaiorIgual, OpRelIgual, OpRelDif,

    OpBoolE, OpBoolOu,

    Delim, 

    Atrib,

    AbrePar, FechaPar,

    Var,

    NumInt,

    NumReal,

    Cadeia
}