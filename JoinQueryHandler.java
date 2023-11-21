package com.palmyra.excel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JoinQueryHandler {
	
	  public static int[] getExpenseSubTypeId(Connection connection, String expenseTypeName, String expenseSubTypeName) throws SQLException {
	        String selectQuery = "SELECT et.id AS exp_type, est.id AS sub_type "
	                + "FROM expense_type et, expense_sub_type est "
	                + "WHERE et.name = ? AND et.id = est.expense_type AND est.name = ?";
	        
	        int[] ids = new int[2];
	        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
	            preparedStatement.setString(1, expenseTypeName.trim());
	            preparedStatement.setString(2, expenseSubTypeName);
	            ResultSet resultSet = preparedStatement.executeQuery();
	            if (resultSet.next()) {
	                ids[0] = resultSet.getInt("exp_type");
	                ids[1] = resultSet.getInt("sub_type");
	            }
	        }
	        return ids;
	    }


}
