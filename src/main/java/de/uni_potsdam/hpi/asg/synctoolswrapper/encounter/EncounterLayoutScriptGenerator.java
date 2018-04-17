package de.uni_potsdam.hpi.asg.synctoolswrapper.encounter;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;

public class EncounterLayoutScriptGenerator extends AbstractScriptGenerator {
    private static final Pattern encAreaPattern = Pattern.compile("\\s*Core Area\\(um\\^2\\)\\s*:\\s*([0-9.]+)");

    private Technology           tech;
    private String               exec;
    private String               shFileName;
    private String               tclFileName;
    private File                 vInFile;
    private File                 sdcInFile;
    private File                 vOutFile;
    private File                 sdfOutFile;
    private String               logFileName;
    private String               areaLogFileName;
    private File                 areaLogFile;

    public EncounterLayoutScriptGenerator(Technology tech, String exec, String shFileName, String tclFileName, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String logFileName, String areaLogFileName) {
        this.tech = tech;
        this.exec = exec;
        this.shFileName = shFileName;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.sdcInFile = sdcInFile;
        this.vOutFile = vOutFile;
        this.sdfOutFile = sdfOutFile;
        this.logFileName = logFileName;
        this.areaLogFileName = areaLogFileName;
    }

    @Override
    public boolean generate(File targetDir) {
        File shFile = new File(targetDir, shFileName);
        File tclFile = new File(targetDir, tclFileName);
        File logFile = new File(targetDir, logFileName);
        areaLogFile = new File(targetDir, areaLogFileName);

        if(!generateShFile(shFile, tclFile, logFile)) {
            return false;
        }

        if(!generateTclFile(tclFile)) {
            return false;
        }

        return true;
    }

    private boolean generateShFile(File shFile, File tclFile, File logFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("enc_tcl_file", tclFile.getName());
        replacements.put("enc_log_file", logFile.getName());
        replacements.put("enc_exec", exec);
        addGeneratedFiles(shFile);
        return replaceInTemplateAndWriteOut("enc_sh", replacements, shFile);
    }

    private boolean generateTclFile(File tclFile) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("enc_tcl_sdcin", sdcInFile.getName());
        replacements.put("enc_tcl_vin", vInFile.getName());
        replacements.put("enc_tcl_sdfout", sdfOutFile.getName());
        replacements.put("enc_tcl_vout", vOutFile.getName());
        replacements.put("enc_tcl_arealog", areaLogFile.getName());
        replacements.put("enc_tcl_lib_layout_tcl", tech.getSynctool().getLayouttcl());
        addGeneratedFiles(tclFile);
        return replaceInTemplateAndWriteOut("enc_tcl_layout", replacements, tclFile);
    }

    public Float getAreaValue() {
        List<String> lines = FileHelper.getInstance().readFile(areaLogFile);
        if(lines == null) {
            return -2f;
        }
        for(String line : lines) {
            Matcher m = encAreaPattern.matcher(line);
            if(m.matches()) {
                return Float.parseFloat(m.group(1));
            }
        }
        return -1f;
    }
}
