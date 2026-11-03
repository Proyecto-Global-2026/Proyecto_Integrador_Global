import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckUsers {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://q39zw9.h.filess.io:5432/Proyecto_Global_pickstrip?sslmode=disable";
        String user = "Proyecto_Global_pickstrip";
        String pass = "cbdbe8081bedc46ee39a2f7801c52ca3cff83c49";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT id, email, enabled, rol_id, substr(password,1,20) as pwd_prefix FROM proyecto_global.usuarios ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getLong("id") + " | " + rs.getString("email") + " | enabled=" + rs.getBoolean("enabled") + " | rol_id=" + rs.getObject("rol_id") + " | " + rs.getString("pwd_prefix"));
                }
            }
        }
    }
}
