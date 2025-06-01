/**
 * Project Name: SayNoToRETAKE
 * Authors: Member1: 230103310, Nessipbay Kamila:)
 *          Member2: 230103276, Akbota Kurman
 *          Member3: 230103216, Nurkanat Bagdatuly
 *
 * Overview:
 * This project is designed to facilitate academic interaction between students and advisers. 
 * It features a JavaFX-based GUI for managing grades, calculating retake probabilities, and 
 * assessing scholarship eligibility. The implementation demonstrates six key design patterns 
 * (Singleton, Factory, Decorator, Proxy, Observer, Strategy) and adheres to clean code principles.
 *
 * Key Features:
 * - User authentication for advisers and students.
 * - Adviser dashboard for viewing student reports.
 * - Student functionalities for grade management and performance analysis.
 * - Modular and scalable codebase.
 *
 * Design Patterns Used:
 * 1. Singleton: Ensures a single instance of the database.
 * 2. Factory: Abstracts user creation logic.
 * 3. Decorator: Dynamically adds bonus scores to subjects.
 * 4. Proxy: Validates student-adviser relationships.
 * 5. Observer: Notifies advisers of grade updates.
 * 6. Strategy: Encapsulates retake and scholarship algorithms.
 * 
 * P.S(Fisrt part is implemention user interface by using javaFX, and second part is implementing patterns(Proxy, Strategy, Singleton, Observer, Factory, Decorator))
 */

