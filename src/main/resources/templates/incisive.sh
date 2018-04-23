#+sim_sh_setup_begin+#
rm -rf INCA_libs
rm -rf waves.shm
rm -f #*sim_sh_sdffile*#.X
#+sim_sh_setup_end+#

#+sim_sh_compile_verilog_begin+#
#*sim_sh_vcompiler*# -work worklib -sv -logfile #*sim_sh_log*# -append_log -errormax 15 -update -linedebug -status #*sim_sh_vfile*# > /dev/null
status=$?
if [ "$status" -ne "0" ] ; then
  exit #*sim_sh_exitcode*#
fi
#+sim_sh_compile_verilog_end+#

#+sim_sh_compile_sdf_begin+#
#*sim_sh_sdfcompiler*# -logfile #*sim_sh_log*# -append_log -compile -status #*sim_sh_sdffile*# > /dev/null
status=$?
if [ "$status" -ne "0" ] ; then
  exit #*sim_sh_exitcode*#
fi
#+sim_sh_compile_sdf_end+#

#+sim_sh_elaborate_begin+#
#*sim_sh_elaborator*# -work worklib -logfile #*sim_sh_log*# -append_log -errormax 15 -timescale 1ps/1fs -access +wc -status -sdf_cmd_file #*sim_sh_sdfcmdfile*# worklib.#*sim_sh_tbroot*# > /dev/null
status=$?
if [ "$status" -ne "0" ] ; then
  exit #*sim_sh_exitcode*#
fi
#+sim_sh_elaborate_end+#

#+sim_sh_start_terminal_begin+#
#*sim_sh_simulator*# -NONTCGLITCH -input #*sim_tcl_file*# -logfile #*sim_sh_simlog*# -errormax 15 -status worklib.#*sim_sh_tbroot*#
status=$?
if [ "$status" -ne "0" ] ; then
  exit #*sim_sh_exitcode*#
fi
#+sim_sh_start_terminal_end+#

#+sim_sh_start_gui_begin+#
#*sim_sh_simulator*# -NONTCGLITCH -gui -logfile #*sim_sh_simlog*# -errormax 15 -status worklib.#*sim_sh_tbroot*#
status=$?
if [ "$status" -ne "0" ] ; then
  exit #*sim_sh_exitcode*#
fi
#+sim_sh_start_gui_end+#