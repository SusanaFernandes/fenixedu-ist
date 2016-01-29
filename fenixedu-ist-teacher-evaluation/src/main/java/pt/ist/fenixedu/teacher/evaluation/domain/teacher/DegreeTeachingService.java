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
package pt.ist.fenixedu.teacher.evaluation.domain.teacher;

import java.math.BigDecimal;
import java.util.Comparator;

import org.fenixedu.academic.domain.Lesson;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import pt.ist.fenixedu.teacher.evaluation.domain.time.calendarStructure.TeacherCreditsFillingCE;

public class DegreeTeachingService extends DegreeTeachingService_Base {

    public static final Comparator<DegreeTeachingService> DEGREE_TEACHING_SERVICE_COMPARATOR_BY_SHIFT =
            new Comparator<DegreeTeachingService>() {
                @Override
                public int compare(DegreeTeachingService degreeTeachingService1, DegreeTeachingService degreeTeachingService2) {
                    return Shift.SHIFT_COMPARATOR_BY_TYPE_AND_ORDERED_LESSONS.compare(degreeTeachingService1.getShift(),
                            degreeTeachingService2.getShift());
                }
            };

    public DegreeTeachingService(TeacherService teacherService, Professorship professorship, Shift shift, Double percentage) {
        super();
        if (teacherService == null || professorship == null || shift == null || percentage == null) {
            throw new DomainException("arguments can't be null");
        }
        if (percentage > 100 || percentage < 0) {
            throw new DomainException("message.invalid.percentage");
        }
        setTeacherService(teacherService);
        TeacherCreditsFillingCE.checkValidCreditsPeriod(getTeacherService().getExecutionPeriod(), Authenticate.getUser());
        setProfessorship(professorship);
        setShift(shift);

        Double availablePercentage = TeacherService.getAvailableShiftPercentage(getShift(), getProfessorship());

        if (percentage > availablePercentage) {
            throw new DomainException("message.exceeded.percentage");
        }

        setPercentage(percentage);
    }

    @Override
    public void delete() {
        TeacherCreditsFillingCE.checkValidCreditsPeriod(getTeacherService().getExecutionPeriod(), Authenticate.getUser());
        new TeacherServiceLog(getTeacherService(), BundleUtil.getString("resources.TeacherCreditsSheetResources", "label.teacher.schedule.delete",
                getTeacherService().getTeacher().getPerson().getNickname(), getShift().getPresentationName(), getPercentage()
                        .toString()));
        setTeacherService(null);
        setShift(null);
        setProfessorship(null);
        super.delete();
    }

    public void updatePercentage(Double percentage) {
        TeacherCreditsFillingCE.checkValidCreditsPeriod(getTeacherService().getExecutionPeriod(), Authenticate.getUser());
        if (percentage == null || percentage > 100 || percentage < 0) {
            throw new DomainException("message.invalid.percentage");
        }
        if (percentage == 0) {
            delete();
        } else {
            Double availablePercentage = TeacherService.getAvailableShiftPercentage(getShift(), getProfessorship());
            if (percentage > availablePercentage) {
                throw new DomainException("message.exceeded.percentage");
            }
            setPercentage(percentage);
        }
    }

    @Deprecated
    public double getHours() {
        double totalHours = 0;
        boolean isTeacherProfessorCategory =
                getProfessorship().getTeacher()
                        .getCategory(getProfessorship().getExecutionCourse().getExecutionPeriod().getAcademicInterval())
                        .map(tc -> tc.getProfessionalCategory()).map(pc -> pc.isTeacherProfessorCategory()).orElse(false);
        if (isTeacherProfessorCategory) {
            double hoursAfter20PM = getShift().getHoursOnSaturdaysOrNightHours(20);
            double hoursBefore20PM = getShift().getUnitHours().doubleValue() - hoursAfter20PM;
            totalHours += hoursBefore20PM * (getPercentage().doubleValue() / 100);
            totalHours += (hoursAfter20PM * (getPercentage().doubleValue() / 100)) * 1.5;
        } else {
            double hours = getShift().getUnitHours().doubleValue();
            totalHours += (hours * (getPercentage().doubleValue() / 100));
        }
        return totalHours;
    }

    public double getEfectiveLoad() {
        boolean isTeacherProfessorCategory =
                getProfessorship().getTeacher()
                        .getCategory(getProfessorship().getExecutionCourse().getExecutionPeriod().getAcademicInterval())
                        .map(tc -> tc.getProfessionalCategory()).map(pc -> pc.isTeacherProfessorCategory()).orElse(false);
        double afterHeightFactor = isTeacherProfessorCategory ? 1.5 : 1;

        double weeklyHoursAfter20 = getTotalHoursAfter20AndSaturdays() / 14;
        double weeklyHoursBefore20 = (getShift().getCourseLoadWeeklyAverage().doubleValue() - weeklyHoursAfter20);

        BigDecimal percentage = new BigDecimal(getPercentage() / 100).setScale(4, BigDecimal.ROUND_HALF_UP);
        return (new BigDecimal((weeklyHoursBefore20 + (weeklyHoursAfter20 * afterHeightFactor))).multiply(percentage)).setScale(
                2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public double getTotalHoursAfter20AndSaturdays() {
        int minutesAfter20AndSaturday = 0;
        for (Lesson lesson : getShift().getAssociatedLessonsSet()) {
            for (Interval lessonInterval : lesson.getAllLessonIntervals()) {
                if (lessonInterval.getStart().getDayOfWeek() == DateTimeConstants.SATURDAY) {
                    minutesAfter20AndSaturday +=
                            Minutes.minutesBetween(lessonInterval.getStart(), lessonInterval.getEnd()).getMinutes();
                } else {
                    DateTime dateTimeAfter20 = lessonInterval.getStart().toLocalDate().toDateTime(new LocalTime(20, 0, 0));
                    if (dateTimeAfter20.isBefore(lessonInterval.getEnd())) {
                        if (!dateTimeAfter20.isAfter(lessonInterval.getStart())) {
                            minutesAfter20AndSaturday +=
                                    Minutes.minutesBetween(lessonInterval.getStart(), lessonInterval.getEnd()).getMinutes();
                        } else {
                            minutesAfter20AndSaturday +=
                                    Minutes.minutesBetween(dateTimeAfter20, lessonInterval.getEnd()).getMinutes();
                        }
                    }

                }
            }
        }
        return (double) minutesAfter20AndSaturday / DateTimeConstants.MINUTES_PER_HOUR;
    }

    public double calculateCredits() {
        return getProfessorship().getExecutionCourse().getUnitCreditValue() == null ? 0.0 : getEfectiveLoad()
                * getProfessorship().getExecutionCourse().getUnitCreditValue().doubleValue();
    }

    public static int getDegreeTeachingServiceLessonRows(Professorship professorship) {
        int lessonNumber = 0;
        for (DegreeTeachingService degreeTeachingService : professorship.getDegreeTeachingServicesSet()) {
            int associatedLessonsCount = degreeTeachingService.getShift().getAssociatedLessonsSet().size();
            if (associatedLessonsCount == 0) {
                lessonNumber += 1;
            }
            lessonNumber += associatedLessonsCount;
        }
        if (lessonNumber == 0) {
            lessonNumber += 1;
        }
        lessonNumber += professorship.getSupportLessonsSet().size();
        return lessonNumber;
    }

}
