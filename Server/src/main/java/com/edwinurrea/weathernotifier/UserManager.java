package com.edwinurrea.weathernotifier;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.JWT;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.mindrot.jbcrypt.BCrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    
    protected static void registerNewUser(String formattedPhoneNumber, String password) {
        // BCrypt hashes the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "INSERT INTO users (phone_number, password_hash) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, formattedPhoneNumber);
                stmt.setString(2, hashedPassword);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to insert user into the database.", e);
        }
    }
    
    protected static void deleteUser(int userId) {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");
        
        String deleteQuery = """
                             DELETE FROM user_zip_codes WHERE user_id = ?;
                             DELETE FROM user_zip_delivery WHERE user_id = ?;
                             DELETE FROM users WHERE user_id = ?;
                             """;
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword); 
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                
            conn.setAutoCommit(false);

            deleteStmt.setInt(1, userId);
            deleteStmt.setInt(2, userId);
            deleteStmt.setInt(3, userId);
                
            deleteStmt.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error: Failed to delete user data.", e);       
        }
    }
    
    protected static String getPassword(String formattedPhoneNumber) {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT password_hash FROM users WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, formattedPhoneNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password_hash");
                    } else {
                        logger.error("Error: User with the given phone number does not exist.");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to retrieve password.", e);
        }
        return null;
    }
    
    protected static void resetPassword(String formattedPhoneNumber, String password) {
        // BCrypt hashes the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT user_id FROM users WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, formattedPhoneNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");

                        String updateQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, hashedPassword);
                            updateStmt.setInt(2, userId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        logger.error("Error: User with the given phone number does not exist.");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to reset password.", e);
        }
    }

    
    protected static boolean isUserAuthenticated(String formattedPhoneNumber, String password) {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT password_hash FROM users WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, formattedPhoneNumber);
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password_hash");
                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to authenticate user using phone number.", e);
        }

        return false;
    }
    
    protected static int getUserId(String formattedPhoneNumber) throws SQLException {
        String dbUrl = DatabaseConnector.config.getProperty("database.url");
        String dbUser = DatabaseConnector.config.getProperty("database.username");
        String dbPassword = DatabaseConnector.config.getProperty("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT user_id FROM users WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, formattedPhoneNumber);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to retrieve user_id from the database.", e);
            return -1;
        }
    }
    
    protected static String generateToken(int userId) {
        String secretKey = DatabaseConnector.config.getProperty("jwt.secret.key");
        if (secretKey == null) {
            throw new IllegalStateException("Secret key not found in configuration.");
        }
        
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withSubject(String.valueOf(userId))
                    .withExpiresAt(Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IllegalStateException("Error generating token.", e);
        }
    }

    protected static String authenticateAndGetToken(String formattedPhoneNumber, String password, int userId) {
        if (isUserAuthenticated(formattedPhoneNumber, password)) {
            return generateToken(userId);
        }
        return null;
    }
}
