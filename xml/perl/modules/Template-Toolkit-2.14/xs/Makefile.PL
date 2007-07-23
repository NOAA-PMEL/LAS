# $Id: Makefile.PL,v 1.1.1.1 2005/02/07 21:46:46 callahan Exp $

use lib qw( ../lib ./lib );
use ExtUtils::MakeMaker;
use Template;

WriteMakefile(
    'NAME'     => 'Template::Stash::XS',
    'C'        => [ qw( Stash.c ) ],
    'XS'       => { 'Stash.xs' => 'Stash.c' },
    'OBJECT'   => 'Stash.o',
    'VERSION'  => $Template::VERSION,
    'NORECURS' => 1,
);

