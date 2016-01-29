/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Teacher Credits.
 *
 * FenixEdu IST Teacher Credits is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Teacher Credits is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Teacher Credits.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Dec 26, 2003 by jpvl
 *  
 */
package pt.ist.fenixedu.teacher.evaluation.util.date;

import java.util.Calendar;
import java.util.Date;

import org.fenixedu.academic.util.FenixUtil;

/**
 * @author jpvl
 */
public class TimePeriod extends FenixUtil {
    private final long start;

    private final long end;

    public TimePeriod(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     *  
     */
    public TimePeriod(Date start, Date end) {
        this(start.getTime(), end.getTime());
    }

    public TimePeriod(Calendar start, Calendar end) {
        this(start.getTimeInMillis(), end.getTimeInMillis());
    }

    public Double hours() {

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(this.end);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(start);

        int endMinutes = endCalendar.get(Calendar.MINUTE);
        int startMinutes = startCalendar.get(Calendar.MINUTE);

        endCalendar.roll(Calendar.HOUR_OF_DAY, -(startCalendar.get(Calendar.HOUR_OF_DAY)));
        endCalendar.roll(Calendar.MINUTE, -(startCalendar.get(Calendar.MINUTE)));

        int minutes = endCalendar.get(Calendar.MINUTE);
        int hours = endMinutes < startMinutes ? endCalendar.get(Calendar.HOUR_OF_DAY) - 1 : endCalendar.get(Calendar.HOUR_OF_DAY);

        double minutesInHours = minutes / 60.0;
        return Double.valueOf(hours + minutesInHours);
    }
}