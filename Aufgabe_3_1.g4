grammar Aufgabe_3_1;


// Parser
start : statement* EOF ;

statement    :  expression
             |  while
             |  ifelse
;
expression  :
            |  '(' expression ')'
            | operator
            | variable
            |  BEZEICHNER
            |  INTEGER
            |  STRING
operator :
         |  operator '*' operator
         |  operator '/' operator
         |  operator '+' operator
         |  operator '-' operator
         |  operator '==' operator
         |  operator '!=' operator
         |  operator '>' operator
         |  operator '<' operator
         ;

variable : BEZEICHNER (':=' expression)?;

while : ID;

ifelse : ID;

// Lexer
BEZEICHNER      :  [a-zA-Z_][a-zA-Z0-9_]* ;
INTEGER:  [0-9]+ ;
STRING: ;

KOMMENTAR :  '#' ~[\n\r]* -> skip ;
WS      : [ \t\n]+ -> skip ;