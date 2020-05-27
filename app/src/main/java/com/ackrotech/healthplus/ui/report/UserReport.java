package com.ackrotech.healthplus;


public class UserReport {
    private String Date;
    private String CentreName;
    private String ReportResult;

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getCentreName() {
        return CentreName;
    }

    public void setCentreName(String centreName) {
        CentreName = centreName;
    }

    public String getReportResult() {
        return ReportResult;
    }

    public void setReportResult(String reportResult) {
        ReportResult = reportResult;
    }

    public UserReport() {

    }
    public UserReport(String Date, String centreName, String ReportResult){
        this.ReportResult = ReportResult;
        this.Date = Date;
        this.CentreName = centreName;

    }
}
