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
package pt.ist.fenixedu.teacher.evaluation.ui.struts.action.credits;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;

import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherServiceComment;
import pt.ist.fenixframework.FenixFramework;

@Mapping(path = "/teacherServiceComments", functionality = ViewTeacherCreditsDA.class)
@Forwards(value = {
        @Forward(name = "editTeacherServiceComment", path = "/teacher/evaluation/credits/degreeTeachingService/editTeacherServiceComment.jsp"),
        @Forward(name = "viewAnnualTeachingCredits", path = "/credits.do?method=viewAnnualTeachingCredits") })
public class ManageTeacherServiceCommentsDispatchAction extends FenixDispatchAction {

    public ActionForward editTeacherServiceComment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {

        TeacherServiceComment teacherServiceComment =
                FenixFramework.getDomainObject((String) getFromRequest(request, "teacherServiceCommentOid"));
        if (teacherServiceComment != null) {
            request.setAttribute("teacherServiceComment", teacherServiceComment);
        } else {
            Teacher teacher = FenixFramework.getDomainObject((String) getFromRequest(request, "teacherOid"));
            ExecutionYear executionYear = FenixFramework.getDomainObject((String) getFromRequest(request, "executionYearOid"));

            ExecutionSemester firstExecutionPeriod = executionYear.getFirstExecutionPeriod();
            TeacherService teacherService = TeacherService.getTeacherService(teacher, firstExecutionPeriod);

            request.setAttribute("teacherService", teacherService);
        }
        return mapping.findForward("editTeacherServiceComment");
    }

    public ActionForward deleteTeacherServiceComment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {

        TeacherServiceComment teacherServiceComment =
                FenixFramework.getDomainObject((String) getFromRequest(request, "teacherServiceCommentOid"));
        request.setAttribute("teacherOid", teacherServiceComment.getTeacherService().getTeacher().getExternalId());
        request.setAttribute("executionYearOid", teacherServiceComment.getTeacherService().getExecutionPeriod()
                .getExecutionYear().getExternalId());
        if (teacherServiceComment != null) {
            teacherServiceComment.delete();
        }
        return mapping.findForward("viewAnnualTeachingCredits");
    }
}
