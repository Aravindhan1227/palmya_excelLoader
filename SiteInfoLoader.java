package com.palmyra.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SiteInfoLoader {

    public static void main(String args[]) {
        loadSiteInfo(System.getProperty("user.home") + "/OneDrive/Desktop/dataload.xlsx");
    }

    public static void loadSiteInfo(String excelFilePath) {
        try (FileInputStream excelFile = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            Sheet sheet = workbook.getSheetAt(0);

            String jdbcUrl = "jdbc:mariadb://localhost:3306/excel";
            String username = "root";
            String password = "iamsherlocked@123";

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    
                    int siteNameRow = findColumnIndex(sheet , "Site");
                    int date = findColumnIndex(sheet, "Date");
                    String siteName = row.getCell(siteNameRow).getStringCellValue();
                    java.util.Date utilDate = row.getCell(date).getDateCellValue();
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                    if (!isDuplicateName(connection, siteName)) {
                    String insertQuery = "INSERT INTO site_info (site_name, site_incharge, site_status, site_started_on) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                        preparedStatement.setString(1, siteName);
                        preparedStatement.setString(2, "Aravind");
                        preparedStatement.setString(3, "A");
                        preparedStatement.setDate(4, sqlDate);

                        preparedStatement.executeUpdate();
                    }
                }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static boolean isDuplicateName(Connection connection, String expenseTypeName) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM site_info WHERE site_name = ?";
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
}