import java.util.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SayNoToRETAKE extends Application {

    private Stage primaryStage;
    private Student student;
    private Adviser adviser;
    private TextArea reportsArea;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeDatabase();
        adviser = Database.getInstance().getAdviser();
        showLoginScreen();
    }

    /**
     * The `initializeDatabase` function sets up a database with an adviser and three students, assigns
     * subjects to the students, and adds the adviser as an observer to the students.
     */
    private void initializeDatabase() {
        Database db = Database.getInstance();
        Adviser adviser = new Adviser("Nursat", "SayNo");
        db.setAdviser(adviser);

        Student student1 = new Student("Nursat", "Kamila", "12345", new ArrayList<>());
        Student student2 = new Student("Nursat", "Akbota", "12345", new ArrayList<>());
        Student student3 = new Student("Nursat", "Nurkanat", "12345", new ArrayList<>());

        db.addStudent(student1);
        db.addStudent(student2);
        db.addStudent(student3);

        setupStudentSubjects(student1);
        setupStudentSubjects(student2);
        setupStudentSubjects(student3);

        student1.addObserver(adviser);
        student2.addObserver(adviser);
        student3.addObserver(adviser);
    }

    /**
     * The `showLoginScreen` method creates a JavaFX login screen with fields for user type, name,
     * password, and adviser name, allowing users to log in as either an adviser or a student.
     */
    private void showLoginScreen() {
        VBox loginLayout = new VBox(10);
        loginLayout.getStyleClass().add("login-layout");

        Label userTypeLabel = new Label("Enter user type (Adviser/Student):");
        TextField userTypeField = new TextField();

        Label nameLabel = new Label("Enter your name:");
        TextField nameField = new TextField();

        Label passwordLabel = new Label("Enter your password:");
        PasswordField passwordField = new PasswordField();

        Label adviserLabel = new Label("Enter your adviser name (for students):");
        TextField adviserField = new TextField();
        adviserLabel.setVisible(false);
        adviserField.setVisible(false);

        userTypeField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isStudent = "Student".equalsIgnoreCase(newVal);
            adviserLabel.setVisible(isStudent);
            adviserField.setVisible(isStudent);
        });

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        loginButton.setOnAction(e -> {
            String userType = userTypeField.getText();
            String name = nameField.getText();
            String password = passwordField.getText();
            String adviserName = adviserField.getText();

            User user = UserFactory.createUser(userType, adviserName, name, password);
            if (user == null) {
                showAlert("Error", "User creation failed. Incorrect user type: " + userType);
                return;
            }

            if (user instanceof Adviser) {
                if (!user.authentication(name, password)) {
                    showAlert("Error", "Wrong name or password for adviser.");
                    return;
                }
                this.adviser = (Adviser) user;
                showAdviserMenu();
            } else if (user instanceof Student) {
                if (adviserName == null || adviserName.isEmpty()) {
                    showAlert("Error", "Adviser name is required for student authentication.");
                    return;
                }

                // The code snippet is performing the following actions:
                // 1. Creating a ProxyAuthentication object with user, adviserName, and adviser
                // parameters.
                // 2. Checking if the authentication using the name and password provided is successful
                // by calling the authentication method on the proxy object. If authentication fails,
                // an error message is shown and the function returns.
                // 3. Casting the user object to a Student object and setting up the student's
                // subjects.
                // 4. Adding the adviser as an observer to the student.
                // 5. Displaying the student menu.

                ProxyAuthentication proxy = new ProxyAuthentication(user, adviserName, adviser);
                if (!proxy.authentication(name, password)) {
                    showAlert("Error", "Wrong name, password, or adviser name.");
                    return;
                }

                student = (Student) user;
                setupStudentSubjects(student);
                student.addObserver(adviser);

                showStudentMenu();
            }
        });

        loginLayout.getChildren().addAll(userTypeLabel, userTypeField, nameLabel, nameField, passwordLabel, passwordField, adviserLabel, adviserField, loginButton);
        
        Scene loginScene = new Scene(loginLayout, 900, 800);
        loginScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Say no to RETAKE");
        primaryStage.show();
    }

    /**
     * The `showAdviserMenu` function creates a menu layout with buttons to view students' reports and
     * logout in a Java application.
     */
    private void showAdviserMenu() {
        VBox adviserMenu = new VBox(10);
        adviserMenu.getStyleClass().add("menu-layout");

        Button viewReportsButton = new Button("View Students' Reports");
        viewReportsButton.getStyleClass().add("menu-button");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("menu-button");

        viewReportsButton.setOnAction(e -> showStudentsReportsToAdviser());
        logoutButton.setOnAction(e -> showLoginScreen());

        adviserMenu.getChildren().addAll(viewReportsButton, logoutButton);
        Scene adviserMenuScene = new Scene(adviserMenu, 900, 800);
        adviserMenuScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(adviserMenuScene);
    }

    /**
     * The function `showStudentsReportsToAdviser` generates and displays reports for all students in a
     * database to an adviser in a Java application.
     */
    private void showStudentsReportsToAdviser() {
        VBox reportsLayout = new VBox(10);
        reportsLayout.getStyleClass().add("report-layout");

        Label title = new Label("Students' Reports");
        title.getStyleClass().add("report-title");

        reportsArea = new TextArea();
        reportsArea.setEditable(false);
        reportsArea.setPrefHeight(900);

        StringBuilder reports = new StringBuilder();
        for (Student student : Database.getInstance().getAllStudents()) {
            reports.append(student.generateReport()).append("\n\n");
        }
        reportsArea.setText(reports.toString());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showAdviserMenu());

        reportsLayout.getChildren().addAll(title, reportsArea, backButton);

        Scene reportsScene = new Scene(reportsLayout, 900, 800);
        reportsScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(reportsScene);
    }

/**
*  The function 'showStudentMenu' displays the student menu with various options such as adding grades, 
* viewing reports, calculating retake probability, and checking scholarship eligibility.
*/
    private void showStudentMenu() {
        VBox studentMenu = new VBox(10);
        studentMenu.getStyleClass().add("menu-layout");

        Button addGradesButton = new Button("Add Grades");
        addGradesButton.getStyleClass().add("menu-button");

        Button viewReportButton = new Button("View Report");
        viewReportButton.getStyleClass().add("menu-button");

        Button calculateRetakeButton = new Button("Calculate Retake Probability");
        calculateRetakeButton.getStyleClass().add("menu-button");

        Button calculateScholarshipButton = new Button("Calculate Scholarship Eligibility");
        calculateScholarshipButton.getStyleClass().add("menu-button");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("menu-button");

        addGradesButton.setOnAction(e -> showAddGradesMenu());
        viewReportButton.setOnAction(e -> showStudentReport());
        calculateRetakeButton.setOnAction(e -> showRetakeProbability());
        calculateScholarshipButton.setOnAction(e -> showScholarshipProbability());
        logoutButton.setOnAction(e -> showLoginScreen());

        studentMenu.getChildren().addAll(addGradesButton, viewReportButton, calculateRetakeButton, calculateScholarshipButton, logoutButton);
        Scene studentMenuScene = new Scene(studentMenu, 900, 800);
        studentMenuScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(studentMenuScene);
    }
