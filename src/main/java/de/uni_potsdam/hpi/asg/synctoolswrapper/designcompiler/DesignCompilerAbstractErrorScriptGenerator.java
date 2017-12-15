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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;

public abstract class DesignCompilerAbstractErrorScriptGenerator extends AbstractScriptGenerator {
    private static final Logger logger = LogManager.getLogger();

    public enum ErrorType {
        ret, exit
    }

    private int                    nextErrorId;
    protected Map<Integer, String> errorMsgMap;

    public DesignCompilerAbstractErrorScriptGenerator() {
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
}
