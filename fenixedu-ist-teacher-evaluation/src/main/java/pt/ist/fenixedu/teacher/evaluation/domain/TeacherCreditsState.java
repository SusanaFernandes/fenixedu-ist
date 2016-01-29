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
package pt.ist.fenixedu.teacher.evaluation.domain;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

import pt.ist.fenixedu.teacher.evaluation.domain.CreditsState;

public class TeacherCreditsState extends TeacherCreditsState_Base {

    public TeacherCreditsState(ExecutionSemester executionSemester) {
        super();
        setExecutionSemester(executionSemester);
        setBasicOperations();
        setCloseState();
        setRootDomainObject(Bennu.getInstance());
    }

    public boolean isOpenState() {
        return getCreditState() == CreditsState.OPEN;
    }

    public boolean isCloseState() {
        return getCreditState() == CreditsState.CLOSE;
    }

    public void setOpenState() {
        setCreditState(CreditsState.OPEN);
        setBasicOperations();
    }

    public void setCloseState() {
        setCreditState(CreditsState.CLOSE);
        setBasicOperations();
    }

    private void setBasicOperations() {
        setPerson(AccessControl.getPerson());
        setLastModifiedDate(new DateTime());
    }

    public static TeacherCreditsState getTeacherCreditsState(ExecutionSemester executionSemester) {
        for (TeacherCreditsState teacherCreditsState : Bennu.getInstance().getTeacherCreditsStateSet()) {
            if (teacherCreditsState.getExecutionSemester().equals(executionSemester)) {
                return teacherCreditsState;
            }
        }
        return null;
    }

}
