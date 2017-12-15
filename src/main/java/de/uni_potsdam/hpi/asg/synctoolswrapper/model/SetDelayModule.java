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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetDelayModule {
    private String              moduleName;
    private Set<SetDelayRecord> setDelayRecords;
    private String              dontTouchEntries;

    public SetDelayModule(String moduleName) {
        this.moduleName = moduleName;
        this.setDelayRecords = new HashSet<>();
    }

    public boolean addMatchRecord(SetDelayRecord rec) {
        return setDelayRecords.add(rec);
    }

    public void setDontTouchEntries(String dontTouchEntries) {
        this.dontTouchEntries = dontTouchEntries;
    }

    public Set<SetDelayRecord> getSetDelayRecords() {
        return Collections.unmodifiableSet(setDelayRecords);
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDontTouchEntries() {
        return dontTouchEntries;
    }
}
