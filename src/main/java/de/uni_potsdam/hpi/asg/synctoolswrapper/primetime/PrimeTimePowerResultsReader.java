package de.uni_potsdam.hpi.asg.synctoolswrapper.primetime;

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

public class PrimeTimePowerResultsReader {

    private static final Pattern avgPowerPattern  = Pattern.compile("\\s*Total Power\\s*=\\s*([0-9e.\\-]+)\\s*\\(100\\.00%\\)");
    private static final Pattern peakPowerPattern = Pattern.compile("\\s*Peak Power\\s*=\\s*([0-9e.\\-]+)");

    private float                avgPower;
    private float                peakPower;
    private float                energy;

    public PrimeTimePowerResultsReader() {
        this.avgPower = -1f;
        this.peakPower = -1f;
        this.energy = -1f;
    }

    public boolean parseFiles(File powerLogFile, String timesStr, String rootModule) {
        if(!parsePowerFile(rootModule, powerLogFile)) {
            return false;
        }
        if(!computeEnergy(timesStr)) {
            return false;
        }
        return true;
    }

    private boolean parsePowerFile(String rootModule, File file) {
        List<String> lines = FileHelper.getInstance().readFile(file);
        if(lines == null) {
            return false;
        }
        Matcher m = null;
        for(String line : lines) {
            m = avgPowerPattern.matcher(line);
            if(m.matches()) {
                avgPower = Float.parseFloat(m.group(1));
            }
            m = peakPowerPattern.matcher(line);
            if(m.matches()) {
                peakPower = Float.parseFloat(m.group(1));
            }
        }
        return true;
    }

    private boolean computeEnergy(String timesStr) {
        String[] split = timesStr.split(" ");
        float val = 0f;
        for(int i = 0; i < split.length; i++) {
            float begin = Float.parseFloat(split[i]);
            i++;
            float end = Float.parseFloat(split[i]);
            val += (end - begin);
        }
        energy = avgPower * val;
        return true;
    }

    public float getAvgPower() {
        return avgPower;
    }

    public float getPeakPower() {
        return peakPower;
    }

    public float getEnergy() {
        return energy;
    }
}
