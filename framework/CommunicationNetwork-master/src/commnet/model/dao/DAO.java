package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public interface DAO<T> {

	/**
	 * Save Object in the database
	 * 
	 * @param object
	 * @return - true if it saves, false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	boolean save(T object) throws InvalidBeanException, SQLException;

	void delete(T object) throws InvalidBeanException, SQLException;

	List<T> list() throws InvalidBeanException, SQLException;

	/**
	 * Get Object in the database
	 * 
	 * @param object
	 * @return Object if it is there, null otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	T get(T object) throws InvalidBeanException, SQLException;

	List<T> search(T object) throws InvalidBeanException;

	void closeResources() throws SQLException;

	default String getFormattedDate(String gmtString) throws RuntimeException {
		gmtString = gmtString.replace(" GMT", "");
		String[] parts = gmtString.split(" ");
		String day = parts[0];
		String month = parts[1];
		String year = parts[2];
		String time = parts[3];

		int monthNumber = getMonthNumber(month);

		String formattedDate = year + "-" + monthNumber + "-" + day + " " + time;

		return formattedDate;
	}

	default int getMonthNumber(String month) {
		if (month.equals("Jan")) {
			return 1;
		}
		if (month.equals("Feb")) {
			return 2;
		}
		if (month.equals("Mar")) {
			return 3;
		}
		if (month.equals("Apr")) {
			return 4;
		}
		if (month.equals("May")) {
			return 5;
		}
		if (month.equals("Jun")) {
			return 6;
		}
		if (month.equals("Jul")) {
			return 7;
		}
		if (month.equals("Aug")) {
			return 8;
		}
		if (month.equals("Sep")) {
			return 9;
		}
		if (month.equals("Oct")) {
			return 10;
		}
		if (month.equals("Nov")) {
			return 11;
		}
		if (month.equals("Dec")) {
			return 12;
		}
		return 0;
	}

	default void deleteManyRowsFromSpecificTable(String tableName, List<Integer> IdDBList) throws SQLException {

		String currentString = "";
		for (Integer idDB : IdDBList) {

			if (currentString.equals("")) {
				currentString = "DELETE FROM " + tableName + " WHERE id= " + idDB + " OR ";
			} else {
				currentString += "id= " + idDB + " OR ";

				if (currentString.length() > 65400) {
					currentString = currentString.substring(0, currentString.length() - 3);
					currentString = currentString + ";";
					if (!deleteRowsById(currentString)) {
						throw new RuntimeException("FAILED to DELETE data from " + tableName);
					}
					currentString = "";
				}
			}
		}

		if (!currentString.isEmpty()) {
			currentString = currentString.substring(0, currentString.length() - 3);
			if (!deleteRowsById(currentString)) {
				throw new RuntimeException("FAILED to DELETE data from " + tableName);
			}
		}
	}

	default boolean deleteRowsById(String query)throws SQLException {
		boolean hasSaved = false;
		Connection conn = null;
		PreparedStatement ps = null; 
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(query);
			hasSaved = ps.executeUpdate() > 0;
			conn.commit();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return hasSaved;
	}

}
