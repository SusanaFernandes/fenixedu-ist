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
package pt.ist.fenixedu.teacher.evaluation.ui.struts.action.credits.departmentMember;

import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;

import pt.ist.fenixedu.teacher.evaluation.ui.struts.action.credits.ManageDegreeProjectTutorialServicesDispatchAction;

@Mapping(module = "departmentMember", path = "/degreeProjectTutorialService",
        functionality = DepartmentMemberViewTeacherCreditsDA.class)
@Forwards(value = {
        @Forward(name = "show-project-tutorial-service", path = "/teacher/evaluation/credits/degreeTeachingService/showProjectTutorialService.jsp"),
        @Forward(name = "viewAnnualTeachingCredits", path = "/departmentMember/credits.do?method=viewAnnualTeachingCredits") })
public class DepartmentMemberManageDegreeProjectTutorialServicesDA extends ManageDegreeProjectTutorialServicesDispatchAction {

}
