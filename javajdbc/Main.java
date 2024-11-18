import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<Manager> managerList = initializeManagers();

        List<Employee> employeeList = initializeEmployees();

        String url = "jdbc:postgresql://localhost:5432/test_db";
        String username = "wadoodh";
        String password = "wadoodh";

        String insertManagerQuery = "INSERT INTO Manager (id, name) VALUES (?, ?)";
        String insertEmployeeQuery = "INSERT INTO Employee (id, name, role, salary, managerId) VALUES (?, ?, ?, ?, ?)";
        String selectQuery = "SELECT e.id, e.name AS employeeName, e.role, e.salary, e.managerId, m.name AS managerName " +
                "FROM Employee e " +
                "INNER JOIN Manager m ON e.managerId = m.id";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to the PostgreSQL server successfully!");
            insertManagers(connection, insertManagerQuery, managerList);
            insertEmployees(connection, insertEmployeeQuery, employeeList);
            displayEmployeeDetails(connection, selectQuery);

        } catch (SQLException e) {
            System.out.println("Error while connecting to the database: " + e.getMessage());
        }
    }

    private static List<Manager> initializeManagers() {
        List<Manager> managerList = new ArrayList<>();
        managerList.add(new Manager(1, "Steve"));
        managerList.add(new Manager(2, "Mike"));
        return managerList;
    }

    private static List<Employee> initializeEmployees() {
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(1, "Kumar", "Developer", 20000.0, 1));
        employeeList.add(new Employee(2, "Abdul", "Developer", 30000.0, 1));
        employeeList.add(new Employee(3, "Thomas", "Tester", 40000.0, 2));
        employeeList.add(new Employee(4, "Frank", "Designer", 50000.0, 2));
        employeeList.add(new Employee(5, "Ram", "Designer", 30000.0, 2));
        return employeeList;
    }

    private static void insertManagers(Connection connection, String query, List<Manager> managers) throws SQLException {
        String truncateQuery = "TRUNCATE TABLE Manager RESTART IDENTITY CASCADE";
        try (PreparedStatement truncateStmt = connection.prepareStatement(truncateQuery)) {
            truncateStmt.executeUpdate();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (Manager manager : managers) {
                preparedStatement.setInt(1, manager.getId());
                preparedStatement.setString(2, manager.getName());
                preparedStatement.executeUpdate();
            }
            System.out.println("Managers inserted successfully!");
        }
    }

    private static void insertEmployees(Connection connection, String query, List<Employee> employees) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (Employee employee : employees) {
                preparedStatement.setInt(1, employee.getId());
                preparedStatement.setString(2, employee.getName());
                preparedStatement.setString(3, employee.getRole());
                preparedStatement.setDouble(4, employee.getSalary());
                preparedStatement.setInt(5, employee.getManagerId());
                preparedStatement.executeUpdate();
            }
            System.out.println("Employees inserted successfully!");
        }
    }

    private static void displayEmployeeDetails(Connection connection, String query) throws SQLException {
        List<Employee> employees = fetchEmployees(connection, query);

        processAndDisplayEmployeeDetails(employees);
    }

    private static List<Employee> fetchEmployees(Connection connection, String query) throws SQLException {
        List<Employee> employees = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Employee and Manager details:");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String employeeName = resultSet.getString("employeeName");
                String role = resultSet.getString("role");
                double salary = resultSet.getDouble("salary");
                String managerName = resultSet.getString("managerName");
                int managerId = resultSet.getInt("managerId");

                Employee employee = new Employee(id, employeeName, role, salary, managerId);
                employees.add(employee);

                System.out.printf("Employee ID: %d, Name: %s, Role: %s, Salary: %.2f, Manager: %s%n",
                        id, employeeName, role, salary, managerName);
            }
        }

        return employees;
    }

    private static void processAndDisplayEmployeeDetails(List<Employee> employees) {
        Predicate<Employee> isDeveloper = employee -> employee.getRole().equals("Developer");

        Function<Employee, Employee> raiseSalary = employee -> {
            employee.setSalary(employee.getSalary() * 1.20);
            return employee;
        };

        Supplier<List<Employee>> updatedDevelopersSupplier = () -> {
            List<Employee> updatedDevelopers = new ArrayList<>();
            for (Employee employee : employees) {
                if (isDeveloper.test(employee)) {
                    Employee updatedEmployee = raiseSalary.apply(employee);
                    updatedDevelopers.add(updatedEmployee);
                }
            }
            return updatedDevelopers;
        };

        List<Employee> updatedDevelopers = updatedDevelopersSupplier.get();

        Consumer<Employee> printEmployee = System.out::println;

        updatedDevelopers.forEach(printEmployee);

        Map<String, List<Employee>> employeesGroupedByRole = employees.stream()
                .collect(Collectors.groupingBy(Employee::getRole));

        System.out.println("\nEmployees grouped by roles:");
        employeesGroupedByRole.forEach((role, employeeList) -> {
            System.out.println("\nRole: " + role);
            employeeList.forEach(printEmployee);
        });
    }


//    private static void displayEmployeeDetails(Connection connection, String query) throws SQLException {
//        try (Statement statement = connection.createStatement();
//             ResultSet resultSet = statement.executeQuery(query)) {
//
//            List<Employee> employees = new ArrayList<>();
//            System.out.println("Employee and Manager details:");
//
//            while (resultSet.next()) {
//                int id = resultSet.getInt("id");
//                String employeeName = resultSet.getString("employeeName");
//                String role = resultSet.getString("role");
//                double salary = resultSet.getDouble("salary");
//                String managerName = resultSet.getString("managerName");
//                int managerId = resultSet.getInt("managerId");
//
//                Employee employee = new Employee(id, employeeName, role, salary, managerId);
//                employees.add(employee);
//
//                System.out.printf("Employee ID: %d, Name: %s, Role: %s, Salary: %.2f, Manager: %s%n",
//                        id, employeeName, role, salary, managerName);
//            }
//
//            Supplier<List<Employee>> updatedDevelopersSupplier = () ->
//                    employees.stream()
//                            .filter(employee -> employee.getRole().equals("Developer")) // Predicate
//                            .map(employee -> {
//                                double raisedSalary = employee.getSalary() * 1.20;
//                                employee.setSalary(raisedSalary);
//                                return employee; // Function
//                            })
//                            .collect(Collectors.toList());
//
//            List<Employee> updatedDevelopers = updatedDevelopersSupplier.get();
//
//            // Print updated employees using forEach (Consumer)
//            updatedDevelopers.forEach(System.out::println);
//
//            Map<String, List<Employee>> employeesGroupedByRole = employees.stream().collect(Collectors.groupingBy(Employee::getRole));
//
//            System.out.println("\nEmployees grouped by roles:");
//            employeesGroupedByRole.forEach((role, employeeList) -> {
//                System.out.println("\nRole: " + role);
//                employeeList.forEach(System.out::println);
//            });
//        }
//    }

}
