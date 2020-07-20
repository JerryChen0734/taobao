import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class test {

    @Test
   public void test1(){
        for (int i = 0; i < 10; i++) {
            System.out.println("盐:"+BCrypt.gensalt());
            System.out.println("123加密后:"+BCrypt.hashpw("123",BCrypt.gensalt()));
            System.out.println();
        }
    }
}
