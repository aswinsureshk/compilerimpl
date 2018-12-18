package cop5556fa18;

public class MathRunTime {


	public static String className = "cop5556fa18/MathRunTime";
	
	//add function
	public static String addstrSignature = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
	
	public static String addstr(String arg1, String arg2) {
		return (String) arg1 + arg2;
	}
	
	// sin function
	public static String sinSignature = "(F)F";
	
	public static float sin(float arg) {
		return (float) Math.sin((double) arg);
	}
	
	// cos function
	public static String cosSignature = "(F)F";
	
	public static float cos(float arg) {
		return (float) Math.cos((double) arg);
	}
	
	// atan function
	public static String atanSignature = "(F)F";
	
	public static float atan(float arg) {
		return (float) Math.atan((double) arg);
	}
	
	// log function
	public static String logSignature = "(F)F";
	
	public static float log(float arg) {
		return (float) Math.log((double) arg);
	}
	
	// abs function	- Integer
	public static String absISignature = "(I)I";
	
	public static int absI(int arg) {
		return Math.abs(arg);
	}
	
	// abs function	- Float
	public static String absFSignature = "(F)F";
		
	public static float absF(float arg) {
		return Math.abs(arg);
	}
	
	// power function
	public static String powSignature = "(FF)F";
	
	public static float pow(float arg0, float arg1) {
		return (float)Math.pow((double)arg0, (double)arg1);
	}
	
	// sleep function
	public static String sleepSignature = "(L)V";
	
	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
