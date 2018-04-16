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

public class DesignCompilerPostSynthesisOperationsScriptGenerator extends DesignCompilerAbstractAreaScriptGenerator {

    private Technology tech;
    private String     tclFileName;
    private File       vInFile;
    private File       sdcInFile;
    private String     rootModule;
    private File       vOutFile;
    private File       sdfOutFile;

    private String     areaLogFileName;

    public DesignCompilerPostSynthesisOperationsScriptGenerator(Technology tech, String tclFileName, File vInFile, File sdcInFile, String rootModule, File vOutFile, File sdfOutFile, String areaLogFileName) {
        this.tech = tech;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.sdcInFile = sdcInFile;
        this.rootModule = rootModule;
        this.vOutFile = vOutFile;
        this.sdfOutFile = sdfOutFile;
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
        Map<String, String> replacements = new HashMap<>();

        if(!generateSetup(code, replacements, tech)) {
            return false;
        }

        if(!generateAnalyze(code, replacements, vInFile)) {
            return false;
        }

        if(!generateElaborate(code, replacements, rootModule)) {
            return false;
        }

        if(!generateReadSdc(code, replacements, sdcInFile)) {
            return false;
        }

        if(!generateReportArea(code, replacements, areaLogFile)) {
            return false;
        }

        if(!generateWriteVerilog(code, replacements, vOutFile, rootModule)) {
            return false;
        }

        if(!generateWriteSdf(code, replacements, sdfOutFile)) {
            return false;
        }

        if(!generateFinal(code, replacements)) {
            return false;
        }

        if(!FileHelper.getInstance().writeFile(tclFile, code)) {
            return false;
        }

        return true;
    }
}
