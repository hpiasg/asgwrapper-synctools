#+sim_tcl_run_begin+#
database -open -vcd ncsim -into #*sim_tcl_vcdfile*#
probe -create main -depth all -vcd -database ncsim
run
database -close ncsim
exit
#+sim_tcl_run_end+#