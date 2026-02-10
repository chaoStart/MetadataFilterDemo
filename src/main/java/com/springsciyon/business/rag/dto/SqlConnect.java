package com.springsciyon.business.rag.dto;

import java.sql.*;

public class SqlConnect {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/rag_metadata?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    static String getMysqlVersion() {
        String sql = "SELECT COUNT(*) AS c, IFNULL(MAX(update_time),0) AS t FROM DocumentTag";
        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("c") + "_" + rs.getString("t");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}