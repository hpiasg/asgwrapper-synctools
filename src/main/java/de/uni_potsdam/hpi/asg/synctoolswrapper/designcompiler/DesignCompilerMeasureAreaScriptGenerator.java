package de.uni_potsdam.hpi.asg.synctoolswrapper.designcompiler;

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

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;

public class DesignCompilerMeasureAreaScriptGenerator extends DesignCompilerAbstractAreaScriptGenerator {

    private Technology tech;
    private String     tclFileName;
    private File       vInFile;
    private File       sdfInFile;
    private String     rootModule;

    private String     areaLogFileName;

    public DesignCompilerMeasureAreaScriptGenerator(Technology tech, String tclFileName, File vInFile, File sdfInFile, String rootModule, String areaLogFileName) {
        this.tech = tech;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.sdfInFile = sdfInFile;
        this.rootModule = rootModule;
        this.areaLogFileName = areaLogFileName;
    }

    @Override
    public boolean generate(File targetDir) {
        File tclFile = new File(targetDir, tclFileName);
        areaLogFile = new File(targetDir, areaLogFileName);

        if(!generateTclFiles(targetDir, tclFile)) {
            return false;
        }

        return true;
    }

    private boolean generateTclFiles(File targetDir, File tclFile) {
        List<String> code = new ArrayList<>();
        List<String> tmpcode;
        Map<String, String> replacements = new HashMap<>();

        // setup
        replacements.put("dc_tcl_search_path", tech.getSynctool().getSearchPaths());
        replacements.put("dc_tcl_libraries", tech.getSynctool().getLibraries());
        tmpcode = replaceInTemplate("dc_tcl_setup", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // analyze
        replacements.put("dc_tcl_vin", vInFile.getName());
        setErrorMsg(replacements, "Anaylze failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_analyze", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // elaborate
        replacements.put("dc_tcl_module", rootModule);
        setErrorMsg(replacements, "Elaborate failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_elab", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // read sdf
        replacements.put("dc_tcl_sdffile", sdfInFile.getName());
        setErrorMsg(replacements, "Read sdf failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_read_sdf", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // report area
        replacements.put("dc_tcl_arealog", areaLogFile.getName());
        setErrorMsg(replacements, "Report area failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_report_area", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // final
        tmpcode = replaceInTemplate("dc_tcl_exit_default", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        if(!FileHelper.getInstance().writeFile(tclFile, code)) {
            return false;
        }

        return true;
    }
}
