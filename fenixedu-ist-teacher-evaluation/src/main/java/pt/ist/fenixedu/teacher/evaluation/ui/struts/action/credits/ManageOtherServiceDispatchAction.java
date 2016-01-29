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
/**
 *  Apr 21, 2006
 */
package pt.ist.fenixedu.teacher.evaluation.ui.struts.action.credits;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.ExceptionHandling;
import org.fenixedu.bennu.struts.annotations.Exceptions;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;

import pt.ist.fenixedu.teacher.evaluation.domain.teacher.OtherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;
import pt.ist.fenixframework.FenixFramework;

@Mapping(path = "/otherServiceManagement", functionality = ManageCreditsPeriods.class)
@Forwards(value = { @Forward(name = "editOtherService", path = "/teacher/evaluation/credits/otherService/editOtherService.jsp"),
        @Forward(name = "viewAnnualTeachingCredits", path = "/credits.do?method=viewAnnualTeachingCredits") })
@Exceptions(value = { @ExceptionHandling(type = org.fenixedu.academic.domain.exceptions.DomainException.class,
        handler = org.fenixedu.academic.ui.struts.config.FenixDomainExceptionHandler.class, scope = "request") })
public class ManageOtherServiceDispatchAction extends FenixDispatchAction {

    @EntryPoint
    public ActionForward prepareEditOtherService(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {
        OtherService otherService = FenixFramework.getDomainObject((String) getFromRequest(request, "otherServiceOid"));
        if (otherService != null) {
            request.setAttribute("otherService", otherService);
        } else {
            ExecutionSemester executionSemester =
                    FenixFramework.getDomainObject((String) getFromRequest(request, "executionPeriodOid"));
            Teacher teacher = FenixFramework.getDomainObject((String) getFromRequest(request, "teacherId"));
            TeacherService teacherService = TeacherService.getTeacherService(teacher, executionSemester);
            request.setAttribute("teacherService", teacherService);
        }
        return mapping.findForward("editOtherService");
    }

    public ActionForward deleteOtherService(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {
        OtherService otherService = FenixFramework.getDomainObject((String) getFromRequest(request, "otherServiceOid"));
        request.setAttribute("teacherOid", otherService.getTeacherService().getTeacher().getExternalId());
        request.setAttribute("executionYearOid", otherService.getTeacherService().getExecutionPeriod().getExecutionYear()
                .getExternalId());
        otherService.delete();
        return mapping.findForward("viewAnnualTeachingCredits");
    }
}