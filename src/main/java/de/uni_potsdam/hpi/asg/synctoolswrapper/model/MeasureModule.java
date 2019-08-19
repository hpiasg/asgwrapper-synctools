package de.uni_potsdam.hpi.asg.synctoolswrapper.model;

/*
 * Copyright (C) 2016 - 2017 Norman Kluge
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
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.uni_potsdam.hpi.asg.synctoolswrapper.model.MeasureRecord.MeasureEdge;
import de.uni_potsdam.hpi.asg.synctoolswrapper.model.MeasureRecord.MeasureType;

public class MeasureModule {
    private static final Logger            logger = LogManager.getLogger();

    private String                         moduleName;
    private File                           sdfFile;

    protected BiMap<String, MeasureRecord> measureRecords;
//    private Map<String, MeasureRecord> measureRecords;

    public MeasureModule(String moduleName) {
        this.moduleName = moduleName;
        this.measureRecords = HashBiMap.create();
//        this.measureRecords = new HashMap<>();
    }

    public boolean addValue(String id, Float value) {
        if(measureRecords.containsKey(id)) {
            measureRecords.get(id).setValue(value);
            return true;
        }
        logger.error("Id not defined: " + id);
        return false;
    }

    public MeasureRecord getMeasureRecord(MeasureEdge fromEdge, String fromSignals, MeasureEdge toEdge, String toSignals, MeasureType type) {
        String id = MeasureRecord.getID(fromEdge, fromSignals, toEdge, toSignals, type);
        if(!measureRecords.containsKey(id)) {
            MeasureRecord rec = new MeasureRecord(fromEdge, fromSignals, toEdge, toSignals, type);
            measureRecords.put(id, rec);
        }
        return measureRecords.get(id);
    }

    public Collection<MeasureRecord> getMeasureRecords() {
        return Collections.unmodifiableCollection(measureRecords.values());
    }

    public String getModuleName() {
        return moduleName;
    }

    public File getSdfFile() {
        return sdfFile;
    }

    public void setSdfFile(File sdfFile) {
        this.sdfFile = sdfFile;
    }
}
