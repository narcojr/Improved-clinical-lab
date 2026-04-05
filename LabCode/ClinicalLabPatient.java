import java.time.LocalDate;
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

    // ── Constructor — records current date and time 
    public ClinicalLabPatient(String name, int age,
                               String sex, String timeLastMeal) {
        this.name           = name;
        this.age            = age;
        this.sex            = sex;
        this.timeLastMeal   = timeLastMeal.trim().isEmpty() ? "N/A" : timeLastMeal.trim();
        this.collectionDate = LocalDate.now();
        this.collectionTime = LocalTime.now().withNano(0);
        this.completedTests = new ArrayList<>();
    }

    //  Add a completed test to this session 
    public void addTest(ClinicalLabTest test) {
        completedTests.add(test);
    }

    //  Getters 
    public String    getName()           { return name; }
    public int       getAge()            { return age; }
    public String    getSex()            { return sex; }
    public String    getTimeLastMeal()   { return timeLastMeal; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public LocalTime getCollectionTime() { return collectionTime; }

    public List<ClinicalLabTest> getCompletedTests() {
        return completedTests;
    }

    @Override
    public String toString() {
        return String.format(
            "Name      : %s%n" +
            "Age       : %d%n" +
            "Sex       : %s%n" +
            "Last Meal : %s%n" +
            "Date      : %s%n" +
            "Time      : %s",
            name, age, sex, timeLastMeal, collectionDate, collectionTime
        );
    }
}
