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
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.ExternalToolsInvoker;
import de.uni_potsdam.hpi.asg.common.invoker.InvokeReturn;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.primetime.PrimeTimeMeasurePowerScriptGenerator;
import de.uni_potsdam.hpi.asg.synctoolswrapper.primetime.PrimeTimePowerResultsReader;

public class PrimeTimeInvoker extends ExternalToolsInvoker {
    private static final Logger logger = LogManager.getLogger();

    private PrimeTimeInvoker() {
        super("primetime");
    }

    protected static String getTemplateFileName() {
        return "primetime.tcl";
    }

    public static InvokeReturn measurePower(Technology tech, File vInFile, File vcdInFile, String vcdScope, File outFile, String rootModule, String timesStr) {
        return new PrimeTimeInvoker().internalMeasurePower(tech, vInFile, vcdInFile, vcdScope, outFile, rootModule, timesStr);
    }

    private InvokeReturn internalMeasurePower(Technology tech, File vInFile, File vcdInFile, String vcdScope, File outFile, String rootModule, String timesStr) {
        String tclFileName = "power.tcl";
        String logFileName = "power.log";
        String powerLogFileName = "power_power.log";

        List<String> params = generateParams(logFileName, tclFileName);

        PrimeTimeMeasurePowerScriptGenerator gen = new PrimeTimeMeasurePowerScriptGenerator(tech, tclFileName, vInFile, vcdInFile, vcdScope, outFile, rootModule, timesStr, powerLogFileName);

        addInputFilesToCopy(vInFile, vcdInFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName, outFile.getName(), powerLogFileName);

        InvokeReturn ret = run(params, "pt_power_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            String msg = "Primetime failed: " + gen.getErrorMsg(ret.getExitCode());
            logger.error(msg);
            ret.setErrorMsg(msg);
        }

        PrimeTimePowerResultsReader results = new PrimeTimePowerResultsReader();
        if(!results.parseFiles(gen.getPowerLogFile(), timesStr, rootModule)) {
            String msg = "Primetime failed: Parsing log files";
            logger.error(msg);
            ret.setErrorMsg(msg);
        }
        ret.setPayload(results);

        return ret;
    }

    private List<String> generateParams(String logFileName, String tclFileName) {
        //@formatter:off
        return Arrays.asList(
            "-f", tclFileName,
            ">", logFileName
        );
        //@formatter:on
    }
}
