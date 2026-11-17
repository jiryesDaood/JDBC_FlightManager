import java.sql.*;
import java.util.Scanner;

public class FlightViewer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Ask for MySQL root password
        System.out.print("Enter MySQL root password: ");
        String password = scanner.nextLine();
        
        // JDBC URL and user
        String url = "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";
        String user = "root";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Ask for terminal number
            System.out.print("Enter a terminal number (2, 3, 4, 5): ");
            String terminal = scanner.nextLine();
            
            // Validate terminal number
            if (!terminal.matches("[2-5]")) {
                System.out.println("Invalid terminal number. Please enter 2, 3, 4, or 5.");
                return;
            }
            
            // SQL query with a placeholder for the terminal number
            String sql = "SELECT a.id, a.flight_number, r.from_city, a.scheduled, a.delay_status, a.terminal FROM Arrivals a INNER JOIN FlightRoutes r ON a.flight_number = r.flight_number WHERE a.terminal = ? ORDER BY a.scheduled ASC";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Bind the parameter
                pstmt.setString(1, terminal);
                
                // Execute the query
                try (ResultSet rs = pstmt.executeQuery()) {
                    // Print results as a formatted table
                    System.out.printf("%-5s | %-10s | %-15s | %-10s | %-20s | %-8s%n", "ID", "Flight", "From", "Scheduled", "Status", "Terminal");
                    System.out.println("-".repeat(84));
                    int count = 0;
                    
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String flightNumber = rs.getString("flight_number");
                        String fromCity = rs.getString("from_city");
                        String scheduled = rs.getString("scheduled");
                        String delayStatus = rs.getString("delay_status");
                        String terminalValue = rs.getString("terminal");
                        
                        System.out.printf("%-5d | %-10s | %-15s | %-10s | %-20s | %-8s%n", id, flightNumber, fromCity, scheduled, delayStatus, terminalValue);
                        count++;
                    }
                    
                    // Print total number of flights found
                    if (count == 0) {
                        System.out.println("No flights found at terminal " + terminal + ".");
                    } else {
                        System.out.println("Total flights found: " + count);
                    }
                }
            }
        } catch (SQLException e) {
            // Handle SQLException by printing a clear error message
            System.err.println("SQL Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
