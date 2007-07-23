#!perl
###########################################################################
# $Id: carp_fork.t,v 1.2 2002/06/26 18:20:13 sirott Exp $
###########################################################################
#
# carp_fork.t
#
# RCS Revision: $Revision: 1.2 $
# Date: $Date: 2002/06/26 18:20:13 $
#
# Copyright (C) 1999 Raphael Manfredi.
# Copyright (C) 2002 Mark Rogaski, mrogaski@cpan.org; all rights reserved.
#
# See the README file included with the
# distribution for license information.
#
# $Log: carp_fork.t,v $
# Revision 1.2  2002/06/26 18:20:13  sirott
# 	* Merged v_6_0_base_branch with main branch
#
# Revision 1.1.2.1  2002/06/02 01:10:59  sirott
# 	* Added all required Perl modules to CVS distribution
#
# Revision 1.1  2002/02/23 06:25:49  wendigo
# Initial revision
#
#
###########################################################################

use Log::Agent;
require Log::Agent::Driver::Fork;
require Log::Agent::Driver::File;

unlink 't/file.out', 't/file.err';

my $driver = Log::Agent::Driver::Fork->make(
    Log::Agent::Driver::File->make( 
        -prefix => 'me',
        -channels => {
            'error' => 't/file.err',
            'output' => 't/file.out'
        },
    )
);
logconfig(-driver => $driver);

do 't/carp.pl';

