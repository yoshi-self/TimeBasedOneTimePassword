import com.yoshi_self.TimeBasedOneTimePassword;

public class Main {
    public static void main(String args[]) {
        try {
            TimeBasedOneTimePassword totp = new TimeBasedOneTimePassword(args[0]);
            String result = totp.calcOneTimePassword();
            System.out.println(result);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
