package com.ackrotech.healthplus.data.model;

import java.util.Date;

public class CovidStats {

    private long confirmed_cases;
    private long recovered;
    private long deaths;
    private long positiveIncrease;
    private Date date;

    public long getPositiveIncrease() {
        return positiveIncrease;
    }

    public void setPositiveIncrease(long positiveIncrease) {
        this.positiveIncrease = positiveIncrease;
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }

    private long dateLong;

    public long getConfirmed_cases() {
        return confirmed_cases;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setConfirmed_cases(long confirmed_cases) {
        this.confirmed_cases = confirmed_cases;
    }

    public long getRecovered() {
        return recovered;
    }

    public void setRecovered(long recovered) {
        this.recovered = recovered;
    }

    public long getDeaths() {
        return deaths;
    }

    public void setDeaths(long deaths) {
        this.deaths = deaths;
    }
}
