import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        // Test password
        String password = "Admin@123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash (strength 12):");
        System.out.println(hash);
        System.out.println();
        System.out.println("Verify matches: " + encoder.matches(password, hash));
        
        // Second password
        password = "User@123";
        hash = encoder.encode(password);
        System.out.println();
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash (strength 12):");
        System.out.println(hash);
    }
}
