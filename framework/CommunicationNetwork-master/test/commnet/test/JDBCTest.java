package commnet.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCTest {

	public static void main(String[] args) throws Throwable {
		String databaseUrl = "jdbc:mysql://localhost:3306/ContNet?serverTimezone=UTC&useSSL=false";
		String user = "root";
		String password = "root";
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection(databaseUrl, user, password);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM developers LIMIT 1");
		System.out.println(rs.next());
		rs.close();
		con.close();
	}
}
