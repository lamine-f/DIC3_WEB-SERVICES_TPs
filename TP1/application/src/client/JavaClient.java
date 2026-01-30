import java.util.*;
import org.apache.xmlrpc.*;

public class JavaClient {

    static void test (XmlRpcClient server, Vector params) throws Exception {
        Object result = server.execute("sample.multiply", params);
        int sum = ((Integer) result).intValue();
        System.out.println("The multiplication is: " + sum);
    }

    public static void main(String[] args) {
        try {
            XmlRpcClient server = new XmlRpcClient("http://localhost/RPC2");
            Vector params = new Vector();
            params.addElement(new Integer(17));
            params.addElement(new Integer(13));
            Object result = server.execute("sample.sum", params);
            int sum = ((Integer) result).intValue();
            System.out.println("The sum is: " + sum);
            //test(server, params);
        } catch (Exception exception) {
            System.err.println("JavaClient: " + exception);
        }
    }
}
