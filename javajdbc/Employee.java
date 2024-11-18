public class Employee {

    private int id;

    private String name;

    private String role;

    private double salary;

    private int managerId;

    public Employee(int id,String name, String role, double salary, int managerId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.salary = salary;
        this.managerId = managerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }


    public int getManagerId() {
        return managerId;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", salary=" + salary +
                ", managerId=" + managerId +
                '}';
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }
}
