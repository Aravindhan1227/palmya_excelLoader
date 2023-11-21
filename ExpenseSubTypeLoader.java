package com.palmyra.excel;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mariadb.jdbc.Statement;

public class ExpenseSubTypeLoader {

    public static void main(String[] args) {
        loadExpenseSubTypes(System.getProperty("user.home") + "/OneDrive/Desktop/dataload.xlsx");
    }

    public static void loadExpenseSubTypes(String excelFilePath) {


        try (FileInputStream excelFile = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(excelFile);
             Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/excel?allowPublicKeyRetrieval=true", "root", "iamsherlocked@123")) {
        	clearExpenseSubTypeTable( connection);
            Sheet sheet = workbook.getSheetAt(0);
            int expTypeColumnIndex = findColumnIndex(sheet, "Sub Type");



            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String expenseSubTypeName = row.getCell(expTypeColumnIndex).getStringCellValue();


                    
                    
                    int expenseTypeColumnIndex = findExpenseTypeColumnIndex(sheet, "Exp. Type");
                    String expenseType =row.getCell(expenseTypeColumnIndex).getStringCellValue();
                    int expenseTypeId = getExpenseTypeId(connection, expenseType);

                    if (!isDuplicateName(connection, expenseSubTypeName , expenseTypeId)) {
                    	String insertQuery = "INSERT INTO expense_sub_type (expense_type , name) VALUES (? , ?)";
                    	try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    		preparedStatement.setInt(1, expenseTypeId);
                    		preparedStatement.setString(2, expenseSubTypeName);
                    		preparedStatement.executeUpdate();
                    	} catch (SQLException e) {
                    		e.printStackTrace();
                    	}
                    }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static int findColumnIndex(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (columnName.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private static void clearExpenseSubTypeTable(Connection connection) {
        String deleteQuery = "DELETE FROM expense_sub_type";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getExpenseTypeId(Connection connection, String expenseTypeName) throws SQLException {
        String selectQuery = "SELECT id FROM expense_type WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, expenseTypeName.trim());           
            try (ResultSet resultSet = preparedStatement.executeQuery();) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private static int findExpenseTypeColumnIndex(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (columnName.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }
    private static boolean isDuplicateName(Connection connection, String expenseTypeName, int expenseTypeId) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM expense_sub_type WHERE name = ? AND expense_type = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, expenseTypeName);
            preparedStatement.setInt(2, expenseTypeId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }
}
