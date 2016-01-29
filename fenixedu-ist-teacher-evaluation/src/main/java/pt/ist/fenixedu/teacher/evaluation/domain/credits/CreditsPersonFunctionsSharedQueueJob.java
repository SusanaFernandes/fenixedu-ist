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
package pt.ist.fenixedu.teacher.evaluation.domain.credits;

import java.math.BigDecimal;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.QueueJobResult;

import pt.ist.fenixedu.contracts.domain.organizationalStructure.PersonFunction;
import pt.ist.fenixedu.contracts.domain.organizationalStructure.PersonFunctionShared;
import pt.ist.fenixedu.contracts.domain.organizationalStructure.SharedFunction;

public class CreditsPersonFunctionsSharedQueueJob extends CreditsPersonFunctionsSharedQueueJob_Base {

    public CreditsPersonFunctionsSharedQueueJob(ExecutionSemester executionSemester) {
        super();
        setExecutionSemester(executionSemester);
    }

    @Override
    public QueueJobResult execute() throws Exception {
        ExecutionSemester previousExecutionSemester =
                getExecutionSemester().getExecutionYear().getPreviousExecutionYear()
                        .getExecutionSemesterFor(getExecutionSemester().getSemester());
        for (PersonFunction personFunction : previousExecutionSemester.getPersonFunctionSet()) {
            if (personFunction instanceof PersonFunctionShared) {
                PersonFunctionShared personFunctionShared = (PersonFunctionShared) personFunction;
                SharedFunction sharedFunction = personFunctionShared.getSharedFunction();
                if (sharedFunction.getEndDateYearMonthDay() == null) {
                    sharedFunction.setEndDateYearMonthDay(previousExecutionSemester.getEndDateYearMonthDay());
                    new SharedFunction(sharedFunction.getTypeName(), getExecutionSemester().getBeginDateYearMonthDay(), null,
                            sharedFunction.getFunctionType(), sharedFunction.getUnit(), BigDecimal.ZERO);
                }
            }
        }
        return new QueueJobResult();
    }

}
