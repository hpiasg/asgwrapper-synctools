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

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.SplitSdfModule;

public class DesignCompilerSdfSplitScriptGenerator extends DesignCompilerAbstractErrorScriptGenerator {

    private Technology          tech;
    private String              tclFileName;

    private File                vInFile;
    private File                sdcInFile;
    private boolean             generateSdf;
    private File                sdfInFile;
    private Set<SplitSdfModule> modules;
    private String              rootModule;

    public DesignCompilerSdfSplitScriptGenerator(Technology tech, String tclFileName, File vInFile, File sdcInFile, boolean generateSdf, File sdfInFile, Set<SplitSdfModule> modules, String rootModule) {
        this.tech = tech;
        this.tclFileName = tclFileName;
        this.vInFile = vInFile;
        this.sdcInFile = sdcInFile;
        this.generateSdf = generateSdf;
        this.sdfInFile = sdfInFile;
        this.modules = modules;
        this.rootModule = rootModule;
    }

    @Override
    public boolean generate(File targetDir) {
        File tclFile = new File(targetDir, tclFileName);
        if(!generateDcTclFile(tclFile)) {
            return false;
        }
        addGeneratedFiles(tclFile);

        return true;
    }

    private boolean generateDcTclFile(File dcTclFile) {
        List<String> code = new ArrayList<>();
        List<String> tmpcode;
        Map<String, String> replacements = new HashMap<>();

        // setup
        replacements.put("dc_tcl_search_path", tech.getSynctool().getSearchPaths());
        replacements.put("dc_tcl_libraries", tech.getSynctool().getLibraries());
        tmpcode = replaceInTemplate("dm_dc_tcl_setup", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // analyze
        replacements.put("dc_tcl_vin", vInFile.getName());
        setErrorMsg(replacements, "Anaylze failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dm_dc_tcl_analyze", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // elaborate
        replacements.put("dc_tcl_module", rootModule);
        setErrorMsg(replacements, "Elaborate failed", ErrorType.exit);
        tmpcode = replaceInTemplate("dm_dc_tcl_elab", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);

        // generate sdf (if needed)
        if(generateSdf) {
            // read sdc
            if(sdcInFile != null) {
                replacements.put("dc_tcl_sdcfile", sdcInFile.getName());
                setErrorMsg(replacements, "Read Sdc failed", ErrorType.exit);
                tmpcode = replaceInTemplate("dm_dc_tcl_read_sdc", replacements);
                if(tmpcode == null) {
                    return false;
                }
                code.addAll(tmpcode);
            }

            // write sdf
            replacements.put("dc_tcl_sdfout", sdfInFile.getName());
            setErrorMsg(replacements, "Generate Sdf failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dm_dc_tcl_write_sdf", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);
        }

        // split sdf
        for(SplitSdfModule mod : modules) {
            replacements.put("dc_tcl_sdfinstname", mod.getInstName());
            replacements.put("dc_tcl_sdfout", mod.getSdfFile().getName());
            setErrorMsg(replacements, "Write Sdf " + mod.getInstName() + " failed", ErrorType.exit);
            tmpcode = replaceInTemplate("dm_dc_tcl_write_sdf_split", replacements);
            if(tmpcode == null) {
                return false;
            }
            code.addAll(tmpcode);
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
}
