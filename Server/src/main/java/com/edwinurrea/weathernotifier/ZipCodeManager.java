package com.edwinurrea.weathernotifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipCodeManager extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    
    public static void insertZipCode(String zipCode) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");
        String query = "INSERT INTO zip_codes (zip_code) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, zipCode);
            stmt.executeUpdate();
        }
    }

    public static void associateUserWithZipDelivery(int userId, int zipCodeId, String deliveryTime) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        String userZipCodeQuery = "INSERT INTO user_zip_codes (user_id, zip_code_id) VALUES (?, ?)";
        String userZipDeliveryQuery = "INSERT INTO user_zip_delivery (user_id, zip_code_id, delivery_time) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try (PreparedStatement userZipCodeStmt = conn.prepareStatement(userZipCodeQuery); 
                PreparedStatement userZipDeliveryStmt = conn.prepareStatement(userZipDeliveryQuery)) {

                userZipCodeStmt.setInt(1, userId);
                userZipCodeStmt.setInt(2, zipCodeId);
                userZipCodeStmt.executeUpdate();

                userZipDeliveryStmt.setInt(1, userId);
                userZipDeliveryStmt.setInt(2, zipCodeId);
                userZipDeliveryStmt.setString(3, deliveryTime);
                userZipDeliveryStmt.executeUpdate();

                conn.commit(); // Commit the transaction
            } catch (SQLException e) {
                conn.rollback(); // If an error occurs, rollback
                throw e; // After roll back, then re-throw the exception
            }
        }
    }
    
    public static int updateZipCodeAndDeliveryTime(int userId, int zipCodeId, String deliveryTime) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");
        
        String updateQuery = "INSERT INTO user_zip_delivery (user_id, zip_code_id, delivery_time) " + 
                            "VALUES (?, ?, ?) " +
                            "ON CONFLICT (user_id, zip_code_id, delivery_time) " + 
                            "DO NOTHING";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, userId);
            updateStmt.setInt(2, zipCodeId);
            updateStmt.setString(3, deliveryTime);
            
            updateStmt.executeUpdate();

        } catch(SQLException e) {
            logger.error("Error updating zip code and delivery time.", e);
            throw e;
        }
        return -1;
    }
    
    public static void editZipCodeAndDeliveryTime(int userId, String zipCode, String oldDeliveryTime, String newDeliveryTime) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        String checkEntryQuery = "SELECT COUNT(*) FROM user_zip_delivery WHERE user_id = ? AND zip_code_id = (SELECT zip_code_id FROM zip_codes WHERE zip_code = ?) AND delivery_time = ?";
        String updateQuery = "UPDATE user_zip_delivery SET delivery_time = ? WHERE user_id = ? AND zip_code_id = (SELECT zip_code_id FROM zip_codes WHERE zip_code = ?) AND delivery_time = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement checkEntryStmt = conn.prepareStatement(checkEntryQuery); 
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            checkEntryStmt.setInt(1, userId);
            checkEntryStmt.setString(2, zipCode);
            checkEntryStmt.setString(3, oldDeliveryTime);

            ResultSet resultSet = checkEntryStmt.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            if (count > 0) {
                updateStmt.setString(1, newDeliveryTime);
                updateStmt.setInt(2, userId);
                updateStmt.setString(3, zipCode);
                updateStmt.setString(4, oldDeliveryTime);

                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("SQL Error while editing the delivery time.", e);
            throw e;
        }
    }
    
    public static void deleteZipCodeAndDeliveryTime(int userId, String zipCode, String deliveryTime) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        String deleteQuery = "DELETE FROM user_zip_delivery WHERE user_id = ? AND zip_code_id = (SELECT zip_code_id FROM zip_codes WHERE zip_code = ?) AND delivery_time = ?";
        String checkAssociationsQuery = "SELECT COUNT(*) FROM user_zip_delivery WHERE zip_code_id = (SELECT zip_code_id FROM zip_codes WHERE zip_code = ?)";
        String deleteUserZipCodesQuery = "DELETE FROM user_zip_codes WHERE zip_code_id = (SELECT zip_code_id FROM zip_codes WHERE zip_code = ?)";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            PreparedStatement checkAssociationsStmt = conn.prepareStatement(checkAssociationsQuery);
            PreparedStatement deleteUserZipCodesStmt = conn.prepareStatement(deleteUserZipCodesQuery)) {
            
            conn.setAutoCommit(false);

            try {
                deleteStmt.setInt(1, userId);
                deleteStmt.setString(2, zipCode);
                deleteStmt.setString(3, deliveryTime);

                deleteStmt.executeUpdate();

                checkAssociationsStmt.setString(1, zipCode);
                ResultSet resultSet = checkAssociationsStmt.executeQuery();

                resultSet.next();
                int count = resultSet.getInt(1);

                if (count == 0) {
                    deleteUserZipCodesStmt.setString(1, zipCode);
                    deleteUserZipCodesStmt.executeUpdate();
                    
                    String deleteZipCodeQuery = "DELETE FROM zip_codes WHERE zip_code = ?";
                    try (PreparedStatement deleteZipCodeStmt = conn.prepareStatement(deleteZipCodeQuery)) {
                        deleteZipCodeStmt.setString(1, zipCode);
                        deleteZipCodeStmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                logger.error("SQL Error occurred while deletion: {}", e);
                throw e;
            }
        }
    }
    
    public static int getZipCodeId(String zipCode) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");
        String query = "SELECT zip_code_id FROM zip_codes WHERE zip_code = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, zipCode);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("zip_code_id");
            }
        }
        return -1;
    }

    public static String getZipCodeByUserId(int userId) throws SQLException {
        String zipCode = null;

        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        String query1 = "SELECT zip_code_id FROM user_zip_codes WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement stmt1 = conn.prepareStatement(query1)) {
            stmt1.setInt(1, userId);
            ResultSet resultSet1 = stmt1.executeQuery();

            if (resultSet1.next()) {
                int zipCodeId = resultSet1.getInt("zip_code_id");

                String query2 = "SELECT zip_code FROM zip_codes WHERE zip_code_id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(query2)) {
                    stmt2.setInt(1, zipCodeId);
                    ResultSet resultSet2 = stmt2.executeQuery();

                    if (resultSet2.next()) {
                        zipCode = resultSet2.getString("zip_code");
                    }
                }
            }
        }

        return zipCode;
    }
    
    public static String getDeliveryTimeForZipCode(int userId, int zipCodeId) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");
        String query = "SELECT delivery_time FROM user_zip_delivery WHERE user_id = ? AND zip_code_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, zipCodeId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("delivery_time");
            }
        }
        return null;
    }
    
    public List<ZipCodeData> getZipCodesAndDeliveryTimes(int userId) throws SQLException {
        List<ZipCodeData> zipCodeDataList = new ArrayList<>();

        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        String query = "SELECT pc.zip_code, upd.delivery_time "
                + "FROM user_zip_codes upc "
                + "INNER JOIN zip_codes pc ON upc.zip_code_id = pc.zip_code_id "
                + "INNER JOIN user_zip_delivery upd ON upc.user_id = upd.user_id "
                + "AND upc.zip_code_id = upd.zip_code_id "
                + "WHERE upc.user_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String zipCode = resultSet.getString("zip_code");
                String deliveryTime = resultSet.getString("delivery_time");
                zipCodeDataList.add(new ZipCodeData(zipCode, deliveryTime));
            }
        }

        return zipCodeDataList;
    }
    
    public class ZipCodeData {
        private final String zipCode;
        private final String deliveryTime;

        public ZipCodeData(String zipCode, String deliveryTime) {
            this.zipCode = zipCode;
            this.deliveryTime = deliveryTime;
        }

        public String getZipCode() {
            return zipCode;
        }

        public String getDeliveryTime() {
            return deliveryTime;
        }
    }
}