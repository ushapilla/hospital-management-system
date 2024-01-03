package Hospitalmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/HospitalManagementSystem";
    private static final String username = "root";
    private static final String password = "root";

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Scanner scanner = new Scanner(System.in)) {
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                Patient patient = new Patient(connection, scanner);
                Doctor doctor = new Doctor(connection);
                Beddao bedDAO = new Beddao(connection);
                Warddao wardDAO = new Warddao(connection);
                Pharmacy pharmacy = new Pharmacy(connection); // Create an instance of Pharmacy
                Lab lab = new Lab(connection);
                Invoicedao invoiceDAO = new Invoicedao(connection);
                Paymentdao paymentDAO = new Paymentdao(connection);

                while (true) {
                    System.out.println("---------HOSPITAL MANAGEMENT SYSTEM----------");
                    System.out.println("1.Add Patient");
                    System.out.println("2.View Patients");
                    System.out.println("3.View Doctors");
                    System.out.println("4.Book Appointment");
                    System.out.println("5.Allocate Bed");
                    System.out.println("6.Add Ward ");
                    System.out.println("7.View Ward");
                    System.out.println("8.Dispense Medication");
                    System.out.println("9.Order Lab Test");
                    System.out.println("10.Enter Lab Test Result");
                    System.out.println("11.View Diagnostic Reports");
                    System.out.println("12.createInvoice");
                    System.out.println("13.makePayment");
                    System.out.println("14.Exit");
                    System.out.println("Enter your choice: ");
                    int choice = scanner.nextInt();

                    switch (choice) {
                        case 1:
                            patient.addPatient();
                            System.out.println();
                            break;
                        case 2:
                            patient.viewPatients();
                            System.out.println();
                            break;
                        case 3:
                            doctor.viewDoctors();
                            System.out.println();
                            break;
                        case 4:
                            bookAppointment(patient, doctor, connection, scanner);
                            System.out.println();
                            break;
                        case 5:
                        	 allocateBed(bedDAO, scanner);
                            break;
                        case 6:
                            addWard(wardDAO, scanner);
                            break;
                        case 7:
                            wardDAO.viewWard();
                            break;
                        case 8:
                            dispenseMedication(pharmacy, scanner);
                            break;
                        case 9:
                            orderLabTest(lab, scanner);
                            break;
                        case 10:
                            enterLabTestResult(lab, scanner);
                            break;
                        case 11:
                            viewDiagnosticReports(lab, scanner);
                            break;
                        case 12:
                        	createInvoice(invoiceDAO, scanner);
                            break;
                        case 13:
                        	 makePayment(paymentDAO, scanner);
                            break;
                        case 14:
                            System.out.println("******THANK YOU! FOR USING HOSPITAL MANAGEMENT SYSTEM!!*****");
                            return;
                        default:
                            System.out.println("Enter valid choice!!!");
                            break;
                    }
                }

                

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void allocateBed(Beddao bedDAO, Scanner scanner) {
        System.out.print("Enter Bed ID: ");
        int bedId = scanner.nextInt();
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();

        boolean bedAllocated = bedDAO.allocateBed(bedId, patientId);

        if (bedAllocated) {
            System.out.println("Bed allocated successfully!");
        } else {
            System.out.println("Failed to allocate bed.");
        }
        System.out.println();
    }

    private static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor Id: ");
        int doctorId = scanner.nextInt();
        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String appointmentDate = scanner.next();
        if (patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)) {
            if (checkDoctorAvailability(doctorId, appointmentDate, connection)) {
                String appointmentQuery = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(appointmentQuery);
                    preparedStatement.setInt(1, patientId);
                    preparedStatement.setInt(2, doctorId);
                    preparedStatement.setString(3, appointmentDate);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Appointment Booked!");
                    } else {
                        System.out.println("Failed to Book Appointment!");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Doctor not available on this date!!");
            }
        } else {
            System.out.println("Either doctor or patient doesn't exist!!!");
        }
    }

    private static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void dispenseMedication(Pharmacy pharmacy, Scanner scanner) {
        System.out.println("Enter Prescription ID:");
        int prescriptionId = scanner.nextInt();
        pharmacy.dispenseMedication(prescriptionId);
    }
    private static void orderLabTest(Lab labImagingIntegration, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        System.out.print("Enter Test Name: ");
        String testName = scanner.nextLine();
        labImagingIntegration.orderTest(patientId, testName);
        System.out.println();
    }

    private static void enterLabTestResult(Lab labImagingIntegration, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        System.out.print("Enter Test Name: ");
        String testName = scanner.nextLine();
        System.out.print("Enter Test Result: ");
        String result = scanner.nextLine();
        labImagingIntegration.enterTestResult(patientId, testName, result);
        System.out.println();
    }

    private static void viewDiagnosticReports(Lab labImagingIntegration, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        labImagingIntegration.viewDiagnosticReports(patientId);
        System.out.println();
    }
    private static void createInvoice(Invoicedao invoiceDAO, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Total Amount: ");
        double totalAmount = scanner.nextDouble();

        int invoiceId = invoiceDAO.createInvoice(patientId, totalAmount);

        if (invoiceId != -1) {
            System.out.println("Invoice created successfully! Invoice ID: " + invoiceId);
        } else {
            System.out.println("Failed to create invoice.");
        }
        System.out.println();
    }

    private static void makePayment(Paymentdao paymentDAO, Scanner scanner) {
        System.out.print("Enter Invoice ID: ");
        int invoiceId = scanner.nextInt();
        System.out.print("Enter Payment Amount: ");
        double amount = scanner.nextDouble();

        boolean paymentSuccessful = paymentDAO.makePayment(invoiceId, amount);

        if (paymentSuccessful) {
            System.out.println("Payment successful!");
        } else {
            System.out.println("Payment failed.");
        }
        System.out.println();
    }

    private static void addWard(Warddao wardDAO, Scanner scanner) {
        System.out.print("Enter Ward ID: ");
        int wardId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        System.out.print("Enter Ward Name: ");
        String wardName = scanner.nextLine();

        Ward ward = new Ward(wardId, wardName);
        wardDAO.addWard(ward);

        System.out.println("Ward added successfully!");
    }
}
