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
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import pt.ist.fenixedu.teacher.evaluation.domain.SupportLesson;
import pt.ist.fenixframework.Atomic;

public class TeacherService extends TeacherService_Base {

    public TeacherService(Teacher teacher, ExecutionSemester executionSemester) {
        super();
        if (teacher == null || executionSemester == null) {
            throw new DomainException("arguments can't be null");
        }
        TeacherService teacherService = getTeacherServiceByExecutionPeriod(teacher, executionSemester);
        if (teacherService != null) {
            throw new DomainException("error.teacherService.already.exists.one.teacherService.in.executionPeriod");
        }
        setRootDomainObject(Bennu.getInstance());
        setTeacher(teacher);
        setExecutionPeriod(executionSemester);
    }

    public void delete() {
        if (getServiceItemsSet().isEmpty()) {
            setTeacher(null);
            setExecutionPeriod(null);
            setRootDomainObject(null);
            deleteDomainObject();
        } else {
            throw new DomainException("There are service items associated to this Teacher Service");
        }
    }

    @Atomic
    public static TeacherService getTeacherService(Teacher teacher, ExecutionSemester executionPeriod) {
        TeacherService teacherService = getTeacherServiceByExecutionPeriod(teacher, executionPeriod);
        if (teacherService == null) {
            teacherService = new TeacherService(teacher, executionPeriod);
        }
        return teacherService;
    }

