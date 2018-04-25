package de.uni_potsdam.hpi.asg.synctoolswrapper.incisive;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.IncisiveInvoker.ExecType;

public class IncisiveSimulationScriptGenerator extends AbstractScriptGenerator {
    private int                   nextErrorId;
    private Map<Integer, String>  errorMsgMap;

    private Technology            tech;
    private Map<ExecType, String> execs;
    private String                shGuiFileName;
    private String                shTerminalFileName;
    private String                tclFileName;
    private String                sdfCmdFileName;
    private String                logFileName;

    private File                  vInFile;
    private File                  tbInFile;
    private File                  sdfInFile;
    private File                  vcdOutFile;
    private File                  simLogFile;

    private String                tbRoot;
    private String                sdfScope;

    public IncisiveSimulationScriptGenerator() {
        this.nextErrorId = 1;
        this.errorMsgMap = new HashMap<>();
    }

    @Override
    public boolean generate(File targetDir) {
        File shGuiFile = new File(targetDir, shGuiFileName);
        File shTerminalFile = new File(targetDir, shTerminalFileName);
        File tclFile = new File(targetDir, tclFileName);
        File sdfCmdFile = new File(targetDir, sdfCmdFileName);
        File logFile = new File(targetDir, logFileName);

        if(!generateShFile(shGuiFile, shTerminalFile, tclFile, sdfCmdFile, logFile)) {
            return false;
        }

        if(!generateTclFile(tclFile)) {
            return false;
        }

        if(!generateSdfCmdFile(sdfCmdFile)) {
            return false;
        }

        return true;
    }

    private boolean generateShFile(File simShGuiFile, File simShTerminalFile, File simTclFile, File simSdfCmdFile, File logFile) {
        List<String> code = new ArrayList<>();
        List<String> tmpcode;
        Map<String, String> replacements = new HashMap<>();

        // execs
        replacements.put("sim_sh_vcompiler", execs.get(ExecType.vCompiler));
        replacements.put("sim_sh_sdfcompiler", execs.get(ExecType.sdfCompiler));
        replacements.put("sim_sh_elaborator", execs.get(ExecType.elaborator));
        replacements.put("sim_sh_simulator", execs.get(ExecType.simulator));

        // setup
        replacements.put("sim_sh_sdffile", sdfInFile.getName());
        setErrorMsg(replacements, "Setup fail");
        tmpcode = replaceInTemplate("sim_sh_setup", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // compile verilog files
        replacements.put("sim_sh_log", logFile.getName());
        List<String> compileFiles = new ArrayList<>();
        if(tech.getSynctool().getVerilogIncludes() != null) {
            for(String str : tech.getSynctool().getVerilogIncludes()) {
                compileFiles.add(str);
            }
        }
        compileFiles.add(vInFile.getName());
        compileFiles.add(tbInFile.getName());
        for(String str : compileFiles) {
            replacements.put("sim_sh_vfile", str);
            setErrorMsg(replacements, "Compile " + str + " fail");
            tmpcode = replaceInTemplate("sim_sh_compile_verilog", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);
        }

        // compile sdf
        setErrorMsg(replacements, "Compile sdf fail");
        tmpcode = replaceInTemplate("sim_sh_compile_sdf", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // elaborate
        replacements.put("sim_sh_sdfcmdfile", simSdfCmdFile.getName());
        replacements.put("sim_sh_tbroot", tbRoot);
        setErrorMsg(replacements, "Elaborate fail");
        tmpcode = replaceInTemplate("sim_sh_elaborate", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // gui
        List<String> guiCode = new ArrayList<>();
        guiCode.addAll(code);
        replacements.put("sim_sh_simlog", simLogFile.getName());
        setErrorMsg(replacements, "Start gui fail");
        tmpcode = replaceInTemplate("sim_sh_start_gui", replacements);
        if(tmpcode == null) {
            return false;
        }
        guiCode.addAll(tmpcode);

        if(!FileHelper.getInstance().writeFile(simShGuiFile, guiCode)) {
            return false;
        }

        // terminal
        List<String> terminalCode = new ArrayList<>();
        terminalCode.addAll(code);
        replacements.put("sim_tcl_file", simTclFile.getName());
        setErrorMsg(replacements, "Start terminal fail");
        tmpcode = replaceInTemplate("sim_sh_start_terminal", replacements);
        if(tmpcode == null) {
            return false;
        }
        terminalCode.addAll(tmpcode);
        if(!FileHelper.getInstance().writeFile(simShTerminalFile, terminalCode)) {
            return false;
        }

        addGeneratedFiles(simShGuiFile, simShTerminalFile);
        return true;
    }

    private boolean generateTclFile(File tclFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("sim_tcl_vcdfile", vcdOutFile.getName());
        addGeneratedFiles(tclFile);
        return replaceInTemplateAndWriteOut("sim_tcl_run", replacements, tclFile);
    }

    private boolean generateSdfCmdFile(File sdfCmdFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("sim_sdfcmd_sdffile", sdfInFile.getName());
        replacements.put("sim_sdfcmd_sdfscope", sdfScope);
        addGeneratedFiles(sdfCmdFile);
        return replaceInTemplateAndWriteOut("sim_sdfcmd", replacements, sdfCmdFile);
    }

    private void setErrorMsg(Map<String, String> replacements, String msg) {
        replacements.put("sim_sh_exitcode", Integer.toString(nextErrorId));
        errorMsgMap.put(nextErrorId++, msg);
    }

    public String getErrorMsg(int code) {
        return errorMsgMap.get(code);
    }

    public void setTech(Technology tech) {
        this.tech = tech;
    }

    public void setExecs(Map<ExecType, String> execs) {
        this.execs = execs;
    }

    public void setShGuiFileName(String shGuiFileName) {
        this.shGuiFileName = shGuiFileName;
    }

    public void setShTerminalFileName(String shTerminalFileName) {
        this.shTerminalFileName = shTerminalFileName;
    }

    public void setTclFileName(String tclFileName) {
        this.tclFileName = tclFileName;
    }

    public void setSdfCmdFileName(String sdfCmdFileName) {
        this.sdfCmdFileName = sdfCmdFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public void setSimLogFile(File simLogFile) {
        this.simLogFile = simLogFile;
    }

    public void setvInFile(File vInFile) {
        this.vInFile = vInFile;
    }

    public void setTbInFile(File tbInFile) {
        this.tbInFile = tbInFile;
    }

    public void setSdfInFile(File sdfInFile) {
        this.sdfInFile = sdfInFile;
    }

    public void setVcdOutFile(File vcdOutFile) {
        this.vcdOutFile = vcdOutFile;
    }

    public void setTbRoot(String tbRoot) {
        this.tbRoot = tbRoot;
    }

    public void setSdfScope(String sdfScope) {
        this.sdfScope = sdfScope;
    }
}
