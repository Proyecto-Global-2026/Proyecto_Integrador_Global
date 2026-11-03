import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class EnableUser3 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://q39zw9.h.filess.io:5432/Proyecto_Global_pickstrip?sslmode=disable";
        String user = "Proyecto_Global_pickstrip";
        String pass = "cbdbe8081bedc46ee39a2f7801c52ca3cff83c49";
        Connection conn = DriverManager.getConnection(url, user, pass);
        Statement st = conn.createStatement();
        int updated = st.executeUpdate("UPDATE proyecto_global.usuarios SET habilitado = true WHERE email = 'hola@gmail.com'");
        System.out.println("Updated rows: " + updated);
        st.close();
        conn.close();
    }
}
