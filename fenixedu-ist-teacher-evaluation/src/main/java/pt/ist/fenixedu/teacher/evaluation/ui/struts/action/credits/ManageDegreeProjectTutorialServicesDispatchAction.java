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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;

import pt.ist.fenixedu.teacher.evaluation.domain.credits.util.ProjectTutorialServiceBean;
import pt.ist.fenixedu.teacher.evaluation.domain.teacher.DegreeProjectTutorialService;
import pt.ist.fenixframework.FenixFramework;

@Mapping(path = "/degreeProjectTutorialService", functionality = ViewTeacherCreditsDA.class)
@Forwards(value = {
        @Forward(name = "show-project-tutorial-service", path = "/teacher/evaluation/credits/degreeTeachingService/showProjectTutorialService.jsp"),
        @Forward(name = "viewAnnualTeachingCredits", path = "/credits.do?method=viewAnnualTeachingCredits") })
public class ManageDegreeProjectTutorialServicesDispatchAction extends FenixDispatchAction {

    public ActionForward showProjectTutorialServiceDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws NumberFormatException, FenixServiceException {

        String professorshipID = (String) getFromRequest(request, "professorshipID");
        Professorship professorship = FenixFramework.getDomainObject(professorshipID);
        if (professorship == null) {
            return mapping.findForward("teacher-not-found");
        }
        List<ProjectTutorialServiceBean> projectTutorialServiceBeans = new ArrayList<ProjectTutorialServiceBean>();
        for (Attends attend : professorship.getExecutionCourse().getAttendsSet()) {
            if (attend.getEnrolment() != null) {
                ProjectTutorialServiceBean projectTutorialServiceBean = new ProjectTutorialServiceBean(professorship, attend);
                projectTutorialServiceBeans.add(projectTutorialServiceBean);
            }
        }

        request.setAttribute("professorship", professorship);
        request.setAttribute("projectTutorialServiceBeans", projectTutorialServiceBeans);
        return mapping.findForward("show-project-tutorial-service");
    }

    public ActionForward updateProjectTutorialService(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String professorshipID = (String) getFromRequest(request, "professorshipID");
        Professorship professorship = FenixFramework.getDomainObject(professorshipID);
        List<ProjectTutorialServiceBean> projectTutorialServiceBeans = getRenderedObject("projectTutorialService");
        try {
            DegreeProjectTutorialService.updateProjectTutorialService(projectTutorialServiceBeans);
        } catch (DomainException domainException) {
            addActionMessage("error", request, domainException.getMessage());
            request.setAttribute("professorship", professorship);
            request.setAttribute("projectTutorialServiceBeans", projectTutorialServiceBeans);
            return mapping.findForward("show-project-tutorial-service");
        }
        request.setAttribute("teacherOid", professorship.getTeacher().getExternalId());
        request.setAttribute("executionYearOid", professorship.getExecutionCourse().getExecutionPeriod().getExecutionYear()
                .getNextExecutionYear().getExternalId());
        return mapping.findForward("viewAnnualTeachingCredits");
    }

}
