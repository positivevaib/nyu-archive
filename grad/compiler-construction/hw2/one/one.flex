%%

%class One
%public
%{
    String name;
%}
%standalone
%unicode

%%

"setname " [a-zA-Z]+            { name = yytext().substring(8); }
"name"                          { System.out.print(name); }
