/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.ui.struts.action.publico;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academic.dto.inquiries.BlockResultsSummaryBean;
import org.fenixedu.academic.dto.inquiries.GroupResultsSummaryBean;
import org.fenixedu.academic.dto.inquiries.QuestionResultsSummaryBean;
import org.fenixedu.academic.dto.inquiries.TeacherShiftTypeGeneralResultBean;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.inquiries.CurricularCourseInquiryTemplate;
import org.fenixedu.academic.domain.inquiries.GroupResultType;
import org.fenixedu.academic.domain.inquiries.InquiriesRoot;
import org.fenixedu.academic.domain.inquiries.InquiryBlock;
import org.fenixedu.academic.domain.inquiries.InquiryGroupQuestion;
import org.fenixedu.academic.domain.inquiries.InquiryQuestion;
import org.fenixedu.academic.domain.inquiries.InquiryResult;
import org.fenixedu.academic.domain.inquiries.InquiryResultType;
import org.fenixedu.academic.domain.inquiries.ResultClassification;
import org.fenixedu.academic.domain.inquiries.ResultsInquiryTemplate;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.bennu.struts.annotations.Mapping;

import pt.ist.fenixframework.FenixFramework;

@Mapping(path = "/viewCourseResults", module = "publico")
public class ViewCourseInquiryPublicResults extends ViewInquiryPublicResults {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        return getCourseResultsActionForward(mapping, actionForm, request, response);
    }

    public static ActionForward getCourseResultsActionForward(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        String executionCourseId = request.getParameter("executionCourseOID");
        if (executionCourseId == null) {
            executionCourseId = request.getParameter("executionCourseID");
        }
        ExecutionCourse executionCourse = FenixFramework.getDomainObject(executionCourseId);
        ExecutionSemester executionPeriod = executionCourse.getExecutionPeriod();

        CurricularCourseInquiryTemplate curricularCourseInquiryTemplate =
                CurricularCourseInquiryTemplate.getTemplateByExecutionPeriod(executionPeriod);
        if (curricularCourseInquiryTemplate == null) {
            request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.m1");
            return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
        }

        DegreeCurricularPlan dcp = FenixFramework.getDomainObject(request.getParameter("degreeCurricularPlanOID"));
        ExecutionDegree executionDegree = dcp.getExecutionDegreeByAcademicInterval(executionPeriod.getAcademicInterval());
        List<InquiryResult> results =
                InquiryResult.getInquiryResultsByExecutionDegreeAndForTeachers(executionCourse, executionDegree);
        boolean hasNotRelevantData = InquiryResult.hasNotRelevantDataFor(executionCourse, executionDegree);

        ResultsInquiryTemplate resultsInquiryTemplate = ResultsInquiryTemplate.getTemplateByExecutionPeriod(executionPeriod);
        Collection<InquiryBlock> resultBlocks = resultsInquiryTemplate.getInquiryBlocksSet();

        GroupResultsSummaryBean ucGroupResultsSummaryBean =
                getGeneralResults(results, resultBlocks, GroupResultType.COURSE_RESULTS);
        GroupResultsSummaryBean answersResultsSummaryBean =
                getGeneralResults(results, resultBlocks, GroupResultType.COURSE_ANSWERS);
        GroupResultsSummaryBean nonAnswersResultsSummaryBean =
                getGeneralResults(results, resultBlocks, GroupResultType.COURSE_NON_ANSWERS);
        if (InquiriesRoot.getAvailableForInquiries(executionCourse)) {
            Collections.sort(nonAnswersResultsSummaryBean.getQuestionsResults(), new BeanComparator("questionResult.value"));
            Collections.reverse(nonAnswersResultsSummaryBean.getQuestionsResults());
        }

        CurricularCourse curricularCourse = executionCourse.getCurricularCourseFor(dcp);
        Double ects = curricularCourse.getEctsCredits(executionPeriod);
        Double contactLoadEcts = curricularCourse.getContactLoad() / 28;
        Double autonumousWorkEcts = ects - contactLoadEcts;

        GroupResultsSummaryBean workLoadSummaryBean = getGeneralResults(results, resultBlocks, GroupResultType.WORKLOAD);
        request.setAttribute("contactLoadEcts", contactLoadEcts);
        request.setAttribute("autonumousWorkEcts", autonumousWorkEcts);
        GroupResultsSummaryBean ucGeneralDataSummaryBean =
                getGeneralResults(results, resultBlocks, GroupResultType.COURSE_GENERAL_DATA);
        GroupResultsSummaryBean ucEvaluationsGroupBean =
                getGeneralResults(results, resultBlocks, GroupResultType.COURSE_EVALUATIONS);
        InquiryQuestion estimatedEvaluationQuestion =
                getEstimatedEvaluationsQuestion(curricularCourseInquiryTemplate.getInquiryBlocksSet());
        QuestionResultsSummaryBean estimatedEvaluationBeanQuestion =
                new QuestionResultsSummaryBean(estimatedEvaluationQuestion, getResultsForQuestion(results,
                        estimatedEvaluationQuestion), null, null);

        InquiryQuestion totalAnswersQuestion = getInquiryQuestion(results, InquiryResultType.COURSE_TOTAL_ANSWERS);
        request.setAttribute("totalAnswers",
                new QuestionResultsSummaryBean(totalAnswersQuestion, getResultsForQuestion(results, totalAnswersQuestion)
                        .iterator().next()));

        List<TeacherShiftTypeGeneralResultBean> teachersSummaryBeans = getTeachersShiftsResults(executionCourse);
        Collections.sort(teachersSummaryBeans, new BeanComparator("professorship.person.name"));
        Collections.sort(teachersSummaryBeans, new BeanComparator("shiftType"));

        ResultClassification auditResult = getAuditResult(results);
        if (auditResult != null) {
            request.setAttribute("auditResult", auditResult.name());
        }

        request.setAttribute("ucGroupResultsSummaryBean", ucGroupResultsSummaryBean);
        request.setAttribute("answersResultsSummaryBean", answersResultsSummaryBean);
        request.setAttribute("nonAnswersResultsSummaryBean", nonAnswersResultsSummaryBean);
        request.setAttribute("workLoadSummaryBean", workLoadSummaryBean);
        request.setAttribute("ucGeneralDataSummaryBean", ucGeneralDataSummaryBean);
        request.setAttribute("ucEvaluationsGroupBean", ucEvaluationsGroupBean);
        request.setAttribute("estimatedEvaluationBeanQuestion", estimatedEvaluationBeanQuestion);
        request.setAttribute("teachersSummaryBeans", teachersSummaryBeans);

        CurricularCourseInquiryTemplate courseInquiryTemplate =
                CurricularCourseInquiryTemplate.getTemplateByExecutionPeriod(executionPeriod);
        List<BlockResultsSummaryBean> blockResultsSummaryBeans = new ArrayList<BlockResultsSummaryBean>();

        for (InquiryBlock inquiryBlock : courseInquiryTemplate.getInquiryBlocksSet()) {
            blockResultsSummaryBeans.add(new BlockResultsSummaryBean(inquiryBlock, results, null, null));
        }
        Collections.sort(blockResultsSummaryBeans, new BeanComparator("inquiryBlock.blockOrder"));

        request.setAttribute("hasNotRelevantData", hasNotRelevantData);
        request.setAttribute("executionCourse", executionCourse);
        request.setAttribute("executionPeriod", executionPeriod);
        request.setAttribute("executionDegree", executionDegree);
        request.setAttribute("resultsDate", results.iterator().next().getResultDate());
        request.setAttribute("blockResultsSummaryBeans", blockResultsSummaryBeans);

        request.setAttribute("publicContext", true);
        return new ActionForward(null, "/inquiries/showCourseInquiryResult_v3.jsp", false, "/teacher");
    }

    private static InquiryQuestion getInquiryQuestion(List<InquiryResult> results, InquiryResultType courseTotalAnswers) {
        for (InquiryResult inquiryResult : results) {
            if (InquiryResultType.COURSE_TOTAL_ANSWERS.equals(inquiryResult.getResultType())) {
                return inquiryResult.getInquiryQuestion();
            }
        }
        return null;
    }

    private static List<InquiryResult> getResultsForQuestion(List<InquiryResult> results, InquiryQuestion inquiryQuestion) {
        List<InquiryResult> questionResults = new ArrayList<InquiryResult>();
        for (InquiryResult inquiryResult : results) {
            if (inquiryResult.getInquiryQuestion() == inquiryQuestion) {
                questionResults.add(inquiryResult);
            }
        }
        return questionResults;
    }

    private static InquiryQuestion getEstimatedEvaluationsQuestion(Collection<InquiryBlock> inquiryBlocks) {
        for (InquiryBlock inquiryBlock : inquiryBlocks) {
            for (InquiryGroupQuestion inquiryGroupQuestion : inquiryBlock.getInquiryGroupsQuestionsSet()) {
                for (InquiryQuestion inquiryQuestion : inquiryGroupQuestion.getInquiryQuestionsSet()) {
                    if (!inquiryQuestion.getPresentResults()) {
                        return inquiryQuestion;
                    }
                }
            }
        }
        return null;
    }

    private static List<TeacherShiftTypeGeneralResultBean> getTeachersShiftsResults(ExecutionCourse executionCourse) {
        List<TeacherShiftTypeGeneralResultBean> teachersSummaries = new ArrayList<TeacherShiftTypeGeneralResultBean>();
        for (InquiryResult inquiryResult : executionCourse.getInquiryResultsSet()) {
            if (InquiryResultType.TEACHER_SHIFT_TYPE.equals(inquiryResult.getResultType())) {
                teachersSummaries.add(new TeacherShiftTypeGeneralResultBean(inquiryResult.getProfessorship(), inquiryResult
                        .getShiftType(), inquiryResult));
            }
        }
        return teachersSummaries;
    }

    private static ResultClassification getAuditResult(List<InquiryResult> results) {
        for (InquiryResult inquiryResult : results) {
            if (InquiryResultType.AUDIT.equals(inquiryResult.getResultType())) {
                return inquiryResult.getResultClassification();
            }
        }
        return null;
    }
}
