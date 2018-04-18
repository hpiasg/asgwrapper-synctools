#
# Copyright (C) 2018 Norman Kluge
# 
# This file is part of ASGwrapper-synctools.
# 
# ASGwrapper-synctools is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# ASGwrapper-synctools is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with ASGwrapper-synctools.  If not, see <http://www.gnu.org/licenses/>.
#

#+pt_tcl_begin+#
set si_enable_analysis TRUE
set power_enable_analysis TRUE
set power_analysis_mode time_based

lappend search_path #*pt_tcl_search_path*#
set link_library { #*pt_tcl_libraries*# }
set target_library { #*pt_tcl_libraries*# }

set rvs [read_verilog #*pt_tcl_vfile*#]
if {$rvs == 0} {
    exit 1
}

set rvs [current_design "#*pt_tcl_module*#"]
if {$rvs == 0} {
    exit 2
}

set rvs [link]
if {$rvs == 0} {
    exit 3
}

set rvs [read_vcd #*pt_tcl_vcdfile*# -strip_path #*pt_tcl_scope*# -time {#*pt_tcl_times*#}]
if {$rvs == 0} {
    exit 4
}

set_power_analysis_options -waveform_format out -waveform_output ./#*pt_tcl_outfile*#
set rvs [check_power]
#if {$rvs == 0} {
#   exit 5
#}

set rvs [update_power]
#if {$rvs == 0} {
#   exit 6
#}

redirect #*pt_tcl_powerlog*# {
    set rvs [report_power] 
    if {$rvs == 0} {
        exit 7
    }
}

exit 0
#+pt_tcl_end+#
