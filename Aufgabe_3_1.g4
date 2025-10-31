grammar Aufgabe_3_1;



// Parser
start : stmt* EOF ;

stmt : expr
     | var
     | while
     | ifelse
;

var : ID ;

while   :  'while' expr 'do'  stmt+  'end' ;

ifelse : 'if' expr 'do'  stmt 'else do' stmt 'end' ;

expr    :  expr '*' expr
        |  expr '/' expr
        |  expr '+' expr
        |  expr '-' expr
        |  expr '==' expr
        |  expr '!=' expr
        |  expr '>' expr
        |  expr '<' expr
        |  expr '>=' expr
        |  expr '=<' expr
        |  var ':=' expr
        |  ID
        |  NUMBER
        |  STRING
        |  '(' expr ')'
;

// Lexer

ID      :  [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER  :  [0-9]+ ;
STRING  :  '"' (~[\n\r"])* '"' ;

KOMMENTAR       :  '#' ~[\n\r]* -> skip ;
WS              :  [ \t\n]+ -> skip ;
