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

public class ExpenseTypeLoader {

    public static void main(String[] args) {
        loadExpenseTypes(System.getProperty("user.home") + "/OneDrive/Desktop/dataload.xlsx");
    }

    public static void loadExpenseTypes(String excelFilePath) {
        try (FileInputStream excelFile = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            String jdbcUrl = "jdbc:mariadb://localhost:3306/excel";
            String username = "root";
            String password = "iamsherlocked@123";

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {

                Sheet sheet = workbook.getSheetAt(0);
                int expTypeColumnIndex = findColumnIndex(sheet, "Exp. Type");

                Set<String> processedNames = new HashSet<>();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    String expenseTypeName = row.getCell(expTypeColumnIndex).getStringCellValue();


//                    if (!processedNames.contains(expenseTypeName)) {
//                        processedNames.add(expenseTypeName);

                        if (!isDuplicateName(connection, expenseTypeName)) {
                            String insertQuery = "INSERT INTO expense_type (name) VALUES (?)";
                            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                                preparedStatement.setString(1, expenseTypeName);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                // Handle the exception according to your application's requirements
                            }
                        }
                        // If the name is a duplicate, you can handle it as needed
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
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

    private static boolean isDuplicateName(Connection connection, String expenseTypeName) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM expense_type WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, expenseTypeName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
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
}
