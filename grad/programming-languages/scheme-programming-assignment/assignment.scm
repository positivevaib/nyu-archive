; Part 1
; Base case: If L is empty, return 0.
; Assumption: (count-numbers M) returns the count of numbers in any list M smaller than L (including (car L) and (cdr L)).
; Step: If (car L) is a list, then return the sum of the count of numbers in lists (car L) and (cdr L).
;       If (car L) is a number, then return the count of numbers in list (cdr L) plus 1.
;       If (car L) is neither a list nor a number (for instance, a literal 'a'), then return the count of numbers in list (cdr L).

(define (count-numbers L) 
  (cond ((null? L) 0) 
        ((list? (car L)) (+ (count-numbers (car L)) (count-numbers (cdr L)))) 
        ((number? (car L)) (+ 1 (count-numbers (cdr L)))) 
        (else (count-numbers (cdr L)))))

; Part 2
; Base case: If L is empty, return the list with x as the only element.
; Assumption: (insert x M) returns a list with x in the correct position for any list M smaller than L (including (cdr L)).
; Step: If x is smaller than or equal to (car L), then return the list L, with x appended as the first element.
;       If x is greater than (car L), then return a list with (car L) as the first element and with x in the correct position in (cdr L).

(define (insert x L)
  (cond ((null? L) (list x))
        ((>= (car L) x) (cons x L))
        (else (cons (car L) (insert x (cdr L))))))

; Part 3
; Base case: If L is empty, return M.
; Assumption: (insert-all N M) returns a sorted list with elements of N and M for any list N smaller than M (including (cdr L)).
; Step: Return a sorted list with elements of (cdr L) and M', where M' is a sorted list with the elements of M, indluding (car L).

(define (insert-all L M)
  (cond ((null? L) M)
        (else (insert-all (cdr L) (insert (car L) M)))))

; Part 4
; The logic is same as in parts 2 and 3, which are now nested in (sort L).

(define (sort L)
  (letrec ((insert (lambda (x L)
                     (cond ((null? L) (list x))          
                           ((>= (car L) x) (cons x L))         
                           (else (cons (car L) (insert x (cdr L)))))))
           (insert-all (lambda (L M)           
                         (cond ((null? L) M)                 
                               (else (insert-all (cdr L) (insert (car L) M)))))))
    (insert-all (cdr L) (list (car L)))))

; Part 5

(define (translate op)
  (cond ((eq? op '+) +)
        ((eq? op '-) -)
        ((eq? op '*) *)
        ((eq? op '/) /)))

; Part 6
; Base case: If exp is a number, return exp.
; Assumption: (postfix-eval subexp) returns the evaluated value of a subexp smaller than exp (including (car exp) and (cdr exp)).
; Step: Return the evaluated value of (car exp) and (car (cdr exp)) using the postfix operator in exp.

(define (postfix-eval exp)
  (cond ((number? exp) exp)
        (else ((translate (car (cdr (cdr exp)))) (postfix-eval (car exp)) (postfix-eval (car (cdr exp)))))))

; Part 7
; Base case: L is empty, return the set containing the empty set, i.e. '(()).
; Assumption: (powerset M) returns the powerset of M, for any set M smaller than L (including (cdr L)).
; Step: Return the list that includes all the elements of the powerset of (cdr L) and the elements of another powerset of (cdr L) with (car L) as the first element of each 
;       list in that second powerset.

(define (powerset L)
  (cond ((null? L) '(()))
        (else (append (map (lambda (y) (cons (car L) y)) (powerset (cdr L))) (powerset (cdr L)))))) 