    public DegreeTeachingService getDegreeTeachingServiceByShiftAndProfessorship(final Shift shift,
            final Professorship professorship) {
        return (DegreeTeachingService) CollectionUtils.find(getDegreeTeachingServices(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                DegreeTeachingService degreeTeachingService = (DegreeTeachingService) arg0;
                return (degreeTeachingService.getShift() == shift) && (degreeTeachingService.getProfessorship() == professorship);
            }
        });
    }

    public List<DegreeTeachingService> getDegreeTeachingServiceByProfessorship(final Professorship professorship) {
        return (List<DegreeTeachingService>) CollectionUtils.select(getDegreeTeachingServices(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                DegreeTeachingService degreeTeachingService = (DegreeTeachingService) arg0;
                return degreeTeachingService.getProfessorship() == professorship;
            }
        });
    }

    public Double getCredits() throws ParseException {
        double credits = getTeachingDegreeCredits();
        credits += getOtherServiceCredits();
        return round(credits);
    }

    public Double getTeachingDegreeHours() {
        double hours = 0;
        for (DegreeTeachingService degreeTeachingService : getDegreeTeachingServices()) {
            if (!degreeTeachingService.getProfessorship().getExecutionCourse().getProjectTutorialCourse()) {
                hours += degreeTeachingService.getEfectiveLoad();
            }
        }
        return round(hours);
    }

    public Double getTeachingDegreeCorrections() {
        double hours = 0;
        for (OtherService otherService : getOtherServices()) {
            if (otherService instanceof DegreeTeachingServiceCorrection) {
                DegreeTeachingServiceCorrection degreeTeachingServiceCorrection = (DegreeTeachingServiceCorrection) otherService;
                if (!degreeTeachingServiceCorrection.getProfessorship().getExecutionCourse().getProjectTutorialCourse()) {
                    hours += degreeTeachingServiceCorrection.getCorrection().doubleValue();
                }
            }
        }
        return round(hours);
    }

    public Double getTeachingDegreeCredits() {
        double credits = 0;
        for (DegreeTeachingService degreeTeachingService : getDegreeTeachingServices()) {
            if (!degreeTeachingService.getProfessorship().getExecutionCourse().getProjectTutorialCourse()) {
                credits += degreeTeachingService.calculateCredits();
            }
        }
        return round(credits);
    }

    public Double getSupportLessonHours() {
        double hours = 0;
        for (SupportLesson supportLesson : getSupportLessons()) {
            hours += supportLesson.hours();
        }
        return round(hours);
    }

    public Double getOtherServiceCredits() {
        double credits = 0;
        for (OtherService otherService : getOtherServices()) {
            credits += otherService.getCredits();
        }
        return round(credits);
    }

    public Double getInstitutionWorkingHours() {
        double hours = 0;
        for (InstitutionWorkTime institutionWorkTime : getInstitutionWorkTimes()) {
            hours += institutionWorkTime.getHours();
        }
        return round(hours);
    }

    public List<DegreeTeachingService> getDegreeTeachingServices() {
        return (List<DegreeTeachingService>) CollectionUtils.select(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof DegreeTeachingService;
            }
        });
    }

    public List<DegreeProjectTutorialService> getDegreeProjectTutorialServices() {
        return (List<DegreeProjectTutorialService>) CollectionUtils.select(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof DegreeProjectTutorialService;
            }
        });
    }

    public List<OtherService> getOtherServices() {
        return (List<OtherService>) CollectionUtils.select(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof OtherService;
            }
        });
    }

    public List<InstitutionWorkTime> getInstitutionWorkTimes() {
        return (List<InstitutionWorkTime>) CollectionUtils.select(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof InstitutionWorkTime;
            }
        });
    }

    public ReductionService getReductionService() {
        return (ReductionService) CollectionUtils.find(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof ReductionService;
            }
        });
    }

    public List<SupportLesson> getSupportLessons() {
        List<SupportLesson> supportLessons = new ArrayList<SupportLesson>();
        for (Professorship professorship : getTeacher().getProfessorships()) {
            ExecutionCourse executionCourse = professorship.getExecutionCourse();
            if (executionCourse.getExecutionPeriod() == getExecutionPeriod()) {
                if (!executionCourse.isMasterDegreeDFAOrDEAOnly()) {
                    supportLessons.addAll(professorship.getSupportLessonsSet());
                }
            }
        }
        return supportLessons;
    }

    public List<TeacherServiceComment> getTeacherServiceComments() {
        return (List<TeacherServiceComment>) CollectionUtils.select(getServiceItemsSet(), new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return arg0 instanceof TeacherServiceComment;
            }
        });
    }

    private Double round(double n) {
        return Math.round((n * 100.0)) / 100.0;
    }

    public BigDecimal getReductionServiceCredits() {
        return getReductionService() == null || getReductionService().getCreditsReductionAttributed() == null ? BigDecimal.ZERO : getReductionService()
                .getCreditsReductionAttributed();
    }

    public SortedSet<TeacherServiceLog> getSortedLogs() {
        return new TreeSet<TeacherServiceLog>(getTeacherServiceLogSet());
    }

    @Atomic
    public void lockTeacherCredits() {
        setTeacherServiceLock(new DateTime());
        new TeacherServiceLog(this, BundleUtil.getString("resources.TeacherCreditsSheetResources", "label.teacher.lockTeacherCredits",
                getExecutionPeriod().getQualifiedName()));
    }

    @Atomic
    public void unlockTeacherCredits() {
        setTeacherServiceLock(null);
        new TeacherServiceLog(this, BundleUtil.getString("resources.TeacherCreditsSheetResources", "label.teacher.unlockTeacherCredits",
                getExecutionPeriod().getQualifiedName()));
    }

    public static Double getHoursLecturedOnExecutionCourse(Teacher teacher, ExecutionCourse executionCourse) {
        double returnValue = 0;
        Professorship professorship = teacher.getProfessorshipByExecutionCourse(executionCourse);
        TeacherService teacherService = getTeacherServiceByExecutionPeriod(teacher, executionCourse.getExecutionPeriod());
        if (teacherService != null) {
            List<DegreeTeachingService> teachingServices = teacherService.getDegreeTeachingServiceByProfessorship(professorship);
            for (DegreeTeachingService teachingService : teachingServices) {
                returnValue +=
                        ((teachingService.getPercentage() / 100) * teachingService.getShift().getUnitHours().doubleValue());
            }
        }
        return returnValue;
    }

    public static Duration getLecturedDurationOnExecutionCourse(Teacher teacher, ExecutionCourse executionCourse) {
        Duration duration = Duration.ZERO;
        Professorship professorship = teacher.getProfessorshipByExecutionCourse(executionCourse);
        TeacherService teacherService = getTeacherServiceByExecutionPeriod(teacher, executionCourse.getExecutionPeriod());
        if (teacherService != null) {
            List<DegreeTeachingService> teachingServices = teacherService.getDegreeTeachingServiceByProfessorship(professorship);
            for (DegreeTeachingService teachingService : teachingServices) {
                duration =
                        duration.plus(new Duration(new Double((teachingService.getPercentage() / 100)
                                * teachingService.getShift().getCourseLoadWeeklyAverage().doubleValue() * 3600 * 1000)
                                .longValue()));
            }
        }
        return duration;
    }

    public static TeacherService getTeacherServiceByExecutionPeriod(Teacher teacher, ExecutionSemester executionSemester) {
        return teacher.getTeacherServicesSet().stream().filter(s -> s.getExecutionPeriod() == executionSemester).findAny()
                .orElse(null);
    }

    public static SortedSet<DegreeTeachingService> getDegreeTeachingServicesOrderedByShift(Professorship professorship) {
        final SortedSet<DegreeTeachingService> degreeTeachingServices =
                new TreeSet<DegreeTeachingService>(DegreeTeachingService.DEGREE_TEACHING_SERVICE_COMPARATOR_BY_SHIFT);
        degreeTeachingServices.addAll(professorship.getDegreeTeachingServicesSet());

        return degreeTeachingServices;
    }

    public static DegreeTeachingService getDegreeTeachingServiceByShift(Professorship professorship, Shift shift) {
        for (DegreeTeachingService degreeTeachingService : professorship.getDegreeTeachingServicesSet()) {
            if (degreeTeachingService.getShift() == shift) {
                return degreeTeachingService;
            }
        }
        return null;
    }

    public static Double getAvailableShiftPercentage(Shift shift, Professorship professorship) {
        Double availablePercentage = 100.0;
        for (DegreeTeachingService degreeTeachingService : shift.getDegreeTeachingServicesSet()) {
            if (degreeTeachingService.getProfessorship() != professorship) {
                availablePercentage -= degreeTeachingService.getPercentage();
            }
        }
        return new BigDecimal(availablePercentage).divide(new BigDecimal(1), 2, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static SortedSet<SupportLesson> getSupportLessonsOrderedByStartTimeAndWeekDay(Professorship professorship) {
        final SortedSet<SupportLesson> supportLessons =
                new TreeSet<SupportLesson>(SupportLesson.SUPPORT_LESSON_COMPARATOR_BY_HOURS_AND_WEEK_DAY);
        supportLessons.addAll(professorship.getSupportLessonsSet());
        return supportLessons;
    }

}
