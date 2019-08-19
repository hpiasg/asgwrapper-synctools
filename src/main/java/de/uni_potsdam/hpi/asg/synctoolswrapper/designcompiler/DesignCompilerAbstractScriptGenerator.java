package de.uni_potsdam.hpi.asg.synctoolswrapper.designcompiler;

/*
 * Copyright (C) 2017 - 2018  Norman Kluge
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;
import de.uni_potsdam.hpi.asg.common.technology.Technology;

public abstract class DesignCompilerAbstractScriptGenerator extends AbstractScriptGenerator {
    private static final Logger logger = LogManager.getLogger();

    public enum ErrorType {
        ret, exit
    }

    private int                    nextErrorId;
    protected Map<Integer, String> errorMsgMap;

    public DesignCompilerAbstractScriptGenerator() {
        this.nextErrorId = 1;
        this.errorMsgMap = new HashMap<>();
    }

    protected void setErrorMsg(Map<String, String> replacements, String msg, ErrorType type) {
        String errId = Integer.toString(nextErrorId);
        String errCode = null;
        switch(type) {
            case exit:
                errCode = generateErrorCode("dc_tcl_exit", "dc_tcl_exitcode", errId);
                break;
            case ret:
                errCode = generateErrorCode("dc_tcl_return", "dc_tcl_retcode", errId);
                break;
        }
        if(errCode == null) {
            return;
        }
        replacements.put("dc_tcl_errorcode", errCode);
        errorMsgMap.put(nextErrorId++, msg);
    }

    public String getErrorMsg(int retCode) {
        if(errorMsgMap.containsKey(retCode)) {
            return errorMsgMap.get(retCode);
        } else {
            return "Unkown error code: " + retCode;
        }
    }

    private String generateErrorCode(String templateName, String templateReplaceStr, String errId) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put(templateReplaceStr, errId);
        List<String> code = replaceInTemplate(templateName, replacements);
        if(code == null) {
            return null;
        }
        if(code.isEmpty() || code.size() > 1) {
            logger.error(templateName + " should conatin exactly one line");
            return null;
        }
        return code.get(0);
    }

    protected boolean generateSetup(List<String> code, Map<String, String> replacements, Technology tech) {
        replacements.put("dc_tcl_search_path", tech.getSynctool().getSearchPaths());
        replacements.put("dc_tcl_libraries", tech.getSynctool().getLibraries());
        List<String> tmpcode = replaceInTemplate("dc_tcl_setup", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateAnalyze(List<String> code, Map<String, String> replacements, File vFile) {
        replacements.put("dc_tcl_vin", vFile.getName());
        setErrorMsg(replacements, "Anaylze failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_analyze", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateElaborate(List<String> code, Map<String, String> replacements, String rootModule) {
        replacements.put("dc_tcl_module", rootModule);
        setErrorMsg(replacements, "Elaborate failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_elab", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateReadSdf(List<String> code, Map<String, String> replacements, File sdfFile) {
        replacements.put("dc_tcl_sdffile", sdfFile.getName());
        setErrorMsg(replacements, "Read SDF failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_read_sdf", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateReadSdc(List<String> code, Map<String, String> replacements, File sdcFile) {
        replacements.put("dc_tcl_sdcfile", sdcFile.getName());
        setErrorMsg(replacements, "Read SDC failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_read_sdc", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateReportArea(List<String> code, Map<String, String> replacements, File areaLogFile) {
        replacements.put("dc_tcl_arealog", areaLogFile.getName());
        setErrorMsg(replacements, "Report area failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_report_area", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateFinal(List<String> code, Map<String, String> replacements) {
        List<String> tmpcode = replaceInTemplate("dc_tcl_exit_default", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateWriteVerilog(List<String> code, Map<String, String> replacements, File vFile, String rootModule) {
        replacements.put("dc_tcl_vout", vFile.getName());
        setErrorMsg(replacements, "Write verilog for module " + rootModule + " failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_write_verilog", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateWriteSdf(List<String> code, Map<String, String> replacements, File sdfFile) {
        replacements.put("dc_tcl_sdfout", sdfFile.getName());
        setErrorMsg(replacements, "Generate Sdf failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_write_sdf", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateTranslate(List<String> code, Map<String, String> replacements) {
        setErrorMsg(replacements, "Translate failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_translate", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

    protected boolean generateUniquify(List<String> code, Map<String, String> replacements) {
        setErrorMsg(replacements, "Uniquify failed", ErrorType.exit);
        List<String> tmpcode = replaceInTemplate("dc_tcl_uniquify", replacements);
        if(tmpcode == null) {
            return false;
        }
        code.addAll(tmpcode);
        return true;
    }

}
