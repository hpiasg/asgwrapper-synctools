package de.uni_potsdam.hpi.asg.synctoolswrapper;

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

import de.uni_potsdam.hpi.asg.common.invoker.AbstractScriptGenerator;

public class TemplatesInitialiser {

    public static boolean init() {
        if(!AbstractScriptGenerator.readTemplateFiles(DesignCompilerInvoker.getTemplateFileName())) {
            return false;
        }
        for(String str : EncounterInvoker.getTemplateFileName()) {
            if(!AbstractScriptGenerator.readTemplateFiles(str)) {
                return false;
            }
        }
        if(!AbstractScriptGenerator.readTemplateFiles(PrimeTimeInvoker.getTemplateFileName())) {
            return false;
        }
        for(String str : IncisiveInvoker.getTemplateFileName()) {
            if(!AbstractScriptGenerator.readTemplateFiles(str)) {
                return false;
            }
        }
        return true;
    }
}
