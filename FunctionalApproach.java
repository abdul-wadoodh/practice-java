import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FunctionalApproach {

    public static void main(String[] args) {
        Supplier<List<Manager>> managerSupplier = () -> Arrays.asList(
                new Manager(1, "Steve"),
                new Manager(2, "Mike")
        );

        Supplier<List<Employee>> employeeSupplier = () -> Arrays.asList(
                new Employee(1, "Kumar", "Developer", 20000.0, 1),
                new Employee(2, "Abdul", "Developer", 30000.0, 1),
                new Employee(3, "Thomas", "Tester", 40000.0, 2),
                new Employee(4, "Frank", "Designer", 50000.0, 2)
        );

        // Database credentials and queries
        String url = "jdbc:postgresql://localhost:5432/test_db";
        String username = "wadoodh";
        String password = "wadoodh";

        String insertManagerQuery = "INSERT INTO Manager (id, name) VALUES (?, ?)";
        String insertEmployeeQuery = "INSERT INTO Employee (id, name, role, salary, managerId) VALUES (?, ?, ?, ?, ?)";
        String selectQuery = "SELECT e.id, e.name AS employeeName, e.role, e.salary, m.name AS managerName " +
                "FROM Employee e " +
                "INNER JOIN Manager m ON e.managerId = m.id";

        // Functional wrapper for database operations
        performDatabaseOperations(
                () -> createConnection(url, username, password),
                connection -> {
                    System.out.println("Connected to the PostgreSQL server successfully!");
                    insertData(connection, insertManagerQuery, managerSupplier.get(), (stmt, manager) -> {
                        stmt.setInt(1, manager.getId());
                        stmt.setString(2, manager.getName());
                    });
                    insertData(connection, insertEmployeeQuery, employeeSupplier.get(), (stmt, employee) -> {
                        stmt.setInt(1, employee.getId());
                        stmt.setString(2, employee.getName());
                        stmt.setString(3, employee.getRole());
                        stmt.setDouble(4, employee.getSalary());
                        stmt.setInt(5, employee.getManagerId());
                    });
                    fetchAndDisplayData(connection, selectQuery, FunctionalApproach::printEmployeeDetails);
                }
        );
    }

    // Creates a database connection
    private static Connection createConnection(String url, String username, String password) {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Error while connecting to the database: " + e.getMessage(), e);
        }
    }

    // Generic method for database operations
    private static void performDatabaseOperations(Supplier<Connection> connectionSupplier, Consumer<Connection> operation) {
        try (Connection connection = connectionSupplier.get()) {
            operation.accept(connection);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Generic method to insert data
    private static <T> void insertData(Connection connection, String query, List<T> dataList, StatementFiller<T> filler) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (T data : dataList) {
                filler.fill(preparedStatement, data);
                preparedStatement.executeUpdate();
            }
            System.out.println("Data inserted successfully!");
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        }
    }

    // Fetches and displays data
    private static void fetchAndDisplayData(Connection connection, String query, Consumer<ResultSet> resultSetConsumer) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            resultSetConsumer.accept(resultSet);
        } catch (SQLException e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }
    }

    // Prints employee details
    private static void printEmployeeDetails(ResultSet resultSet) {
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String employeeName = resultSet.getString("employeeName");
                String role = resultSet.getString("role");
                double salary = resultSet.getDouble("salary");
                String managerName = resultSet.getString("managerName");
                System.out.printf("Employee ID: %d, Name: %s, Role: %s, Salary: %.2f, Manager: %s%n", id, employeeName, role, salary, managerName);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while processing result set: " + e.getMessage(), e);
        }
    }

    // Functional interface for filling a PreparedStatement
    @FunctionalInterface
    interface StatementFiller<T> {
        void fill(PreparedStatement stmt, T data) throws SQLException;
    }

}
