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

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.service.services.exceptions.NotAuthorizedException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;

import pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualTeachingCredits;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.AnnualTeachingCreditsDocument;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.util.AnnualTeachingCreditsBean;
import pt.ist.fenixedu.teacher.evaluation.domain.credits.util.TeacherCreditsBean;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.TeacherService;
import pt.ist.fenixedu.teacher.evaluation.ui.struts.action.DepartmentCreditsManagerApp;
import pt.ist.fenixframework.FenixFramework;

@StrutsFunctionality(app = DepartmentCreditsManagerApp.class, path = "credits", titleKey = "label.teacher.credits")
@Mapping(path = "/credits")
@Forwards({ @Forward(name = "selectTeacher", path = "/teacher/evaluation/credits/selectTeacher.jsp"),
        @Forward(name = "showTeacherCredits", path = "/teacher/evaluation/credits/showTeacherCredits.jsp"),
        @Forward(name = "showPastTeacherCredits", path = "/teacher/evaluation/credits/showPastTeacherCredits.jsp"),
        @Forward(name = "showAnnualTeacherCredits", path = "/teacher/evaluation/credits/showAnnualTeacherCredits.jsp") })
public class ViewTeacherCreditsDA extends FenixDispatchAction {

    @EntryPoint
    public ActionForward prepareTeacherSearch(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {
        request.setAttribute("teacherBean", new TeacherCreditsBean());
        return mapping.findForward("selectTeacher");
    }

    public ActionForward showTeacherCredits(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException, Exception {
        TeacherCreditsBean teacherBean = getRenderedObject();

        if (!isTeacherOfManageableDepartments(teacherBean.getTeacher())) {
            addActionMessage("error", request, "message.teacher.not-found-or-not-belong-to-department");
            return prepareTeacherSearch(mapping, form, request, response);
        }

        teacherBean.prepareAnnualTeachingCredits(Authenticate.getUser());
        request.setAttribute("teacherBean", teacherBean);
        return mapping.findForward("showTeacherCredits");
    }

    private boolean isTeacherOfManageableDepartments(Teacher teacher) {
        User user = Authenticate.getUser();
        return RoleType.SCIENTIFIC_COUNCIL.isMember(user)
                || user.getPerson().getManageableDepartmentCreditsSet().contains(teacher.getDepartment());
    }

    public ActionForward viewAnnualTeachingCredits(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException, Exception {

        Teacher teacher = FenixFramework.getDomainObject((String) getFromRequest(request, "teacherOid"));

        User user = Authenticate.getUser();
        ExecutionYear executionYear = FenixFramework.getDomainObject((String) getFromRequest(request, "executionYearOid"));
        if (teacher == null) {
            Professorship professorship = FenixFramework.getDomainObject(getStringFromRequest(request, "professorshipID"));
            if (professorship != null) {
                teacher = professorship.getTeacher();
                executionYear = professorship.getExecutionCourse().getExecutionYear();
            }
        }

        boolean withConfidencialInformation =
                (RoleType.SCIENTIFIC_COUNCIL.isMember(user) || teacher.getPerson().getUser().equals(user));

        AnnualTeachingCreditsBean annualTeachingCreditsBean = null;

        for (AnnualTeachingCredits annualTeachingCredits : teacher.getAnnualTeachingCreditsSet()) {
            if (annualTeachingCredits.getAnnualCreditsState().getExecutionYear().equals(executionYear)) {
                if (annualTeachingCredits.isPastResume()) {
                    TeacherCreditsBean teacherBean = new TeacherCreditsBean(teacher);
                    teacherBean.preparePastTeachingCredits();
                    request.setAttribute("teacherBean", teacherBean);
                    return mapping.findForward("showPastTeacherCredits");
                } else {
                    if (annualTeachingCredits.isClosed()) {
                        AnnualTeachingCreditsDocument lastTeacherCreditsDocument =
                                annualTeachingCredits.getLastTeacherCreditsDocument(withConfidencialInformation);
                        if (lastTeacherCreditsDocument != null) {
                            response.setContentType("application/pdf");
                            response.setHeader("Content-disposition",
                                    "attachment; filename=" + lastTeacherCreditsDocument.getFilename());
                            final OutputStream outputStream = response.getOutputStream();
                            outputStream.write(lastTeacherCreditsDocument.getContent());
                            outputStream.close();
                            return null;
                        } else {
                            throw new NotAuthorizedException();
                        }
                    }
                    annualTeachingCreditsBean = new AnnualTeachingCreditsBean(annualTeachingCredits);
                    break;
                }
            }
        }
        if (annualTeachingCreditsBean == null) {
            annualTeachingCreditsBean = new AnnualTeachingCreditsBean(executionYear, teacher);
        }
        request.setAttribute("annualTeachingCreditsBean", annualTeachingCreditsBean);
        request.setAttribute("teacherBean", new TeacherCreditsBean());
        return mapping.findForward("showAnnualTeacherCredits");
    }

    public ActionForward recalculateCredits(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException, Exception {
        Teacher teacher = FenixFramework.getDomainObject((String) getFromRequest(request, "teacherOid"));
        ExecutionYear executionYear = FenixFramework.getDomainObject((String) getFromRequest(request, "executionYearOid"));
        AnnualTeachingCredits annualTeachingCredits = AnnualTeachingCredits.readByYearAndTeacher(executionYear, teacher);
        if (annualTeachingCredits != null) {
            annualTeachingCredits.calculateCredits();
        }
        return viewAnnualTeachingCredits(mapping, form, request, response);
    }

    public ActionForward unlockTeacherCredits(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException, Exception {
        Teacher teacher = FenixFramework.getDomainObject((String) getFromRequest(request, "teacherOid"));
        ExecutionSemester executionSemester =
                FenixFramework.getDomainObject((String) getFromRequest(request, "executionPeriodOid"));
        TeacherService teacherService = TeacherService.getTeacherService(teacher, executionSemester);
        teacherService.unlockTeacherCredits();
        request.setAttribute("teacherOid", teacher.getExternalId());
        request.setAttribute("executionYearOid", executionSemester.getExecutionYear().getExternalId());
        return viewAnnualTeachingCredits(mapping, form, request, response);
    }

}