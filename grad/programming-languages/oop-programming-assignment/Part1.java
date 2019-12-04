import java.io.*;
import java.util.*;

public class Part1 {
    public static void main(String[] args) {
        test();
    }

    static <E extends Comparable<E>> void addToCList(E z, ComparableList<E> L) {
        L.add(z);        
    }

    static void test() {
	    ComparableList<A> c1 = new ComparableList<A>();
	    ComparableList<A> c2 = new ComparableList<A>();
	    for (int i = 0; i < 10; i++) {
	        addToCList(new A(i), c1);
	        addToCList(new A(i), c2);
	    }
	
	    addToCList(new A(12), c1);
	    addToCList(new B(6,6), c2);
	
	    addToCList(new B(7,11), c1);
	    addToCList(new A(13), c2);

	    System.out.print("c1: ");
	    System.out.println(c1);
	
	    System.out.print("c2: ");
	    System.out.println(c2);

	    switch (c1.compareTo(c2)) {
	    case -1: 
	        System.out.println("c1 < c2");
	        break;
	    case 0:
	        System.out.println("c1 = c2");
	        break;
	    case 1:
	        System.out.println("c1 > c2");
	        break;
	    default:
	        System.out.println("Uh Oh");
	        break;
	    }

    }
}

class ComparableList<E extends Comparable<E>> extends ArrayList<E> implements Comparable<ComparableList<E>> {
    public int compareTo(ComparableList<E> L) {
        Iterator<E> iterOne = this.iterator();
        Iterator<E> iterTwo = L.iterator();

        int result = 0;
        while (iterOne.hasNext() && iterTwo.hasNext()) {
            result = iterOne.next().compareTo(iterTwo.next());

            if (result == 0)
                continue;

            return result;
        }

        int sizeOne = this.size();
        int sizeTwo = L.size();

        if (sizeOne < sizeTwo)
            result = -1;
        else if (sizeOne > sizeTwo)
            result = 1;

        return result;
    }
}

class A implements Comparable<A> {
    Integer x;

    public A(Integer x) {
        this.x = x;    
    }

    public Integer getSum() {
        return this.x;
    }

    public int compareTo(A a) {
        return this.getSum().compareTo(a.getSum());
    }

    public String toString() {
        String result = "A<" + this.x + ">";
        return result;
    }
}

class B extends A {
    Integer y;

    public B(Integer x, Integer y) {
        super(x);
        this.y = y;
    }

    public Integer getSum() {
        int sum = this.x + this.y;
        return sum;
    }

    public String toString() {
        String result = "B<" + this.x + "," + this.y + ">";
        return result;
    }
}
