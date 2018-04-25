package de.uni_potsdam.hpi.asg.synctoolswrapper;

/*
 * Copyright (C) 2018 Norman Kluge
 * 
 * This file is part of ASGwrapper-synctools.
 * 
 * ASGwrapper-synctools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGwrapper-synctools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGwrapper-synctools.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.ExternalToolsInvoker;
import de.uni_potsdam.hpi.asg.common.invoker.InvokeReturn;
import de.uni_potsdam.hpi.asg.common.invoker.config.ToolConfig;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.encounter.EncounterLayoutScriptGenerator;

public class EncounterInvoker extends ExternalToolsInvoker {
    private static final Logger logger = LogManager.getLogger();

    private EncounterInvoker() {
        super("encounterBash");
    }

    protected static List<String> getTemplateFileName() {
        return Arrays.asList("encounter.sh", "encounter.tcl");
    }

    public static InvokeReturn layoutOperation(Technology tech, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile) {
        return new EncounterInvoker().internalLayoutOperation(tech, vInFile, sdcInFile, vOutFile, sdfOutFile);
    }

    private InvokeReturn internalLayoutOperation(Technology tech, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile) {
        ToolConfig cfg = getToolConfig("encounter");
        if(cfg == null) {
            return null;
        }
        String exec = cfg.getCmdline();
        String shFileName = "layout.sh";
        String tclFileName = "layout.tcl";
        String logFileName = "layout.log";
        String areaLogFileName = "layout_area.log";

        List<String> params = new ArrayList<>();
        params.add(shFileName);

        EncounterLayoutScriptGenerator gen = new EncounterLayoutScriptGenerator(tech, exec, shFileName, tclFileName, vInFile, sdcInFile, vOutFile, sdfOutFile, logFileName, areaLogFileName);

        addInputFilesToCopy(vInFile, sdcInFile);
        addOutputFilesToExport(vOutFile, sdfOutFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName, areaLogFileName);

        InvokeReturn ret = run(params, "enc_layout_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                String msg = "Layout ret code: " + ret.getExitCode();
                logger.error(msg);
                ret.setErrorMsg(msg);
            }
        }

        Float area = gen.getAreaValue();
        if(area == null) {
            logger.error("Could not read area result");
            ret.setResult(false);
            return ret;
        }
        ret.setPayload(area);

        return ret;
    }
}
