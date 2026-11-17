import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Updates the delay status of flight {@code LX354} in the {@code Arrivals} table of {@code FlightDB}.
 * <p>
 * <b>Security fix (SQL Injection).</b> The previous version of this class built the SQL command with
 * a plain {@link java.sql.Statement} and raw string concatenation:
 * </p>
 * <pre>
 * "UPDATE Arrivals SET delay_status = '" + userInput + "' WHERE flight_number = 'LX354'"
 * </pre>
 * <p>
 * Because the user's input became part of the SQL text itself, the payload {@code Landed' #}
 * produced the command {@code UPDATE Arrivals SET delay_status = 'Landed' #' WHERE flight_number = 'LX354'}.
 * The single quote ({@code '}) closed the string literal early, and {@code #} started a MySQL
 * comment, so the database ignored the entire {@code WHERE} clause and overwrote all 299 rows.
 * </p>
 * <p>
 * <b>Why {@link PreparedStatement} prevents this.</b> The SQL is sent to the server with {@code ?}
 * placeholders and compiled <i>before</i> any user data is supplied, so the structure of the query
 * (the {@code WHERE} clause included) is fixed. Values bound with {@code setString} are transmitted
 * strictly as data (literals) and are never parsed as SQL, so quotes, comment markers or extra
 * keywords in the input are stored verbatim. The same payload now updates exactly one row and
 * sets its status literally to {@code Landed' #}.
 * </p>
 */
public class SimpleFlightUpdater {

    /**
     * Prompts for the MySQL root password and a new delay status, then updates flight {@code LX354}
     * using a parameterized {@link PreparedStatement} inside try-with-resources blocks.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Database connection details
        String url = "jdbc:mysql://localhost:3306/FlightDB?allowLoadLocalInfile=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem&useSSL=false";
        String user = "root";

        // Prompt the user for the MySQL root password
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter MySQL root password: ");
        String password = scanner.nextLine();

        // New delay status to be set
        System.out.print("Enter new delay status for flight 'LX354': ");
        String newDelayStatus = scanner.nextLine();
        scanner.close();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            // SQL update statement using PreparedStatement with parameterized queries
            String sql = "UPDATE Arrivals SET delay_status = ? WHERE flight_number = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newDelayStatus);
                pstmt.setString(2, "LX354");

                // Execute the update and get the number of affected rows
                int affectedRows = pstmt.executeUpdate();

                // Print the number of affected rows
                System.out.println("Number of affected rows: " + affectedRows);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