/**
 * The function 'showStudentReport' displays the student report with a title, a non-editable text area containing the report,
 * and a back button to return to the student menu.
 */
    private void showStudentReport() {
        VBox reportLayout = new VBox(10);
        reportLayout.getStyleClass().add("report-layout");

        Label title = new Label("Student Report");
        title.getStyleClass().add("report-title");

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(400);
        reportArea.setText(student.generateReport());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showStudentMenu());

        reportLayout.getChildren().addAll(title, reportArea, backButton);

        Scene reportScene = new Scene(reportLayout, 900, 800);
        reportScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(reportScene);
    }

/**
* The function 'showRetakeProbability' displays the retake probability result for the student.
* It includes a title, a non-editable text area showing the calculated probability, 
* and a back button to return to the student menu.
*/

    private void showRetakeProbability() {
        VBox probabilityLayout = new VBox(10);
        probabilityLayout.getStyleClass().add("report-layout");

        Label title = new Label("Retake Probability");
        title.getStyleClass().add("report-title");
        title.getStyleClass().add("black-text");

        TextArea resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setPrefHeight(400);
        student.setStrategy(new RetakeStrategy());
        resultTextArea.setText(student.executeStrategy());
        resultTextArea.getStyleClass().add("blue-text-field");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showStudentMenu());

        probabilityLayout.getChildren().addAll(title, resultTextArea, backButton);

        Scene probabilityScene = new Scene(probabilityLayout, 900, 800);
        probabilityScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(probabilityScene);
    }

/**
* The function 'showScholarshipProbability' displays the scholarship eligibility result for the student.
* It includes a title, a non-editable text area showing the calculated scholarship probability, 
* and a back button to return to the student menu.
*/
    private void showScholarshipProbability() {
        VBox probabilityLayout = new VBox(10);
        probabilityLayout.getStyleClass().add("report-layout");

        Label title = new Label("Scholarship Probability");
        title.getStyleClass().add("report-title");
        title.getStyleClass().add("black-text");

        TextArea resultTextArea = new TextArea();
        student.setStrategy(new ScholarshipStrategy());
        resultTextArea.setText(student.executeStrategy());
        resultTextArea.getStyleClass().add("blue-text-field");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showStudentMenu());

        probabilityLayout.getChildren().addAll(title, resultTextArea, backButton);

        Scene probabilityScene = new Scene(probabilityLayout, 900, 800);
        probabilityScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(probabilityScene);
    }

/**
* The function 'showAddGradesMenu' displays the menu for adding grades to a subject.
* It allows the user to select a subject, enter a category, input a score, 
* and submit the grade. A back button returns the user to the student menu.
*/
    private void showAddGradesMenu() {
        VBox addGradesLayout = new VBox(10);
        addGradesLayout.getStyleClass().add("menu-layout");

        Label subjectLabel = new Label("Choose a subject:");
        ComboBox<String> subjectComboBox = new ComboBox<>();
        student.getSubjects().forEach(subject -> subjectComboBox.getItems().add(subject.getName()));

        Label categoryLabel = new Label("Enter category:");
        TextField categoryField = new TextField();
        categoryField.setPromptText("e.g., Quiz, Midterm, Lab.work, Project, Home work, Contest, Ders");

        Label scoreLabel = new Label("Enter total score:");
        TextField scoreField = new TextField();
        scoreField.setPromptText("e.g., Quiz(40), Midterm(30), Lab.work(40), Project(20), Contest(30), Home work(30), Ders(30)");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showStudentMenu());

        Button addButton = new Button("Add Grade");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> {
            String subjectName = subjectComboBox.getValue();
            String category = categoryField.getText();
            double score;
            try {
                score = Double.parseDouble(scoreField.getText());
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid score. Please enter a number.");
                return;
            }

            Subject subject = student.getSubjects().stream()
                    .filter(s -> s.getName().equals(subjectName))
                    .findFirst()
                    .orElse(null);

            if (subject == null || !subject.getMaxPoints().containsKey(category)) {
                showAlert("Error", "Invalid subject or category.");
                return;
            }

            subject.addGrade(category, score);
            showAlert("Success", "Grade added successfully!");
        });

        addGradesLayout.getChildren().addAll(subjectLabel, subjectComboBox, categoryLabel, categoryField, scoreLabel, scoreField, addButton, backButton);

        Scene addGradesScene = new Scene(addGradesLayout, 900, 800);
        addGradesScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(addGradesScene);
    }

