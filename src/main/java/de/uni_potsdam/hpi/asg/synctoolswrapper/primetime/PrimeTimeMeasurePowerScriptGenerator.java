package de.uni_potsdam.hpi.asg.synctoolswrapper.primetime;

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
import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;
import de.uni_potsdam.hpi.asg.common.technology.Technology;

public class PrimeTimeMeasurePowerScriptGenerator extends AbstractScriptGenerator {

    private Technology tech;
    private String     tclFileName;
    private File       vInFile;
    private File       vcdInFile;
    private String     vcdScope;
    private File       outFile;
    private String     rootModule;
    private String     timesStr;
    private String     powerLogFileName;

    private File       powerLogFile;

    public PrimeTimeMeasurePowerScriptGenerator(Technology tech, String tclFileName, File vInFile, File vcdInFile, String vcdScope, File outFile, String rootModule, String timesStr, String powerLogFileName) {
        this.tech = tech;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.vcdInFile = vcdInFile;
        this.vcdScope = vcdScope;
        this.outFile = outFile;
        this.rootModule = rootModule;
        this.timesStr = timesStr;
        this.powerLogFileName = powerLogFileName;
    }

    @Override
    public boolean generate(File targetDir) {
        File tclFile = new File(targetDir, tclFileName);
        powerLogFile = new File(targetDir, powerLogFileName);

        if(!generateTclFiles(targetDir, tclFile)) {
            return false;
        }

        return true;
    }

    private boolean generateTclFiles(File targetDir, File tclFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("pt_tcl_search_path", tech.getSynctool().getSearchPaths());
        replacements.put("pt_tcl_libraries", tech.getSynctool().getLibraries());
        replacements.put("pt_tcl_vfile", vInFile.getName());
        replacements.put("pt_tcl_module", rootModule);
        replacements.put("pt_tcl_vcdfile", vcdInFile.getName());
        replacements.put("pt_tcl_scope", vcdScope);
        replacements.put("pt_tcl_times", timesStr);
        replacements.put("pt_tcl_outfile", outFile.getName());
        replacements.put("pt_tcl_powerlog", powerLogFile.getName());
        addGeneratedFiles(tclFile);
        return replaceInTemplateAndWriteOut("pt_tcl", replacements, tclFile);
    }

    public String getErrorMsg(int code) {
        switch(code) {
            case 0:
                return "Ok";
            case 1:
                return "Read verilog failed";
            case 2:
                return "Current design failed";
            case 3:
                return "Link failed";
            case 4:
                return "Read vcd failed";
            case 5:
            case 6:
                return "5, 6";
            case 7:
                return "Report power failed";
            case 255: //-1
                return "Runtime error";
            default:
                return "Unknwon error code: " + code;
        }
    }

    public File getPowerLogFile() {
        return powerLogFile;
    }
}
