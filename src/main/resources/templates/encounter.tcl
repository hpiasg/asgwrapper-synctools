#+pt_tcl_begin+#
set si_enable_analysis TRUE
set power_enable_analysis TRUE
set power_analysis_mode time_based

set search_path [concat {*} $search_path]
set link_path [concat * $link_path]

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

set rvs [read_vcd #*pt_tcl_vcdfile*# -strip_path #*pt_tcl_scope*# -time {#-pt_times-#}]
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
