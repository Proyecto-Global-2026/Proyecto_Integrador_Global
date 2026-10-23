import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RunRetUpd {
	public static void main(String[] args) throws Exception {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://q39zw9.h.filess.io:5432/Proyecto_Global_pickstrip?currentSchema=proyecto_global";
		try (Connection c = DriverManager.getConnection(url, "Proyecto_Global_pickstrip",
				"cbdbe8081bedc46ee39a2f7801c52ca3cff83c49"); Statement s = c.createStatement()) {
			int u = s.executeUpdate(
					"UPDATE proyecto_global.usuarios SET activo=false, updated_at=NOW() - INTERVAL '5 days' WHERE id=(SELECT id FROM proyecto_global.usuarios WHERE activo=true AND anonimizado=false ORDER BY created_at ASC LIMIT 1)");
			System.out.println("usuarios:" + u);
			int a = s.executeUpdate(
					"UPDATE proyecto_global.archivos_planeacion SET created_at=NOW() - INTERVAL '5 days' WHERE id=(SELECT id FROM proyecto_global.archivos_planeacion WHERE eliminado_por_retencion=false ORDER BY created_at DESC LIMIT 1)");
			System.out.println("archivos:" + a);
			int r = s.executeUpdate(
					"UPDATE proyecto_global.revisiones_planeacion SET created_at=NOW() - INTERVAL '5 days' WHERE id=(SELECT id FROM proyecto_global.revisiones_planeacion ORDER BY created_at DESC LIMIT 1)");
			System.out.println("revisiones:" + r);
			System.out.println("Listo");
		}
	}
}
