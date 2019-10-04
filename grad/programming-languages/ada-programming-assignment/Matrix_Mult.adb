package body Matrix_Mult is

    procedure Mat_Mult(A: in Mat; B: in Mat; C: out Mat) is
    task type Dot_Product is
        entry Dot(Row_Index: Integer; Col_Index: Integer);
        entry Dot_Done;
    end Dot_Product;

    task body Dot_Product is
        Row: Integer;
        Col: Integer;
    begin
        accept Dot(Row_Index: Integer; Col_Index: Integer) do
            Row := Row_Index;
            Col := Col_Index;
        end Dot;

        C(Row, Col) := 0;
        for i in 1..Size loop
            C(Row, Col) := C(Row, Col) + A(Row, i)*B(i, Col); 
        end loop;

        accept Dot_Done do
            null;
        end Dot_Done;
    end Dot_Product;

    Tasks_Mat: array(1..Size, 1..Size) of Dot_Product;

    begin
        for i in 1..Size loop
            for j in 1..Size loop
                Tasks_Mat(i, j).Dot(i, j);
            end loop;
        end loop;

        for i in 1..Size loop
            for j in 1..Size loop
                Tasks_Mat(i, j).Dot_Done;
            end loop;
        end loop;
    end Mat_Mult;
end Matrix_Mult;
