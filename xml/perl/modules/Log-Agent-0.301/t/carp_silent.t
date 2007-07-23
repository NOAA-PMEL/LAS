#!./perl

#
# $Id: carp_silent.t,v 1.2 2002/06/26 18:20:13 sirott Exp $
#
#  Copyright (c) 1999, Raphael Manfredi
#  
#  You may redistribute only under the terms of the Artistic License,
#  as specified in the README file that comes with the distribution.
#
# HISTORY
# $Log: carp_silent.t,v $
# Revision 1.2  2002/06/26 18:20:13  sirott
# 	* Merged v_6_0_base_branch with main branch
#
# Revision 1.1.2.1  2002/06/02 01:10:59  sirott
# 	* Added all required Perl modules to CVS distribution
#
# Revision 1.1  2002/03/09 16:16:55  wendigo
# New maintainer
#
# Revision 0.2  2000/11/06 19:30:33  ram
# Baseline for second Alpha release.
#
# $EndLog$
#

print "1..2\n";

require 't/code.pl';
sub ok;

use Log::Agent;
require Log::Agent::Driver::Silent;

open(ORIG_STDOUT, ">&STDOUT") || die "can't dup STDOUT: $!\n";
select(ORIG_STDOUT);

open(STDOUT, ">t/file.out") || die "can't redirect STDOUT: $!\n";
open(STDERR, ">t/file.err") || die "can't redirect STDOUT: $!\n";

my $driver = Log::Agent::Driver::Silent->make();
logconfig(-driver => $driver);

sub test {
	logcarp "none";
	logcroak "test";
}

my $line = __LINE__ + 1;
test();

sub END {
	ok 1, !contains("t/file.err", "none");
	ok 2, contains("t/file.err", "test at t/carp_silent.t line $line");

	unlink 't/file.out', 't/file.err';
	exit 0;
}

