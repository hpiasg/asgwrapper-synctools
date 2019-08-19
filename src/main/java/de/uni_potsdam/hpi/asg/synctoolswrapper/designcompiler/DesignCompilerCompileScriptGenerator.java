package de.uni_potsdam.hpi.asg.synctoolswrapper.designcompiler;

/*
 * Copyright (C) 2017 Norman Kluge
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.common.technology.SyncTool;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.CompileModule;

public class DesignCompilerCompileScriptGenerator extends DesignCompilerAbstractScriptGenerator {

    private final Logger       logger         = LogManager.getLogger();

    private final String       ASG_ID_STR     = "ASGtools";
    private final Pattern      RESULT_PATTERN = Pattern.compile(";" + ASG_ID_STR + ";([\\w_]+);(\\d+);");

    private Set<CompileModule> modules;
    private SyncTool           syncToolConfig;
    private String             tclFileName;
    private String             logFileName;
    private File               targetDir;

    public DesignCompilerCompileScriptGenerator(Set<CompileModule> modules, SyncTool syncToolConfig, String tclFileName, String logFileName) {
        this.modules = modules;
        this.syncToolConfig = syncToolConfig;
        this.tclFileName = tclFileName;
        this.logFileName = logFileName;
    }

    @Override
    public boolean generate(File targetDir) {
        this.targetDir = targetDir;
        File tclFile = new File(targetDir, tclFileName);

        if(!generateTclFiles(targetDir, tclFile)) {
            return false;
        }
        addDownloadIncludeFileNames(logFileName);

        return true;
    }

    public boolean parseLogFile() {
        File logFile = new File(targetDir, logFileName);
        List<String> lines = FileHelper.getInstance().readFile(logFile);
        if(lines == null) {
            return false;
        }
        Map<String, Boolean> results = new HashMap<>();
        for(String line : lines) {
            Matcher m = RESULT_PATTERN.matcher(line);
            if(m.matches()) {
                String component = m.group(1);
                int result = Integer.parseInt(m.group(2));
                if(result == 0) { // ok
                    results.put(component, true);
                } else { // error
                    logger.warn("Data path optimisation for component '" + component + "' failed: " + getErrorMsg(result));
                    results.put(component, false);
                }
            }
        }

        for(CompileModule mod : modules) {
            if(!results.containsKey(mod.getModuleName())) {
                logger.warn("No result for component '" + mod.getModuleName() + "'");
                continue;
            }
            mod.setOptmisationSuccessful(results.get(mod.getModuleName()));
        }

        return true;
    }

    private boolean generateTclFiles(File targetDir, File tclFile) {
        Map<String, String> replacements = new HashMap<>();
        List<String> code = new ArrayList<>();

        for(CompileModule mod : modules) {
            File compTclFile = new File(targetDir, mod.getUnoptimisedFile().getName().replace(CommonConstants.VERILOG_FILE_EXTENSION, ".tcl"));

            replacements.clear();
            replacements.put("dc_tcl_component", mod.getModuleName());
            replacements.put("dc_tcl_component_tclfile", compTclFile.getName());
            replacements.put("dc_tcl_asgidstr", ASG_ID_STR);

            List<String> tmpCode = replaceInTemplate("dc_tcl_source_tcl_main", replacements);
            if(tmpCode == null) {
                return false;
            }
            code.addAll(tmpCode);

            if(!generateComponentTclFile(compTclFile, mod.getUnoptimisedFile(), mod.getModuleName(), mod.getOptimisedFile())) {
                return false;
            }
            addGeneratedFiles(compTclFile);
        }

        replacements.clear();
        List<String> tmpCode = replaceInTemplate("dc_tcl_exit_default", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);
        addGeneratedFiles(tclFile);
        if(!FileHelper.getInstance().writeFile(tclFile, code)) {
            return false;
        }

        return true;
    }

    private boolean generateComponentTclFile(File tclFile, File vInFile, String rootModule, File vOutFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("dc_tcl_search_path", syncToolConfig.getSearchPaths());
        replacements.put("dc_tcl_libraries", syncToolConfig.getLibraries());
        replacements.put("dc_tcl_vin", vInFile.getName());
        replacements.put("dc_tcl_module", rootModule);
        replacements.put("dc_tcl_vout", vOutFile.getName());

        List<String> code = new ArrayList<>();

        // setup
        List<String> tmpCode = replaceInTemplate("dc_tcl_setup", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // analyze
        setErrorMsg(replacements, "Analyze", ErrorType.ret);
        tmpCode = replaceInTemplate("dc_tcl_analyze", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // elaborate
        setErrorMsg(replacements, "Elaborate", ErrorType.ret);
        tmpCode = replaceInTemplate("dc_tcl_elab", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // translate
        setErrorMsg(replacements, "Translate", ErrorType.ret);
        tmpCode = replaceInTemplate("dc_tcl_translate", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // compile
        setErrorMsg(replacements, "Compile", ErrorType.ret);
        tmpCode = replaceInTemplate("dc_tcl_compile", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // write verilog
        tmpCode = replaceInTemplate("dc_tcl_write_verilog", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        // translate
        tmpCode = replaceInTemplate("dc_tcl_return_default", replacements);
        if(tmpCode == null) {
            return false;
        }
        code.addAll(tmpCode);

        if(!FileHelper.getInstance().writeFile(tclFile, code)) {
            return false;
        }

        return true;
    }
}