/**
* The function 'setupStudentSubjects' initializes the subjects for a student if they do not already have any subjects.
* It creates a set of base subjects with specific categories and their respective weightings, 
* then adds bonus points to some of the subjects using the decorator pattern.
*/
    private void setupStudentSubjects(Student student) {
        if (student.getSubjects().isEmpty()) {
            Subject calculus = new BaseSubject("Calculus", Map.of("Quiz", 40.0, "Midterm", 30.0));
            Subject designPatterns = new BaseSubject("Design Patterns", Map.of("Lab.work", 40.0, "Project", 20.0));
            Subject algorithms = new BaseSubject("Data Structure and Algorithms", Map.of("Home work", 30.0, "Contest", 30.0));
            Subject turkish = new BaseSubject("Turkish language", Map.of("Midterm", 30.0, "Ders", 30.0));
    
            // Adding bonuses to some subjects
            Subject bonusCalculus = new BonusDecoratorSubject(calculus, 5.0);
            Subject bonusDesignPatterns = new BonusDecoratorSubject(designPatterns, 3.0);
    
            student.addSubject(bonusCalculus);
            student.addSubject(bonusDesignPatterns);
            student.addSubject(algorithms);
            student.addSubject(turkish);
        }
    }

/**
* The function 'showAlert' displays an information alert with a given title and message.
* It creates an alert of type INFORMATION, sets the title and content, and shows the alert to the user.
*/
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

/**
 * 2'nd PART
 * Class: Database
 * Pattern: Singleton
 *
 * Purpose:
 * Ensures a single instance for centralized student and adviser data management.
 *
 * Key Points:
 * - Provides global access to data.
 * - Enforces the single instance via `getInstance`.
 */

    public static class Database {
        private static final Database instance = new Database();
        private Map<String, Student> students = new HashMap<>();
        private Adviser adviser;
        private Database() {}

        public static Database getInstance() {
            return instance;
        }

        public void addStudent(Student student) {
            students.put(student.getName(), student);
        }

        public Student getStudent(String name) {
            return students.get(name);
        }

        public Collection<Student> getAllStudents() {
            return students.values();
        }

        public Adviser getAdviser(){
            return adviser;
        }

        public void setAdviser(Adviser adviser){
            this.adviser = adviser;
        }
    }

/**
* The 'Subject' interface defines the contract for subject objects.
* It provides methods to get the subject name, total score, grades, and maximum points,
* as well as the ability to add grades for the subject.(Part of implementing Decorator pattern)
*/

    public interface Subject{
        String getName();
        double getTotalScore();
        Map<String, Double> getGrades();
        Map<String, Double> getMaxPoints();
        void addGrade(String category, double totalScore);
    }   

