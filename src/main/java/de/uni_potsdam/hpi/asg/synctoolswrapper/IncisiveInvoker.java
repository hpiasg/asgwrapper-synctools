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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.ExternalToolsInvoker;
import de.uni_potsdam.hpi.asg.common.invoker.InvokeReturn;
import de.uni_potsdam.hpi.asg.common.invoker.config.ToolConfig;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.incisive.IncisiveSimulationScriptGenerator;

public class IncisiveInvoker extends ExternalToolsInvoker {
    private static final Logger logger = LogManager.getLogger();

    public enum ExecType {
        vCompiler, sdfCompiler, elaborator, simulator
    }

    private static Map<ExecType, String> execs;

    private IncisiveInvoker() {
        super("incisiveBash");
    }

    protected static List<String> getTemplateFileName() {
        return Arrays.asList("incisive.sh", "incisive.tcl", "incisive_sdf.com");
    }

    public static InvokeReturn simulation(Technology tech, File vInFile, File tbInFile, File sdfInFile, File vcdOutFile, File simLogFile, String tbRoot, String sdfScope) {
        return new IncisiveInvoker().internalSimulation(tech, vInFile, tbInFile, sdfInFile, vcdOutFile, simLogFile, tbRoot, sdfScope);
    }

    private InvokeReturn internalSimulation(Technology tech, File vInFile, File tbInFile, File sdfInFile, File vcdOutFile, File simLogFile, String tbRoot, String sdfScope) {
        if(!checkCreateExecs()) {
            logger.error("Could not find all execs");
            return null;
        }

        String shGuiFileName = "simulation_gui.sh";
        String shTerminalFileName = "simulation_terminal.sh";
        String tclFileName = "simulation.tcl";
        String sdfCmdFileName = "simulation.tcl";
        String logFileName = "simulation.log";

        List<String> params = new ArrayList<>();
        params.add(shTerminalFileName);

        IncisiveSimulationScriptGenerator gen = new IncisiveSimulationScriptGenerator();
        gen.setTech(tech);
        gen.setExecs(execs);
        gen.setShGuiFileName(shGuiFileName);
        gen.setShTerminalFileName(shTerminalFileName);
        gen.setTclFileName(tclFileName);
        gen.setSdfCmdFileName(sdfCmdFileName);
        gen.setLogFileName(logFileName);
        gen.setvInFile(vInFile);
        gen.setTbInFile(tbInFile);
        gen.setSdfInFile(sdfInFile);
        gen.setVcdOutFile(vcdOutFile);
        gen.setSimLogFile(simLogFile);
        gen.setTbRoot(tbRoot);
        gen.setSdfScope(sdfScope);

        addInputFilesToCopy(vInFile, tbInFile, sdfInFile);
        addOutputFilesToExport(vcdOutFile, simLogFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "inc_sim_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                String msg = gen.getErrorMsg(ret.getExitCode());
                logger.error(msg);
                ret.setErrorMsg(msg);
            }
        }

        return ret;
    }

    private boolean checkCreateExecs() {
        if(execs == null) {
            execs = new HashMap<>();

            ToolConfig cfg = getToolConfig("incisiveCompilerVerilog");
            if(cfg == null) {
                return false;
            }
            execs.put(ExecType.vCompiler, cfg.getCmdline());

            cfg = getToolConfig("incisiveCompilerSdf");
            if(cfg == null) {
                return false;
            }
            execs.put(ExecType.sdfCompiler, cfg.getCmdline());

            cfg = getToolConfig("incisiveElaborator");
            if(cfg == null) {
                return false;
            }
            execs.put(ExecType.elaborator, cfg.getCmdline());

            cfg = getToolConfig("incisiveSimulator");
            if(cfg == null) {
                return false;
            }
            execs.put(ExecType.simulator, cfg.getCmdline());
        }

        return true;
    }
}
