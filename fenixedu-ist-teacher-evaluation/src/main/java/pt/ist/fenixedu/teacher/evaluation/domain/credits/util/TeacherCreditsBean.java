/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Teacher Evaluation.
 *
 * FenixEdu IST Teacher Evaluation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Teacher Evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Teacher Evaluation.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.teacher.evaluation.domain.credits.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.bennu.core.domain.User;

import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonProfessionalData;
import pt.ist.fenixedu.teacher.evaluation.domain.TeacherCredits;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualCreditsState;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualTeachingCredits;
import pt.ist.fenixedu.teacher.evaluation.dto.credits.CreditLineDTO;

public class TeacherCreditsBean implements Serializable {
    private Teacher teacher;
    private Set<AnnualTeachingCreditsBean> annualTeachingCredits;
    private Set<CreditLineDTO> pastTeachingCredits;
    private boolean hasAnyYearWithCreditsLimitation = false;
    private boolean hasAnyYearWithCorrections = false;
    private boolean canSeeCreditsReduction = false;

    public TeacherCreditsBean(Teacher teacher) {
        this.teacher = teacher;
    }

    public TeacherCreditsBean() {
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Set<CreditLineDTO> getPastTeachingCredits() {
        return pastTeachingCredits;
    }

    public Set<AnnualTeachingCreditsBean> getAnnualTeachingCredits() {
        return annualTeachingCredits;
    }

    public boolean getHasAnyYearWithCreditsLimitation() {
        return hasAnyYearWithCreditsLimitation;
    }

    public void setHasAnyYearWithCreditsLimitation(boolean hasAnyYearWithCreditsLimitation) {
        this.hasAnyYearWithCreditsLimitation = hasAnyYearWithCreditsLimitation;
    }

    public boolean isHasAnyYearWithCorrections() {
        return hasAnyYearWithCorrections;
    }

    public void setHasAnyYearWithCorrections(boolean hasAnyYearWithCorrections) {
        this.hasAnyYearWithCorrections = hasAnyYearWithCorrections;
    }

    public void prepareAnnualTeachingCredits(User user) {
        annualTeachingCredits = new TreeSet<AnnualTeachingCreditsBean>(new Comparator<AnnualTeachingCreditsBean>() {

            @Override
            public int compare(AnnualTeachingCreditsBean annualTeachingCreditsBean1,
                    AnnualTeachingCreditsBean annualTeachingCreditsBean2) {
                return annualTeachingCreditsBean1.getExecutionYear().compareTo(annualTeachingCreditsBean2.getExecutionYear());
            }
        });

        for (AnnualTeachingCredits annualTeachingCredits : teacher.getAnnualTeachingCreditsSet()) {
            AnnualTeachingCreditsBean annualTeachingCreditsBean = new AnnualTeachingCreditsBean(annualTeachingCredits);
            this.annualTeachingCredits.add(annualTeachingCreditsBean);
            if (annualTeachingCredits.getHasAnyLimitation()) {
                hasAnyYearWithCreditsLimitation = true;
            }
            if (annualTeachingCreditsBean.getCorrectionInYears().size() > 0) {
                hasAnyYearWithCorrections = true;
            }
        }

        ExecutionYear nextExecutionYear = AnnualCreditsState.getLastClosedAnnualCreditsState() != null ? AnnualCreditsState
                .getLastClosedAnnualCreditsState().getExecutionYear()
                .getNextExecutionYear() : ExecutionYear.readCurrentExecutionYear();
        for (ExecutionYear executionYear = nextExecutionYear; executionYear != null
                && !executionYear.isAfter(ExecutionYear.readCurrentExecutionYear()); executionYear =
                        executionYear.getNextExecutionYear()) {
            if (isTeacherActiveForYear(executionYear)) {
                this.annualTeachingCredits.add(new AnnualTeachingCreditsBean(executionYear, teacher));
            }
        }

        if (RoleType.SCIENTIFIC_COUNCIL.isMember(user) || RoleType.DEPARTMENT_MEMBER.isMember(user)) {
            setCanSeeCreditsReduction(true);
        }
    }

    private boolean isTeacherActiveForYear(ExecutionYear currentExecutionYear) {
        for (ExecutionSemester executionSemester : currentExecutionYear.getExecutionPeriodsSet()) {
            if (PersonProfessionalData.isTeacherActiveOrHasAuthorizationForSemester(teacher, executionSemester)) {
                return true;
            }
        }
        return false;
    }

    public void preparePastTeachingCredits() {
        pastTeachingCredits = new TreeSet<CreditLineDTO>(new Comparator<CreditLineDTO>() {

            @Override
            public int compare(CreditLineDTO creditLineDTO1, CreditLineDTO creditLineDTO2) {
                return creditLineDTO1.getExecutionPeriod().compareTo(creditLineDTO2.getExecutionPeriod());
            }
        });
        for (TeacherCredits teacherCredits : teacher.getTeacherCreditsSet()) {
            CreditLineDTO creditLineDTO =
                    new CreditLineDTO(teacherCredits.getTeacherCreditsState().getExecutionSemester(), teacherCredits);
            pastTeachingCredits.add(creditLineDTO);
            if (creditLineDTO.getCorrectionInYears().size() > 0) {
                hasAnyYearWithCorrections = true;
            }
        }
    }

    public boolean getCanSeeCreditsReduction() {
        return canSeeCreditsReduction;
    }

    public void setCanSeeCreditsReduction(boolean canSeeCreditsReduction) {
        this.canSeeCreditsReduction = canSeeCreditsReduction;
    }

}
