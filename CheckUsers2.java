import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckUsers2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://q39zw9.h.filess.io:5432/Proyecto_Global_pickstrip?sslmode=disable";
        String user = "Proyecto_Global_pickstrip";
        String pass = "cbdbe8081bedc46ee39a2f7801c52ca3cff83c49";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT id, email, rol_id, substr(password,1,30) as pwd FROM proyecto_global.usuarios ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getLong("id") + " | " + rs.getString("email") + " | rol_id=" + rs.getObject("rol_id") + " | " + rs.getString("pwd"));
                    }
                }
            }
        }
    }
}
