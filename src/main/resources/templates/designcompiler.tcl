#
# Copyright (C) 2017 - 2018 Norman Kluge
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

#-- error handling

#+dc_tcl_return_default_begin+#
return 0
#+dc_tcl_return_default_end+#

#+dc_tcl_return_begin+#
return #*dc_tcl_retcode*#
#+dc_tcl_return_end+#

#+dc_tcl_exit_default_begin+#
exit 0
#+dc_tcl_exit_default_end+#

#+dc_tcl_exit_begin+#
exit #*dc_tcl_exitcode*#
#+dc_tcl_exit_end+#

#-- commands

#+dc_tcl_setup_begin+#
sh rm -f default.svf
sh rm -rf dccompile
sh mkdir dccompile
define_design_lib WORK -path ./dccompile

lappend search_path #*dc_tcl_search_path*#
set link_library { #*dc_tcl_libraries*# }
set target_library { #*dc_tcl_libraries*# }
#+dc_tcl_setup_end+#

#+dc_tcl_analyze_begin+#
set rvs [analyze -library WORK -format verilog {#*dc_tcl_vin*#}]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_analyze_end+#

#+dc_tcl_elab_begin+#
set rvs [elaborate #*dc_tcl_module*# -architecture verilog -library DEFAULT]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_elab_end+#

#+dc_tcl_translate_begin+#
set rvs [translate]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_translate_end+#

#+dc_tcl_uniquify_begin+#
set rvs [uniquify]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_uniquify_end+#

#+dc_tcl_compile_begin+#
set rvs [compile -ungroup_all -no_design_rule -map_effort high]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_compile_end+#

#+dc_tcl_write_verilog_begin+#
write -hierarchy -format verilog -output #*dc_tcl_vout*#
#+dc_tcl_write_verilog_end+#

#+dc_tcl_write_sdf_begin+#
write_sdf -significant_digits 10 #*dc_tcl_sdfout*#
#+dc_tcl_write_sdf_end+#

#+dc_tcl_read_sdf_begin+#
set rvs [read_sdf #*dc_tcl_sdffile*#]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_read_sdf_end+#

#+dc_tcl_write_sdf_split_begin+#
write_sdf -instance #*dc_tcl_sdfinstname*# -significant_digits 10 #*dc_tcl_sdfout*#
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_write_sdf_split_end+#

#+dc_tcl_read_sdc_begin+#
set rvs [read_sdc #*dc_tcl_sdcfile*#]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_read_sdc_end+#

#+dc_tcl_report_area_begin+#
redirect #*dc_tcl_arealog*# {
	set rvs [report_area]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_report_area_end+#

#+dc_tcl_subsequent_begin+#
set_dont_touch {#*dc_tcl_donttouch*#} true
set rvs [compile_ultra]
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_subsequent_end+#

#+dc_tcl_source_tcl_main_begin+#
echo \n\n#*dc_tcl_component*#\n\n
set rvs [source #*dc_tcl_component_tclfile*#]
echo \;#*dc_tcl_asgidstr*#\;#*dc_tcl_component*#\;$rvs\;
#+dc_tcl_source_tcl_main_end+#

#-- commands sublog

#+dc_tcl_elab_sub_begin+#
redirect #*dc_tcl_sub_log*# {
	set rvs [elaborate #*dc_tcl_sub_module*# -architecture verilog -library DEFAULT]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_elab_sub_end+#

#+dc_tcl_read_sdf_sub_begin+#
redirect #*dc_tcl_sub_log*# {
	set rvs [read_sdf #*dc_tcl_sub_sdffile*#]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_read_sdf_sub_end+#

#+dc_tcl_donttouch_sub_begin+#
redirect -append #*dc_tcl_sub_log*# {
	set rvs [set_dont_touch_network [get_ports { #*dc_tcl_sub_donttouch*# }]]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_donttouch_sub_end+#

#+dc_tcl_compile_sub_begin+#
redirect -append #*dc_tcl_sub_log*# {
	set rvs [compile]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_compile_sub_end+#

#+dc_tcl_echo_sub_begin+#
redirect -append #*dc_tcl_sub_log*# {
    echo \;#*dc_tcl_asgidstr*#\;#*dc_tcl_sub_id*#\;
}
#+dc_tcl_echo_sub_end+#

#-- commands setdelay

#+dc_tcl_setdelay_min_sub_begin+#
redirect -append #*dc_tcl_sub_log*# {
	set rvs [set_min_delay -from [get_ports { #*dc_tcl_sub_from*# }] -to [get_ports { #*dc_tcl_sub_to*# }] #*dc_tcl_sub_time_min*#]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_setdelay_min_sub_end+#

#+dc_tcl_setdelay_max_sub_begin+#
redirect -append #*dc_tcl_sub_log*# {
	set rvs [set_max_delay -from [get_ports { #*dc_tcl_sub_from*# }] -to [get_ports { #*dc_tcl_sub_to*# }] #*dc_tcl_sub_time_max*#]
}
if {$rvs == 0} {
	#*dc_tcl_errorcode*#
}
#+dc_tcl_setdelay_max_sub_end+#

#-- commands measure

#+dc_tcl_measure_min_rise_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_rise_rise_end+#

#+dc_tcl_measure_min_rise_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_rise_fall_end+#

#+dc_tcl_measure_min_rise_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_rise_both_end+#

#+dc_tcl_measure_min_fall_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_fall_rise_end+#

#+dc_tcl_measure_min_fall_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_fall_fall_end+#

#+dc_tcl_measure_min_fall_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_fall_both_end+#

#+dc_tcl_measure_min_both_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_both_rise_end+#

#+dc_tcl_measure_min_both_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_both_fall_end+#

#+dc_tcl_measure_min_both_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay min -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_min_both_both_end+#

#+dc_tcl_measure_max_rise_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_rise_rise_end+#

#+dc_tcl_measure_max_rise_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_rise_fall_end+#

#+dc_tcl_measure_max_rise_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -rise_from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_rise_both_end+#

#+dc_tcl_measure_max_fall_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_fall_rise_end+#

#+dc_tcl_measure_max_fall_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_fall_fall_end+#

#+dc_tcl_measure_max_fall_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -fall_from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_fall_both_end+#

#+dc_tcl_measure_max_both_rise_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -rise_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_both_rise_end+#

#+dc_tcl_measure_max_both_fall_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -fall_to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_both_fall_end+#

#+dc_tcl_measure_max_both_both_begin+#
redirect -append #*dc_tcl_sub_log*# {
    set rvs [report_timing -from { #*dc_tcl_sub_from*# } -to { #*dc_tcl_sub_to*# } -path full -delay max -nworst 1 -max_paths 1 -significant_digits 5 -sort_by group]
}
if {$rvs == 0} {
    #*dc_tcl_errorcode*#
}
#+dc_tcl_measure_max_both_both_end+#
