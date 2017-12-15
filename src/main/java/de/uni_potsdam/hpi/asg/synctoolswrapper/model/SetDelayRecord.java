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

public class SetDelayRecord {

    private String fromSignals;
    private String toSignals;

    private Float  minValue;
    private Float  maxValue;

    public SetDelayRecord(String fromSignals, String toSignals, Float minValue, Float maxValue) {
        this.fromSignals = fromSignals;
        this.toSignals = toSignals;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getFromSignals() {
        return fromSignals;
    }

    public String getToSignals() {
        return toSignals;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }
}
