package de.uni_potsdam.hpi.asg.synctoolswrapper.designcompiler;

/*
 * Copyright (C) 2017 - 2018 Norman Kluge
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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.ExternalToolsInvoker;
import de.uni_potsdam.hpi.asg.common.invoker.InvokeReturn;
import de.uni_potsdam.hpi.asg.common.technology.SyncTool;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.CompileModule;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.MeasureModule;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.SetDelayModule;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.SplitSdfModule;

public class DesignCompilerInvoker extends ExternalToolsInvoker {
    private static final Logger logger = LogManager.getLogger();

    private DesignCompilerInvoker() {
        super("designcompiler");
    }

    public static String getTemplateFileName() {
        return "designcompiler.tcl";
    }

    public static InvokeReturn splitSdf(String id, File vFile, File sdcFile, Technology tech, boolean generateSdf, File sdfInFile, Set<SplitSdfModule> modules, String rootModule) {
        return new DesignCompilerInvoker().internalSplitSdf(id, vFile, sdcFile, tech, generateSdf, sdfInFile, modules, rootModule);
    }

    public static InvokeReturn measure(String id, Technology tech, Set<MeasureModule> modules, File vInFile) {
        return new DesignCompilerInvoker().internalMeasure(id, tech, modules, vInFile);
    }

    public static InvokeReturn setDelay(String id, Technology tech, Set<SetDelayModule> modules, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String rootModule) {
        return new DesignCompilerInvoker().internalSetDelay(id, tech, modules, vInFile, sdcInFile, vOutFile, sdfOutFile, rootModule);
    }

    public static boolean compileMultiple(Set<CompileModule> modules, SyncTool syncToolConfig) {
        return new DesignCompilerInvoker().internalCompileMultiple(modules, syncToolConfig);
    }

    public static InvokeReturn measureArea(Technology tech, File vInFile, File sdfInFile, String rootModule) {
        return new DesignCompilerInvoker().internalMeasureArea(tech, vInFile, sdfInFile, rootModule);
    }

    public static InvokeReturn postSynthesisOperations(Technology tech, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String rootModule) {
        return new DesignCompilerInvoker().internalPostSynthesisOperations(tech, vInFile, sdcInFile, vOutFile, sdfOutFile, rootModule);
    }

    public static InvokeReturn subsequentOperation(Technology tech, File vInFile, File sdcInFile, File vOutFile, String rootModule) {
        return new DesignCompilerInvoker().internalSubsequentOperation(tech, vInFile, sdcInFile, vOutFile, rootModule);
    }

    public static InvokeReturn translateAndUniquifyOperation(Technology tech, File vInFile, File vOutFile, String rootModule) {
        return new DesignCompilerInvoker().internalTranslateAndUniquifyOperation(tech, vInFile, vOutFile, rootModule);
    }

    private InvokeReturn internalSplitSdf(String id, File vFile, File sdcFile, Technology tech, boolean generateSdf, File sdfInFile, Set<SplitSdfModule> modules, String rootModule) {
        String tclFileName = "split.tcl";
        String logFileName = "split.log";
        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerSdfSplitScriptGenerator gen = new DesignCompilerSdfSplitScriptGenerator(tech, tclFileName, vFile, sdcFile, generateSdf, sdfInFile, modules, rootModule);

        addInputFilesToCopy(vFile);
        if(sdcFile != null) {
            addInputFilesToCopy(sdcFile);
        }

        if(!generateSdf) {
            addInputFilesToCopy(sdfInFile);
        } else {
            addOutputFilesToExport(sdfInFile);
        }
        for(SplitSdfModule mod : modules) {
            addOutputFilesToExport(mod.getSdfFile());
        }
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "dc_splitsdf_" + id, gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }
        return ret;
    }

    private InvokeReturn internalMeasure(String id, Technology tech, Set<MeasureModule> modules, File vInFile) {
        String tclFileName = "measure.tcl";
        String logFileName = "measure.log";
        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerMeasureScriptGenerator gen = new DesignCompilerMeasureScriptGenerator(tech, modules, tclFileName, vInFile);

        addInputFilesToCopy(vInFile);
        for(MeasureModule mod : modules) {
            if(!mod.getMeasureRecords().isEmpty()) {
                if(mod.getSdfFile() != null) {
                    addInputFilesToCopy(mod.getSdfFile());
                }
            }
        }
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "dc_measure_" + id, gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
            return ret;
        }
        if(!gen.parseValues()) {
            ret.setResult(false);
        }
        return ret;
    }

    private InvokeReturn internalSetDelay(String id, Technology tech, Set<SetDelayModule> modules, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String rootModule) {
        String tclFileName = "setdelay.tcl";
        String logFileName = "setdelay.log";

        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerSetDelayScriptGenerator gen = new DesignCompilerSetDelayScriptGenerator(tech, modules, tclFileName, vInFile, sdcInFile, vOutFile, sdfOutFile, rootModule);

        addInputFilesToCopy(vInFile);
        if(sdcInFile != null) {
            addInputFilesToCopy(sdcInFile);
        }
        addOutputFilesToExport(vOutFile, sdfOutFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "dc_setdelay_" + id, gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }

        return ret;
    }

    private boolean internalCompileMultiple(Set<CompileModule> modules, SyncTool syncToolConfig) {
        String tclFileName = "compile_main.tcl";
        String logFileName = "compile_main.log";

        List<String> params = generateParams(logFileName, tclFileName);

        for(CompileModule mod : modules) {
            addInputFilesToCopy(mod.getUnoptimisedFile());
            addOutputFilesToExport(mod.getOptimisedFile());
        }
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        DesignCompilerCompileScriptGenerator gen = new DesignCompilerCompileScriptGenerator(modules, syncToolConfig, tclFileName, logFileName);

        InvokeReturn ret = run(params, "dc_compile", gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
                return false;
            }
        }

        if(!gen.parseLogFile()) {
            return false;
        }

        return true;
    }

    private InvokeReturn internalMeasureArea(Technology tech, File vInFile, File sdfInFile, String rootModule) {
        String tclFileName = "measureArea.tcl";
        String logFileName = "measureArea.log";
        String areaLogFileName = "measureArea_area.log";

        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerMeasureAreaScriptGenerator gen = new DesignCompilerMeasureAreaScriptGenerator(tech, tclFileName, vInFile, sdfInFile, rootModule, areaLogFileName);

        addInputFilesToCopy(vInFile, sdfInFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName, areaLogFileName);

        InvokeReturn ret = run(params, "dc_measureArea_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }

        Float val = gen.readResult();
        if(val == null) {
            logger.error("Could not read area result");
            ret.setResult(false);
            return ret;
        }
        ret.setPayload(val);

        return ret;
    }

    private InvokeReturn internalPostSynthesisOperations(Technology tech, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String rootModule) {
        String tclFileName = "postSynOp.tcl";
        String logFileName = "postSynOp.log";
        String areaLogFileName = "postSynOp_area.log";

        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerPostSynthesisOperationsScriptGenerator gen = new DesignCompilerPostSynthesisOperationsScriptGenerator(tech, tclFileName, vInFile, sdcInFile, rootModule, vOutFile, sdfOutFile, areaLogFileName);

        addInputFilesToCopy(vInFile, sdcInFile);
        addOutputFilesToExport(vOutFile, sdfOutFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName, areaLogFileName);

        InvokeReturn ret = run(params, "dc_postSynOp_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }

        Float val = gen.readResult();
        if(val == null) {
            logger.error("Could not read area result");
            ret.setResult(false);
            return ret;
        }
        ret.setPayload(val);

        return ret;
    }

    private InvokeReturn internalSubsequentOperation(Technology tech, File vInFile, File sdcInFile, File vOutFile, String rootModule) {
        String tclFileName = "subsequent.tcl";
        String logFileName = "subsequent.log";

        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerSubequentScriptGenerator gen = new DesignCompilerSubequentScriptGenerator(tech, tclFileName, vInFile, sdcInFile, rootModule, vOutFile);

        addInputFilesToCopy(vInFile, sdcInFile);
        addOutputFilesToExport(vOutFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "dc_subsequent_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }

        return ret;
    }

    private InvokeReturn internalTranslateAndUniquifyOperation(Technology tech, File vInFile, File vOutFile, String rootModule) {
        String tclFileName = "translate.tcl";
        String logFileName = "translate.log";

        List<String> params = generateParams(logFileName, tclFileName);

        DesignCompilerTranslateScriptGenerator gen = new DesignCompilerTranslateScriptGenerator(tech, tclFileName, vInFile, rootModule, vOutFile);

        addInputFilesToCopy(vInFile);
        addOutputFilesToExport(vOutFile);
        addOutputFilesDownloadOnlyStartsWith(logFileName);

        InvokeReturn ret = run(params, "dc_translate_" + vInFile.getName(), gen);
        if(!errorHandling(ret)) {
            if(ret != null) {
                logger.error(gen.getErrorMsg(ret.getExitCode()));
            }
        }

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
