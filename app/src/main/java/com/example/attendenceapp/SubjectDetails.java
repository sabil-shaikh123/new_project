package com.example.attendenceapp;
//this is a package for the subject in the student class
public class SubjectDetails {

    private String subjectName;
    private int attendedClasses;
    private int totalClasses;
    private double attendancePercentage;

    public SubjectDetails(String subjectName, int attendedClasses, int totalClasses, double attendancePercentage) {
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
        return attendancePercentage;
    }
}
