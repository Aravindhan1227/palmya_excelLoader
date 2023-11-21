package com.palmyra.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SiteExpenseLoader {

    public static void main(String args[]) {
        loadSiteExpenses(System.getProperty("user.home") + "/OneDrive/Desktop/dataload.xlsx");
    }

    public static void loadSiteExpenses(String excelFilePath) {
        try (FileInputStream excelFile = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            Sheet sheet = workbook.getSheetAt(0);

            String jdbcUrl = "jdbc:mariadb://localhost:3306/excel";
            String username = "root";
            String password = "iamsherlocked@123";

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                deleteRecord(connection, "site_expense");
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    String expenseType = getStringCellValue(row, "Exp. Type");
                    String expenseSubTypeName = getStringCellValue(row, "Sub Type");
                    String siteName = getStringCellValue(row, "Site");

                    int[] ids = JoinQueryHandler.getExpenseSubTypeId(connection, expenseType, expenseSubTypeName);
                    int expenseTypeId = ids[0];
                    int expenseSubTypeId = ids[1];
                    int siteId = getSiteId(connection, siteName);

                    int date = findColumnIndex(sheet, "Date");
                    java.util.Date utilDate = row.getCell(date).getDateCellValue();
                    java.sql.Date expenseOn = new java.sql.Date(utilDate.getTime());
                    BigDecimal amount = getDecimalCellValue(row, "Amount");
                    String description = getStringCellValue(row, "Description");

                 
                    String insertQuery = "INSERT INTO site_expense (expense_type, expense_sub_type, site, expense_on, amount, description) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        preparedStatement.setInt(1, expenseTypeId);
                        preparedStatement.setInt(2, expenseSubTypeId);
                        preparedStatement.setInt(3, siteId);
                        preparedStatement.setDate(4, expenseOn);
                        preparedStatement.setBigDecimal(5, amount);
                        preparedStatement.setString(6, description);

                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteRecord(Connection connection, String tableName) throws SQLException {
        String deleteQuery = "DELETE FROM " + tableName ;

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }

   

    private static BigDecimal getDecimalCellValue(Row row, String columnName) {
        int columnIndex = findColumnIndex(row.getSheet(), columnName);
        return new BigDecimal(row.getCell(columnIndex).getNumericCellValue());
    }

    private static String getStringCellValue(Row row, String columnName) {
        int columnIndex = findColumnIndex(row.getSheet(), columnName);
        return row.getCell(columnIndex).getStringCellValue();
    }

   

    private static int findColumnIndex(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            if (columnName.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }

        return -1;
    }
  

        

    private static int getSiteId(Connection connection, String siteName) throws SQLException {
        String selectQuery = "SELECT id FROM site_info WHERE site_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, siteName.trim());
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }



}
