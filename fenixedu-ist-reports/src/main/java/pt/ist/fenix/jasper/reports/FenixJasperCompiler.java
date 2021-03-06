/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Report Templates.
 *
 * FenixEdu IST Report Templates is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Report Templates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Report Templates.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenix.jasper.reports;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRAbstractMultiClassCompiler;

public class FenixJasperCompiler extends JRAbstractMultiClassCompiler {

    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    @Override
    public String compileClasses(File[] sourceFiles, String classpath) throws JRException {
        // Retrieve files from the standard location
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFiles));

        CompilationTask task =
                compiler.getTask(new StringWriter(), fileManager, null, Arrays.asList("-classpath", classpath), null, sources);

        if (!task.call()) {
            throw new JRException("Error compiling report java source files");
        }

        return null;
    }

}