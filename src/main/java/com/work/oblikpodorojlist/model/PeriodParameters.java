package com.work.oblikpodorojlist.model;

import java.time.LocalDate;
import java.time.Period;

public class PeriodParameters {
    private LocalDate startDate;
    private LocalDate endDate;
    private Period period;

    public PeriodParameters() {
    }

    public PeriodParameters(LocalDate startDate, LocalDate endDate, Period period) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
