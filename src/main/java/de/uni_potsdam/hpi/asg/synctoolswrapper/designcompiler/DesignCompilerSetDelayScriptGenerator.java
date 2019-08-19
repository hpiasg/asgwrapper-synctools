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

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.SetDelayModule;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.SetDelayRecord;

public class DesignCompilerSetDelayScriptGenerator extends DesignCompilerAbstractScriptGenerator {
    private Technology                tech;
    private Set<SetDelayModule>       modules;
    private String                    tclFileName;
    private File                      vInFile;
    private File                      sdcInFile;
    private File                      vOutFile;
    private File                      sdfOutFile;
    private Map<SetDelayModule, File> subLogFiles;
    private String                    rootModule;

    public DesignCompilerSetDelayScriptGenerator(Technology tech, Set<SetDelayModule> modules, String tclFileName, File vInFile, File sdcInFile, File vOutFile, File sdfOutFile, String rootModule) {
        this.tech = tech;
        this.modules = modules;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.sdcInFile = sdcInFile;
        this.vOutFile = vOutFile;
        this.sdfOutFile = sdfOutFile;
        this.rootModule = rootModule;
        this.subLogFiles = new HashMap<>();
    }

    @Override
    public boolean generate(File targetDir) {
        for(SetDelayModule mod : modules) {
            if(!mod.getSetDelayRecords().isEmpty()) {
                File f = new File(mod.getModuleName() + "_setdelay.log");
                subLogFiles.put(mod, f);
                addDownloadIncludeFileNames(f.getName());
            }
        }

        File tclFile = new File(targetDir, tclFileName);
        if(!generateDcTclFile(tclFile)) {
            return false;
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

        for(SetDelayModule mod : modules) {
            if(mod.getSetDelayRecords().isEmpty()) {
                continue;
            }
            replacements.put("dc_tcl_sub_log", subLogFiles.get(mod).getName());

            // elab
            replacements.put("dc_tcl_sub_module", mod.getModuleName());
            setErrorMsg(replacements, "Elab " + mod.getModuleName() + " failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dc_tcl_elab_sub", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);

            // set delays
            for(SetDelayRecord rec : mod.getSetDelayRecords()) {
                tmpcode = generateSetDelayCode(replacements, mod.getModuleName(), rec);
                if(tmpcode == null) {
                    return false;
                }
                code.addAll(tmpcode);
            }

            // set dont touch
            replacements.put("dc_tcl_sub_donttouch", mod.getDontTouchEntries());
            setErrorMsg(replacements, "Dont touch of " + mod.getModuleName() + " of: {" + mod.getDontTouchEntries() + "} failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dc_tcl_donttouch_sub", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);

            // compile
            setErrorMsg(replacements, "Compile " + mod.getModuleName() + " failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dc_tcl_compile_sub", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);
        }

        // final
        replacements.put("dc_tcl_module", rootModule);
        setErrorMsg(replacements, "Elaborate " + rootModule + " failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_elab", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        if(sdcInFile != null) {
            replacements.put("dc_tcl_sdcfile", sdcInFile.getName());
            setErrorMsg(replacements, "Read Sdc failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dc_tcl_read_sdc", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);
        }

        replacements.put("dc_tcl_sdfout", sdfOutFile.getName());
        setErrorMsg(replacements, "Write Sdf for module " + rootModule + " failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_write_sdf", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        replacements.put("dc_tcl_vout", vOutFile.getName());
        setErrorMsg(replacements, "Write verilog for module " + rootModule + " failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dc_tcl_write_verilog", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        tmpcode = replaceInTemplate("dc_tcl_exit_default", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        addGeneratedFiles(dcTclFile);
        if(!FileHelper.getInstance().writeFile(dcTclFile, code)) {
            return false;
        }

        return true;
    }

    private List<String> generateSetDelayCode(Map<String, String> replacements, String moduleName, SetDelayRecord rec) {
        String from = rec.getFromSignals();
        String to = rec.getToSignals();
        Float min = rec.getMinValue();
        Float max = rec.getMaxValue();

        List<String> code = new ArrayList<>();
        List<String> tmpCode = null;
        replacements.put("dc_tcl_sub_from", from);
        replacements.put("dc_tcl_sub_to", to);
        replacements.put("dc_tcl_sub_time_min", min.toString());
        replacements.put("dc_tcl_sub_time_max", max.toString());

        setErrorMsg(replacements, "Set min delay of " + moduleName + " from: {" + from + "}, to: {" + to + "}, value: " + min + " failed", ErrorType.exit);
        tmpCode = replaceInTemplate("dc_tcl_setdelay_min_sub", replacements);
        if(tmpCode == null) {
            return null;
        }
        code.addAll(tmpCode);
        setErrorMsg(replacements, "Set max delay of " + moduleName + " from: {" + from + "}, to: {" + to + "}, value: " + min + " failed", ErrorType.exit);
        tmpCode = replaceInTemplate("dc_tcl_setdelay_max_sub", replacements);
        if(tmpCode == null) {
            return null;
        }
        code.addAll(tmpCode);

        return code;
    }
}
