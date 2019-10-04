package Matrix_Mult is
    Size: Integer := 10;
    type Mat is array(1..Size, 1..Size) of Integer;
    procedure Mat_Mult(A: in Mat; B: in Mat; C: out Mat);
end Matrix_Mult;
