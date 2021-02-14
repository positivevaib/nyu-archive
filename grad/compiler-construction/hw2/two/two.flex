%%

%class Two
%public
%{
    StringBuffer names = new StringBuffer();
    String newName = "";
%}
%standalone
%unicode

%state NAMES

%%

<YYINITIAL> {

"beginnames"                    { yybegin(NAMES); }
"names"                         { System.out.print(names); }

}

<NAMES> {

"endnames"                      { if (names.length() == 0) names.append(newName); else names.append(" and " + newName); yybegin(YYINITIAL); }
[a-zA-Z]+                       { if (names.length() == 0) names.append(newName); else names.append(", " + newName); newName = yytext(); }

}
