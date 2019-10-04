with Text_IO;
with Ada.Integer_Text_IO;
with Matrix_Mult;

use Text_IO;
use Ada.Integer_Text_IO;
use Matrix_Mult;

procedure Assignment_Main is
    A: Mat;
    B: Mat;
    C: Mat;

    task type Read_Mat is
        entry Read(M: out Mat);
    end Read_Mat; 

    task body Read_Mat is
    begin
        accept Read(M: out Mat) do
            for i in 1..Size loop
                for j in 1..Size loop
                    Ada.Integer_Text_IO.Get(M(i, j));
                end loop;
            end loop;
        end Read;
    end Read_Mat;

    task Print is
        entry Mat_Multiplied(C: in Mat);
    end Print;

    task body Print is
    begin
        accept Mat_Multiplied(C: in Mat) do
            for i in 1..Size loop
                for j in 1..Size loop
                    Put(C(i, j), width => 10);
                end loop;
                New_Line;
            end loop;
        end Mat_Multiplied;
    end Print;

    Read_One: Read_Mat;
    Read_Two: Read_Mat;

begin
    Read_One.Read(A);
    Read_Two.Read(B);
    Matrix_Mult.Mat_Mult(A, B, C);
    Print.Mat_Multiplied(C);    
end Assignment_main;
