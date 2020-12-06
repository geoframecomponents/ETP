package etpPointCase;

public class fakemain {
	public static void main(String[] args) {
	    A a = new A();
	    int blah = a.getNumber();
	    B b = new B();
	    b.setNumber(blah+5.7);
	    System.out.println(b);
	    double power = b.tryparam();
	    System.out.println(power);

	}

}
