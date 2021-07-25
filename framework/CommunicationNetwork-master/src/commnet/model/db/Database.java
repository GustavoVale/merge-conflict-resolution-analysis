package commnet.model.db;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.cj.jdbc.MysqlDataSource;

import commnet.util.Logger;

public final class Database {

	private static final MysqlDataSource ds = new MysqlDataSource();

	/**
	 * Initialize connection with the database. It needs JDBC url, user and password
	 * 
	 * @param dbUrl
	 * @param dbuser
	 * @param dbpass
	 * @throws IOException
	 * @throws SQLException
	 * @throws PropertyVetoException
	 */
	private static void initDataSource(String dbUrl, String dbuser, String dbpass)
			throws IOException, SQLException, PropertyVetoException {
		ds.setUser(dbuser);
		ds.setPassword(dbpass);
		ds.setURL(dbUrl);
	}

	/** 
	 * Get connection in the database
	 * @return required variables JDBC url, user and password
	 */
	public static Connection getConnection() {
		Thread currentThread = Thread.currentThread();
		ClassLoader classloader = currentThread.getContextClassLoader();
		InputStream input = classloader.getResourceAsStream("db.properties");
		Properties prop = new Properties();
		try {
			if (input != null) {
				prop.load(input);
				input.close();
			} else {
				Logger.logStackTrace(new FileNotFoundException("The file 'db.properties was not found."));
			}
		} catch (IOException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
		String url = prop.getProperty("database.url");
		String user = prop.getProperty("database.user");
		String pass = prop.getProperty("database.password");
		return getConnection(url, user, pass);
	}

	/**
	 * Get connection with the database passing the three required parameters. 
	 * @param dbUrl
	 * @param dbuser
	 * @param dbpass
	 * @return Connection
	 */
	private static Connection getConnection(String dbUrl, String dbuser, String dbpass) {
		try {
			initDataSource(dbUrl, dbuser, dbpass);
			Connection connection = ds.getConnection();
			connection.setAutoCommit(false);
			connection.commit();
			return connection;
		} catch (SQLException | IOException | PropertyVetoException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
	}

}
