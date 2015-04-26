import org.mindrot.jbcrypt.BCrypt;

public class GenPasskey {
	public static void main(String[] args) {
		if(args.length > 0) {
			System.out.println(BCrypt.hashpw(args[0], BCrypt.gensalt(4)));
		}
	}
}
