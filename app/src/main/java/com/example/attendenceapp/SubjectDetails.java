package com.example.attendenceapp;
//this is a package for the subject in the student class
public class SubjectDetails {

    private String subjectName;
    private int attendedClasses;
    private int totalClasses;
    private double attendancePercentage;

    public SubjectDetails(String subjectName, int attendedClasses, int totalClasses) {
        this.subjectName = subjectName;
        this.attendedClasses = attendedClasses;
        this.totalClasses = totalClasses;
        this.attendancePercentage = attendancePercentage;
    }

    // Getters for all fields
    public String getSubjectName() {
        return subjectName;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public double getAttendancePercentage() {
        if (totalClasses == 0 || attendedClasses == 0) {
            return 0.0; // Avoid division by zero
        }
        return (attendedClasses / (double) totalClasses) * 100;
    }

}
