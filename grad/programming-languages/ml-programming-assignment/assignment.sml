Control.Print.printDepth := 100;
Control.Print.printLength := 100;

(* Part 1 *)
fun merge [] L2 = L2
  | merge L1 [] = L1
  | merge (x::xs) (y::ys) = if x < y then x :: merge xs (y::ys) else y :: merge
  (x::xs) ys

(* Part 2 *)
fun split [] = ([], [])
  | split [x] = ([x], [])
  | split (x1::x2::xs) =
  let
    val (xs1, xs2) = split xs
  in
    ((x1::xs1), (x2::xs2))
  end

(* Part 3 *)
fun mergeSort [] = []
  | mergeSort [x] = [x]
  | mergeSort L = 
  let
    val (xs1, xs2) = split L
  in
    merge (mergeSort xs1) (mergeSort xs2)
  end

(* Part 4 *)
fun sort (op <) [] = []
  | sort (op <) [x] = [x]
  | sort (op <) L =
  let 
    fun merge [] L2 = L2
      | merge L1 [] = L1
      | merge (x::xs) (y::ys) = if x < y then x :: merge xs (y::ys) else y ::
      merge (x::xs) ys

    fun split [] = ([], [])
      | split [x] = ([x], [])
      | split (x1::x2::xs) =
      let
        val (xs1, xs2) = split xs
      in
        ((x1::xs1), (x2::xs2))
      end

    val (xs1, xs2) = split L
  in
    merge (sort (op <) xs1) (sort (op <) xs2)
  end

(* Part 5 *)
datatype 'a tree = empty | leaf of 'a | node of 'a * 'a tree * 'a tree

(* Part 6 *)
fun labels empty = []
  | labels (leaf a) = [a]
  | labels (node (a, L, R)) = labels L @ a :: labels R

(* Part 7 *)
infix ==
fun replace (op ==) x y empty = empty
  | replace (op ==) x y (leaf a) = if a == x then leaf y else leaf a
  | replace (op ==) x y (node (a, L, R)) =
  let
    val newLabel = if a == x then y else a
  in
    (node (newLabel, replace (op ==) x y L, replace (op ==) x y R)) 
  end

(* Part 8 *)
fun replaceEmpty y empty = y
  | replaceEmpty y (leaf a) = leaf a
  | replaceEmpty y (node (a, L, R)) = (node (a, replaceEmpty y L,
  replaceEmpty y R))

(* Part 9 *)
fun mapTree f empty = f empty
  | mapTree f (leaf a) = f (leaf a)
  | mapTree f (node (a, L, R)) = f (node (a, mapTree f
  L, mapTree f R)) 

(* Part 10 *)
fun sortTree (op <) T = mapTree (fn empty => empty | (leaf a) => (leaf (sort (op
  <) a)) | (node (a, L, R)) => (node (sort (op <) a, L, R))) T
