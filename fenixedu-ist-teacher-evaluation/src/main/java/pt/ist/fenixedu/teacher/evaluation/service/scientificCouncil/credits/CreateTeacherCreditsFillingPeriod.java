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
package pt.ist.fenixedu.teacher.evaluation.service.scientificCouncil.credits;

import static org.fenixedu.academic.predicate.AccessControl.check;

import org.fenixedu.academic.predicate.RolePredicates;

import pt.ist.fenixedu.teacher.evaluation.domain.time.calendarStructure.TeacherCreditsFillingCE;
import pt.ist.fenixedu.teacher.evaluation.dto.teacherCredits.TeacherCreditsPeriodBean;
import pt.ist.fenixframework.Atomic;

public class CreateTeacherCreditsFillingPeriod {

    @Atomic
    public static void run(TeacherCreditsPeriodBean bean) {
        check(RolePredicates.SCIENTIFIC_COUNCIL_PREDICATE);
        if (bean != null) {
            if (bean.isTeacher()) {
                TeacherCreditsFillingCE.editTeacherCreditsPeriod(bean.getExecutionPeriod(), bean.getBeginForTeacher(),
                        bean.getEndForTeacher());
            } else {
                TeacherCreditsFillingCE.editDepartmentOfficeCreditsPeriod(bean.getExecutionPeriod(),
                        bean.getBeginForDepartmentAdmOffice(), bean.getEndForDepartmentAdmOffice());
            }
        }
    }
}