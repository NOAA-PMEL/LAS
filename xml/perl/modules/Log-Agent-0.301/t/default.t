#!./perl

#
# $Id: default.t,v 1.2 2002/06/26 18:20:13 sirott Exp $
#
#  Copyright (c) 1999, Raphael Manfredi
#  
#  You may redistribute only under the terms of the Artistic License,
#  as specified in the README file that comes with the distribution.
#
# HISTORY
# $Log: default.t,v $
# Revision 1.2  2002/06/26 18:20:13  sirott
# 	* Merged v_6_0_base_branch with main branch
#
# Revision 1.1.2.1  2002/06/02 01:10:59  sirott
# 	* Added all required Perl modules to CVS distribution
#
# Revision 1.1  2002/03/09 16:16:55  wendigo
# New maintainer
#
# Revision 0.2  2000/11/06 19:30:34  ram
# Baseline for second Alpha release.
#
# $EndLog$
#

print "1..4\n";

require 't/code.pl';
sub ok;

use Log::Agent;

open(ORIG_STDOUT, ">&STDOUT") || die "can't dup STDOUT: $!\n";
select(ORIG_STDOUT);

open(STDOUT, ">t/default.out") || die "can't redirect STDOUT: $!\n";
open(STDERR, ">t/default.err") || die "can't redirect STDERR: $!\n";

logerr "error";
logsay "message";
logtrc 'debug', "debug";

close STDOUT;
close STDERR;

ok 1, contains("t/default.err", '^Error$');
ok 2, contains("t/default.err", '^Message$');
ok 3, !contains("t/default.err", '^Debug$');
ok 4, 0 == -s "t/default.out";

unlink 't/default.out', 't/default.err';

