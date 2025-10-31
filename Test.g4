grammar Test;


// Parser
start : statement* EOF ;

statement    :  expr;

expr    :  expr '*' expr
        |  expr '/' expr
        |  expr '+' expr
        |  expr '-' expr
        |  expr '==' expr
        |  expr '!=' expr
        |  expr '>' expr
        |  expr '<' expr
        |  VARIABLE ':=' expr
        |  ID
        |  NUMBER
        |  STRING
        |  '(' expr ')'
;



// Lexer
ID      :  [a-z][a-zA-Z0-9]* ;
NUMBER  :  [0-9]+ ;
STRING  :  '"' (~[\n\r"])* '"' ;
VARIABLE:  [a-zA-Z_][a-zA-Z0-9_]* ;

KOMMENTAR       :  '#' ~[\n\r]* -> skip ;
WS              :  [ \t\n]+ -> skip ;