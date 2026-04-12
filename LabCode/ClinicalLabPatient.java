import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicalLabPatient {

    private final String    name;
    private final int       age;
    private final String    sex;
    private final String    timeLastMeal;
    private final LocalDate collectionDate;
    private final LocalTime collectionTime;
    private final List<ClinicalLabTest> completedTests;

    // Payment fields
    private String paymentMethod    = "CASH";
    private String paymentReference = "";
    private double totalAmount      = 0.0;
    private double amountPaid       = 0.0;
    private double change           = 0.0;

    public ClinicalLabPatient(String name, int age, String sex, String timeLastMeal) {
        this.name           = name;
        this.age            = age;
        this.sex            = sex;
        this.timeLastMeal   = (timeLastMeal == null || timeLastMeal.trim().isEmpty())
                              ? "N/A" : timeLastMeal.trim();
        this.collectionDate = LocalDate.now();
        this.collectionTime = LocalTime.now().withNano(0);
        this.completedTests = new ArrayList<>();
    }

    public void addTest(ClinicalLabTest test) {
        completedTests.add(test);
    }

    // Getters
    public String    getName()             { return name; }
    public int       getAge()              { return age; }
    public String    getSex()              { return sex; }
    public String    getTimeLastMeal()     { return timeLastMeal; }
    public LocalDate getCollectionDate()   { return collectionDate; }
    public LocalTime getCollectionTime()   { return collectionTime; }
    public String    getPaymentMethod()    { return paymentMethod; }
    public String    getPaymentReference() { return paymentReference; }
    public double    getTotalAmount()      { return totalAmount; }
    public double    getAmountPaid()       { return amountPaid; }
    public double    getChange()           { return change; }

    public List<ClinicalLabTest> getCompletedTests() { return completedTests; }

    public LocalDateTime getCollectionDateTime() {
        return LocalDateTime.of(collectionDate, collectionTime);
    }

    public double getSubtotal() {
        double sum = 0;
        for (ClinicalLabTest t : completedTests) sum += t.getPrice();
        return sum;
    }

    // Setters
    public void setPaymentMethod(String m) {
        this.paymentMethod = (m == null) ? "CASH" : m;
    }
    public void setPaymentReference(String r) {
        this.paymentReference = (r == null) ? "" : r.trim();
    }
    public void setTotalAmount(double a) { this.totalAmount = a; }
    public void setAmountPaid(double a)  { this.amountPaid  = a; }
    public void setChange(double c)      { this.change      = c; }

    @Override
    public String toString() {
        return String.format(
            "Name         : %s%n" +
            "Age          : %d%n" +
            "Sex          : %s%n" +
            "Last Meal    : %s%n" +
            "Date         : %s%n" +
            "Time         : %s",
            name, age, sex, timeLastMeal, collectionDate, collectionTime);
    }
}