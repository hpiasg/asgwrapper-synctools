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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.MeasureModule;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.MeasureRecord;

public class DesignCompilerMeasureScriptGenerator extends DesignCompilerAbstractScriptGenerator {
    private static final Logger      logger      = LogManager.getLogger();

    private static final String      asgIdStr    = "ASGtools";
    private static final Pattern     arrivalTime = Pattern.compile("\\s+data arrival time\\s+([0-9.]+)");
    private static final Pattern     pathSpec    = Pattern.compile(";" + asgIdStr + ";(.*);");

    private Technology               tech;
    private Set<MeasureModule>       modules;
    private String                   tclFileName;
    private File                     vInFile;

    private Map<MeasureModule, File> measureOutputFiles;

    public DesignCompilerMeasureScriptGenerator(Technology tech, Set<MeasureModule> modules, String tclFileName, File vInFile) {
        this.tech = tech;
        this.modules = modules;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.measureOutputFiles = new HashMap<>();
    }

    @Override
    public boolean generate(File targetDir) {
        for(MeasureModule mod : modules) {
            if(!mod.getMeasureRecords().isEmpty()) {
                File f = new File(targetDir, mod.getModuleName() + "_measure.log");
                measureOutputFiles.put(mod, f);
                addDownloadIncludeFileNames(f.getName());
            }
        }

        File tclFile = new File(targetDir, tclFileName);
        if(!generateDcTclFile(tclFile)) {
            return false;
        }
        addGeneratedFiles(tclFile);

        return true;
    }

    public boolean parseValues() {
        for(Entry<MeasureModule, File> entry : measureOutputFiles.entrySet()) {
            MeasureModule mod = entry.getKey();
            File file = entry.getValue();
            List<String> lines = FileHelper.getInstance().readFile(file);
            if(lines == null) {
                return false;
            }
            Matcher m = null;
            String currSpec = null;
            for(String line : lines) {
                m = pathSpec.matcher(line);
                if(m.matches()) {
                    currSpec = m.group(1);
                    continue;
                }
                m = arrivalTime.matcher(line);
                if(m.matches()) {
                    if(currSpec == null) {
                        logger.error("No spec?");
                        return false;
                    }
                    mod.addValue(currSpec, Float.parseFloat(m.group(1)));
                    currSpec = null;
                }
            }
        }
        return true;
    }

    private boolean generateDcTclFile(File dcTclFile) {
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

        for(MeasureModule mod : modules) {
            if(!mod.getMeasureRecords().isEmpty()) {
                File file = measureOutputFiles.get(mod);
                replacements.put("dc_tcl_sub_log", file.getName());

                // elab
                replacements.put("dc_tcl_sub_module", mod.getModuleName());
                setErrorMsg(replacements, "Elab " + mod.getModuleName() + " failed", ErrorType.exit);
                tmpcode = replaceInTemplate("dc_tcl_elab_sub", replacements);
                if(tmpcode == null) {
                    return false;
                }
                code.addAll(tmpcode);

                // read sdf
                if(mod.getSdfFile() != null) {
                    replacements.put("dc_tcl_sub_sdffile", mod.getSdfFile().getName());
                    setErrorMsg(replacements, "Read Sdf " + mod.getModuleName() + " failed", ErrorType.exit);
                    tmpcode = replaceInTemplate("dc_tcl_read_sdf_sub", replacements);
                    if(tmpcode == null) {
                        return false;
                    }
                    code.addAll(tmpcode);
                }

                for(MeasureRecord rec : mod.getMeasureRecords()) {
                    // measure
                    String template = getMeasureTemplateName(rec);
                    String from = rec.getFromSignals();
                    String to = rec.getToSignals();
                    replacements.put("dc_tcl_sub_from", from);
                    replacements.put("dc_tcl_sub_to", to);
                    replacements.put("dc_tcl_asgidstr", asgIdStr);
                    replacements.put("dc_tcl_sub_id", rec.getId());
                    setErrorMsg(replacements, "Report timing of module " + mod.getModuleName() + ": " + template + ", from: {" + from + "}, to: {" + to + "} failed", ErrorType.exit);
                    tmpcode = replaceInTemplates(new String[]{"dc_tcl_echo_sub", template}, replacements);
                    if(tmpcode == null) {
                        return false;
                    }
                    code.addAll(tmpcode);
                }
            }
        }

        // final
        tmpcode = replaceInTemplate("dc_tcl_exit_default", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        if(!FileHelper.getInstance().writeFile(dcTclFile, code)) {
            return false;
        }

        return true;
    }

    private String getMeasureTemplateName(MeasureRecord rec) {
        StringBuilder templateName = new StringBuilder();
        templateName.append("dc_tcl_measure_");
        switch(rec.getType()) {
            case max:
                templateName.append("max_");
                break;
            case min:
                templateName.append("min_");
                break;
        }
        switch(rec.getFromEdge()) {
            case both:
                templateName.append("both_");
                break;
            case falling:
                templateName.append("fall_");
                break;
            case rising:
                templateName.append("rise_");
                break;
        }
        switch(rec.getToEdge()) {
            case both:
                templateName.append("both");
                break;
            case falling:
                templateName.append("fall");
                break;
            case rising:
                templateName.append("rise");
                break;
        }

        return templateName.toString();
    }
}