/**
* The 'BaseSubject' class implements the 'Subject' interface.
* It represents a subject with its name, categories, and associated grades.
* It allows adding grades to different categories and calculating the total score for the subject.
*/

    public class BaseSubject implements Subject {
        private String name;
        private Map<String, Double> grades;
        private Map<String, Double> maxPoints;
    
        public BaseSubject(String name, Map<String, Double> maxPoints) {
            this.name = name;
            this.maxPoints = new HashMap<>(maxPoints);
            this.grades = new HashMap<>();
            for (String category : maxPoints.keySet()) {
                grades.put(category, 0.0);
            }
        }
    
        @Override
        public String getName() {
            return name;
        }
    
        @Override
        public void addGrade(String category, double totalScore) {
            if (!grades.containsKey(category)) {
                System.out.println("Invalid category: " + category);
                return;
            }
    
            double currentScore = grades.get(category);
            double maxScore = maxPoints.get(category);
    
            if (totalScore + currentScore > maxScore) {
                System.out.println("Error: Total score for " + category + " exceeds maximum (" + (totalScore + currentScore) + "/" + maxScore + ").");
                return;
            }
            grades.put(category, currentScore + totalScore);
            System.out.println("Score updated for " + category + ": " + grades.get(category) + "/" + maxScore + ".");
        }
    
        @Override
        public double getTotalScore() {
            return grades.values().stream().mapToDouble(Double::doubleValue).sum();
        }
    
        @Override
        public Map<String, Double> getGrades() {
            return grades;
        }
    
        @Override
        public Map<String, Double> getMaxPoints() {
            return maxPoints;
        }
    }

    /**
 * The BonusDecoratorSubject class demonstrates the Decorator pattern.
 * It dynamically adds bonus points to a Subject without modifying the original subject's implementation.
 * This pattern promotes open-closed design by allowing extension of functionality.
 */

    public class BonusDecoratorSubject implements Subject {
        private Subject subject;
        private double bonus;
    
        public BonusDecoratorSubject(Subject subject, double bonus) {
            this.subject = subject;
            this.bonus = bonus;
        }
    
        @Override
        public String getName() {
            return subject.getName();
        }
    
        @Override
        public void addGrade(String category, double totalScore) {
            subject.addGrade(category, totalScore);
        }
    
        @Override
        public double getTotalScore() {
            return subject.getTotalScore() + bonus;
        }
    
        @Override
        public Map<String, Double> getGrades() {
            return subject.getGrades();
        }
    
        @Override
        public Map<String, Double> getMaxPoints() {
            return subject.getMaxPoints();
        }
    }

    /**
 * The Strategy pattern is used to define different algorithms for calculating retake probability 
 * and scholarship eligibility. The `Student` class uses the Strategy interface to execute the chosen algorithm.
 */
    public interface Strategy {
        String calculate(Student student);
    }

    public static class RetakeStrategy implements Strategy {
        private static final double passScore = 50.0; 
        @Override
        public String calculate(Student student) {
            StringBuilder result = new StringBuilder();
            result.append("\nRetake Probability for ").append(student.getName()).append("\n");
    
            for (Subject subject : student.getSubjects()) {
                double totalScore = subject.getTotalScore();
                double maxScore = 100;
                double requiredScore = passScore;
    
                if (totalScore >= requiredScore) {
                    result.append("- ").append(subject.getName()).append(": No need to retake (Score: ").append(totalScore).append("/").append(maxScore).append(")\n");
                } else {
                    double remainingScoreNeeded = requiredScore - totalScore;
                    double retakeProbability = (remainingScoreNeeded / maxScore) * 100;
                    result.append("- ").append(subject.getName()).append(": ").append(String.format("%.2f", retakeProbability)).append("% chance of retaking (Score: ").append(totalScore).append("/").append(maxScore).append(")\n");
                }
            }
            return result.toString();
        }
    }

    public static class ScholarshipStrategy implements Strategy {
        @Override
        public String calculate(Student student) {
            StringBuilder result = new StringBuilder();
            result.append("\nScholarship Eligibility for ").append(student.getName()).append("\n");

            for (Subject subject : student.getSubjects()) {
                double totalScore = subject.getTotalScore();
                result.append("- ").append(subject.getName()).append(": ").append(totalScore).append("\n");

                if (totalScore >= 60) {
                    result.append("You are on track for a scholarship.\n");
                } else if (totalScore >= 30) {
                    result.append("You need to score at least ").append(70 - totalScore).append(" in the final to secure the scholarship.\n");
                } else {
                    result.append("You need significant improvement to qualify for a scholarship.\n");
                }
            }
            return result.toString();
        }
    }


    // The above code is defining a Java interface named `User`. This interface can be used to define a
    // contract for classes that implement it, specifying the methods that those classes must
    // implement. Using for implementing Factory pattern.
    public interface User {
        boolean authentication(String name, String password);
    }

    /**
 * The Adviser class implements the User interface and the Observer interface, providing methods for
 * authentication(Proxy pattern), updating reports, and receiving notifications about student grades(Observer).
 */
    private static class Adviser implements User, Observer {
        private String name;
        private String password;
        private TextArea reportsArea;

        public Adviser(String name, String password) {
            this.name = name;
            this.password = password;
        }

        @Override
        public boolean authentication(String name, String password) {
            return this.name.equals(name) && this.password.equals(password);
        }


        private void updateReports() {
            if (reportsArea != null) {
            StringBuilder reports = new StringBuilder();
        for (Student student : Database.getInstance().getAllStudents()) {
                reports.append(student.generateReport()).append("\n\n");
        }
            reportsArea.setText(reports.toString());
    }
}       

        
       /**
        * The update method(method of Observer) in Java notifies the adviser about a student adding a grade and then
        * updates reports.
        * The `student` parameter in the `update` method represents an instance of the
        * `Student` class. It is used to update information related to a student, such as adding a
        * grade.
        */

        @Override
        public void update(Student student) {
            System.out.println("Adviser " + this.name + " received notification: Student " + student.getName() + " added a grade.");
            updateReports();
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }
    }

    /**
 * The Observer pattern is implemented to notify the Adviser about changes in the Student's grades.
 * - Observable: The `Student` class allows observers (Adviser) to register, remove, and receive updates.
 * - Observer: The `Adviser` class updates its reports whenever a Student's grades are modified.
 */
    public interface Observer {
        void update(Student student);
    }

    public interface Observable {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);
        void notifyObservers();
    }

    /**
     * The `Student` class in Java represents a student with properties such as adviser, name,
     * password, subjects and grades, and implements the `User` and `Observable` interfaces for
     * proxy and observer pattern functionality.
     */
    static class Student implements User, Observable {
        private String adviser;
        private String name;
        private String password;
        private List<Subject> subjectsAndGrades;
        private Strategy strategy;
        private List<Observer> observers = new ArrayList<>();

        public Student(String adviser, String name, String password, List<Subject> subjectsAndGrades) {
            this.adviser = adviser;
            this.name = name;
            this.password = password;
            this.subjectsAndGrades = subjectsAndGrades;
        }

        public void addSubject(Subject subject) {
            subjectsAndGrades.add(subject);
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public String executeStrategy() {
            if (strategy != null) {
                return strategy.calculate(this);
            } else {
                return "Strategy not set.";
            }
        }

        public String getName() {
            return name;
        }

        public List<Subject> getSubjects() {
            return subjectsAndGrades;
        }

        public void addGrade(String subjectName, String category, double grade) {
            for (Subject subject : subjectsAndGrades) {
                if (subject.getName().equalsIgnoreCase(subjectName)) {
                    subject.addGrade(category, grade);
                    notifyObservers();
                    return;
                }
            }
            System.out.println("Subject not found: " + subjectName);
        }

        public String generateReport() {
            StringBuilder report = new StringBuilder();
            report.append("\nGrade Report for ---").append(this.name).append("\n");
            for (Subject subject : subjectsAndGrades) {
                double totalScore = subject.getTotalScore();
                report.append("- ").append(subject.getName()).append(": ").append(totalScore).append("\n");
            }
            return report.toString();
        }

        // The above code is implementing the proxy interface in Java.
        @Override
        public boolean authentication(String name, String password) {
            return (this.name).equals(name) && (this.password).equals(password);
        }

        @Override
        public void addObserver(Observer observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(Observer observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(this);
            }
        }
    }


/**
 * The ProxyAuthentication class implements the Proxy pattern.
 * It adds an intermediary layer to validate the student's adviser relationship 
 * before granting access to certain functionalities.
 * This pattern enhances security and controls access.
 */
    public static class ProxyAuthentication implements User {
        private User user;
        private String adviserName;
        private Adviser adviser;

        public ProxyAuthentication(User user, String adviserName, Adviser adviser) {
            this.user = user;
            this.adviserName = adviserName;
            this.adviser = adviser;
        }

        @Override
        public boolean authentication(String name, String password) {
            if (user instanceof Student) {
                Student student = (Student) user;
                if (!student.authentication(name, password)) {
                    return false;
                }

                return adviserName.equals(student.adviser) && adviser.authentication(adviser.getName(), adviser.getPassword());
            } else {
                return user.authentication(name, password);
            }
        }
    }

/**
 * The UserFactory class demonstrates the Factory Method pattern.
 * It provides a centralized way to create instances of different user types (Adviser or Student).
 * This pattern abstracts the creation logic, making it flexible to add new user types in the future.
 */
    public static class UserFactory {
        public static User createUser(String userType, String adviserName, String name, String password) {
            Database db = Database.getInstance();
    
            if ("Adviser".equalsIgnoreCase(userType)) {
                Adviser adviser = db.getAdviser();
                if (adviser != null && adviser.authentication(name, password)) {
                    return adviser;
                }
            } else if ("Student".equalsIgnoreCase(userType)) {
                Student student = db.getStudent(name);
                if (student != null && student.authentication(name, password)) {
                    return student;
                }
            }
    
            return null;
        }
    }
}
