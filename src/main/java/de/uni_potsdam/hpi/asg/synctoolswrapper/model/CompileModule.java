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

public class CompileModule {

    private String  moduleName;
    private boolean optmisationSuccessful;
    private File    unoptimisedFile;
    private File    optimisedFile;

    public CompileModule(String moduleName, File unoptimisedFile, File optimisedFile) {
        this.moduleName = moduleName;
        this.unoptimisedFile = unoptimisedFile;
        this.optimisedFile = optimisedFile;
        this.optmisationSuccessful = false;
    }

    public String getModuleName() {
        return moduleName;
    }

    public File getUnoptimisedFile() {
        return unoptimisedFile;
    }

    public File getOptimisedFile() {
        return optimisedFile;
    }

    public void setOptmisationSuccessful(boolean optmisationSuccessful) {
        this.optmisationSuccessful = optmisationSuccessful;
    }

    public boolean isOptmisationSuccessful() {
        return optmisationSuccessful;
    }
}
