# $Id: Makefile.PL,v 1.2 2002/06/26 18:20:08 sirott Exp $
#
#  Copyright (c) 1999, Raphael Manfredi
#  
#  You may redistribute only under the terms of the Artistic License,
#  as specified in the README file that comes with the distribution.
#
# HISTORY
# $Log: Makefile.PL,v $
# Revision 1.2  2002/06/26 18:20:08  sirott
# 	* Merged v_6_0_base_branch with main branch
#
# Revision 1.1.2.1  2002/06/02 01:10:52  sirott
# 	* Added all required Perl modules to CVS distribution
#
# Revision 1.1  2002/03/09 16:28:34  wendigo
# New maintainer
#
# Revision 0.2  2000/11/06 19:30:32  ram
# Baseline for second Alpha release.
#
# $EndLog$
#

use ExtUtils::MakeMaker;

WriteMakefile(
    'NAME'	=> 'Log::Agent',
    'VERSION_FROM' => 'Agent.pm', # finds $VERSION
    'LIBS'	=> [''],		# e.g., '-lm' 
    'DEFINE'	=> '',		# e.g., '-DHAVE_SOMETHING' 
    'INC'	=> '',			# e.g., '-I/usr/include/other' 
	# 'PREFIX' => '/home/ram/usr/lib/site_perl',
);

