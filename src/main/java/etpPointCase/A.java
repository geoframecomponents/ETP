package etpPointCase;

public class A {
	private int a_number;
	public int getNumber() { return a_number; }
}
//class A {
//}
class B {
    private double b_number;
    public void setNumber(double num) { b_number = num; 
    System.out.println(b_number);
    }
	public double tryparam( ) {
		double denDelta = Math.pow((b_number), 2);
		return denDelta;  // -----> [mm/day]
	}
		
}