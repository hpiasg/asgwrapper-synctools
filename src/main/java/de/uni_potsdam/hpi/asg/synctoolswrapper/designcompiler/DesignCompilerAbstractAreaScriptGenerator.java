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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public abstract class DesignCompilerAbstractAreaScriptGenerator extends DesignCompilerAbstractScriptGenerator {

    private static final Pattern dcAreaPattern = Pattern.compile("Total cell area:\\s*([0-9.]+)");

    protected File               areaLogFile;

    public Float readResult() {
        List<String> lines = FileHelper.getInstance().readFile(areaLogFile);
        if(lines == null) {
            return null;
        }
        for(String line : lines) {
            Matcher m = dcAreaPattern.matcher(line);
            if(m.matches()) {
                return Float.parseFloat(m.group(1));
            }
        }
        return null;
    }
}
