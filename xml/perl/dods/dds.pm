####################################################################
#
#    This file was generated using Parse::Yapp version 1.02.
#
#        Don't edit this file, use source file instead.
#
#             ANY CHANGE MADE HERE WILL BE LOST !
#
####################################################################
package dds;
use vars qw ( @ISA );
use strict;

@ISA= qw ( Parse::Yapp::Driver );
use Parse::Yapp::Driver;

#line 1 "dds.y"

  use LASDods;
  my $Type;
  sub dods_error {
    die "Error: ", @_;
  }
  sub dods_warn {
    print STDERR "Error: ", @_, "\n";
  }


sub new {
        my($class)=shift;
        ref($class)
    and $class=ref($class);

    my($self)=$class->SUPER::new( yyversion => '1.02',
                                  yystates =>
[
	{#State 0
		ACTIONS => {
			'error' => 1,
			'SCAN_DATASET' => 3,
			'SCAN_ERROR' => 5
		},
		GOTOS => {
			'dataset' => 2,
			'start' => 4,
			'datasets' => 7,
			'error_message' => 6
		}
	},
	{#State 1
		DEFAULT => -9
	},
	{#State 2
		DEFAULT => -6
	},
	{#State 3
		ACTIONS => {
			'LB' => 8
		}
	},
	{#State 4
		ACTIONS => {
			'' => 9
		}
	},
	{#State 5
		ACTIONS => {
			'LB' => 10
		}
	},
	{#State 6
		DEFAULT => -2
	},
	{#State 7
		ACTIONS => {
			'' => -1,
			'error' => 1,
			'SCAN_DATASET' => 3
		},
		GOTOS => {
			'dataset' => 11
		}
	},
	{#State 8
		ACTIONS => {
			'SCAN_UINT32' => 21,
			'SCAN_UINT16' => 20,
			'SCAN_FLOAT32' => 24,
			'SCAN_LIST' => 23,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'RB' => -10,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_FLOAT64' => 33,
			'SCAN_STRUCTURE' => 32,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'declarations' => 13,
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 16,
			'grid' => 26,
			'base_type' => 25,
			'sequence' => 18,
			'structure' => 17
		}
	},
	{#State 9
		DEFAULT => -0
	},
	{#State 10
		ACTIONS => {
			'SCAN_ID' => 35
		},
		GOTOS => {
			'err_code' => 34
		}
	},
	{#State 11
		DEFAULT => -7
	},
	{#State 12
		DEFAULT => -14
	},
	{#State 13
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'RB' => 36,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 37,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 14
		DEFAULT => -28
	},
	{#State 15
		DEFAULT => -26
	},
	{#State 16
		DEFAULT => -11
	},
	{#State 17
		ACTIONS => {
			'LB' => 38
		}
	},
	{#State 18
		ACTIONS => {
			'LB' => 39
		}
	},
	{#State 19
		DEFAULT => -25
	},
	{#State 20
		DEFAULT => -27
	},
	{#State 21
		DEFAULT => -29
	},
	{#State 22
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 40,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 23
		DEFAULT => -21
	},
	{#State 24
		DEFAULT => -30
	},
	{#State 25
		DEFAULT => -15,
		GOTOS => {
			'@1-1' => 41
		}
	},
	{#State 26
		ACTIONS => {
			'LB' => 42
		}
	},
	{#State 27
		DEFAULT => -24
	},
	{#State 28
		DEFAULT => -33
	},
	{#State 29
		DEFAULT => -32
	},
	{#State 30
		DEFAULT => -20
	},
	{#State 31
		DEFAULT => -23
	},
	{#State 32
		DEFAULT => -22
	},
	{#State 33
		DEFAULT => -31
	},
	{#State 34
		ACTIONS => {
			'SCAN_ID' => 44
		},
		GOTOS => {
			'err_message' => 43
		}
	},
	{#State 35
		ACTIONS => {
			'EQUALS' => 45
		}
	},
	{#State 36
		ACTIONS => {
			'error' => 49,
			'SCAN_NAME' => 47,
			'SCAN_ID' => 48
		},
		GOTOS => {
			'name' => 46
		}
	},
	{#State 37
		DEFAULT => -12
	},
	{#State 38
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'RB' => -10,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'declarations' => 50,
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 16,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 39
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'RB' => -10,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'declarations' => 51,
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 16,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 40
		DEFAULT => -13
	},
	{#State 41
		ACTIONS => {
			'SCAN_ID' => 53
		},
		GOTOS => {
			'var' => 52
		}
	},
	{#State 42
		ACTIONS => {
			'SCAN_ARRAY' => 54
		}
	},
	{#State 43
		ACTIONS => {
			'RB' => 55
		}
	},
	{#State 44
		ACTIONS => {
			'EQUALS' => 56
		}
	},
	{#State 45
		ACTIONS => {
			'SCAN_INT' => 57
		}
	},
	{#State 46
		ACTIONS => {
			'SEMICOLON' => 58
		}
	},
	{#State 47
		DEFAULT => -39
	},
	{#State 48
		DEFAULT => -40
	},
	{#State 49
		DEFAULT => -41
	},
	{#State 50
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'RB' => 59,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 37,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 51
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'RB' => 60,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 37,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 52
		ACTIONS => {
			'error' => 64,
			'LSB' => 62,
			'SEMICOLON' => 63
		},
		GOTOS => {
			'array_decl' => 61
		}
	},
	{#State 53
		DEFAULT => -34
	},
	{#State 54
		ACTIONS => {
			'COLON' => 65
		}
	},
	{#State 55
		ACTIONS => {
			'SEMICOLON' => 66
		}
	},
	{#State 56
		ACTIONS => {
			'SCAN_STR' => 67
		}
	},
	{#State 57
		ACTIONS => {
			'SEMICOLON' => 68
		}
	},
	{#State 58
		DEFAULT => -8
	},
	{#State 59
		ACTIONS => {
			'SCAN_ID' => 53
		},
		GOTOS => {
			'var' => 69
		}
	},
	{#State 60
		ACTIONS => {
			'SCAN_ID' => 53
		},
		GOTOS => {
			'var' => 70
		}
	},
	{#State 61
		DEFAULT => -35
	},
	{#State 62
		ACTIONS => {
			'SCAN_INT' => 71,
			'SCAN_ID' => 72
		}
	},
	{#State 63
		DEFAULT => -16
	},
	{#State 64
		DEFAULT => -38
	},
	{#State 65
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 73,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 66
		DEFAULT => -3
	},
	{#State 67
		ACTIONS => {
			'SEMICOLON' => 74
		}
	},
	{#State 68
		DEFAULT => -4
	},
	{#State 69
		ACTIONS => {
			'error' => 64,
			'LSB' => 62,
			'SEMICOLON' => 75
		},
		GOTOS => {
			'array_decl' => 61
		}
	},
	{#State 70
		ACTIONS => {
			'error' => 64,
			'LSB' => 62,
			'SEMICOLON' => 76
		},
		GOTOS => {
			'array_decl' => 61
		}
	},
	{#State 71
		ACTIONS => {
			'RSB' => 77
		}
	},
	{#State 72
		ACTIONS => {
			'EQUALS' => 78
		}
	},
	{#State 73
		ACTIONS => {
			'SCAN_MAPS' => 79
		}
	},
	{#State 74
		DEFAULT => -5
	},
	{#State 75
		DEFAULT => -17
	},
	{#State 76
		DEFAULT => -18
	},
	{#State 77
		DEFAULT => -36
	},
	{#State 78
		ACTIONS => {
			'SCAN_INT' => 80
		}
	},
	{#State 79
		ACTIONS => {
			'COLON' => 81
		}
	},
	{#State 80
		ACTIONS => {
			'RSB' => 82
		}
	},
	{#State 81
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'RB' => -10,
			'SCAN_SEQUENCE' => 31,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'declarations' => 83,
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 16,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 82
		DEFAULT => -37
	},
	{#State 83
		ACTIONS => {
			'SCAN_UINT16' => 20,
			'SCAN_UINT32' => 21,
			'SCAN_LIST' => 23,
			'SCAN_FLOAT32' => 24,
			'SCAN_GRID' => 27,
			'SCAN_STRING' => 29,
			'SCAN_URL' => 28,
			'error' => 30,
			'SCAN_SEQUENCE' => 31,
			'RB' => 84,
			'SCAN_INT32' => 14,
			'SCAN_INT16' => 15,
			'SCAN_STRUCTURE' => 32,
			'SCAN_FLOAT64' => 33,
			'SCAN_BYTE' => 19
		},
		GOTOS => {
			'non_list_decl' => 12,
			'list' => 22,
			'declaration' => 37,
			'base_type' => 25,
			'grid' => 26,
			'structure' => 17,
			'sequence' => 18
		}
	},
	{#State 84
		ACTIONS => {
			'SCAN_ID' => 53
		},
		GOTOS => {
			'var' => 85
		}
	},
	{#State 85
		ACTIONS => {
			'error' => 64,
			'LSB' => 62,
			'SEMICOLON' => 86
		},
		GOTOS => {
			'array_decl' => 61
		}
	},
	{#State 86
		DEFAULT => -19
	}
],
                                  yyrules  =>
[
	[#Rule 0
		 '$start', 2, undef
	],
	[#Rule 1
		 'start', 1, undef
	],
	[#Rule 2
		 'start', 1, undef
	],
	[#Rule 3
		 'error_message', 6, undef
	],
	[#Rule 4
		 'err_code', 4, undef
	],
	[#Rule 5
		 'err_message', 4,
sub
#line 48 "dds.y"
{dods_error("Server returned error: ",$_[3]); }
	],
	[#Rule 6
		 'datasets', 1, undef
	],
	[#Rule 7
		 'datasets', 2, undef
	],
	[#Rule 8
		 'dataset', 6, undef
	],
	[#Rule 9
		 'dataset', 1, undef
	],
	[#Rule 10
		 'declarations', 0, undef
	],
	[#Rule 11
		 'declarations', 1, undef
	],
	[#Rule 12
		 'declarations', 2, undef
	],
	[#Rule 13
		 'declaration', 2, undef
	],
	[#Rule 14
		 'declaration', 1, undef
	],
	[#Rule 15
		 '@1-1', 0,
sub
#line 72 "dds.y"
{ $Type = $_[1] }
	],
	[#Rule 16
		 'non_list_decl', 4, undef
	],
	[#Rule 17
		 'non_list_decl', 6, undef
	],
	[#Rule 18
		 'non_list_decl', 6, undef
	],
	[#Rule 19
		 'non_list_decl', 11, undef
	],
	[#Rule 20
		 'non_list_decl', 1, undef
	],
	[#Rule 21
		 'list', 1,
sub
#line 82 "dds.y"
{ dods_error('DODS list not supported'); }
	],
	[#Rule 22
		 'structure', 1,
sub
#line 85 "dds.y"
{ dods_error('DODS structure not supported');}
	],
	[#Rule 23
		 'sequence', 1,
sub
#line 88 "dds.y"
{ dods_error('DODS sequence not supported'); }
	],
	[#Rule 24
		 'grid', 1, undef
	],
	[#Rule 25
		 'base_type', 1, undef
	],
	[#Rule 26
		 'base_type', 1, undef
	],
	[#Rule 27
		 'base_type', 1, undef
	],
	[#Rule 28
		 'base_type', 1, undef
	],
	[#Rule 29
		 'base_type', 1, undef
	],
	[#Rule 30
		 'base_type', 1, undef
	],
	[#Rule 31
		 'base_type', 1, undef
	],
	[#Rule 32
		 'base_type', 1, undef
	],
	[#Rule 33
		 'base_type', 1, undef
	],
	[#Rule 34
		 'var', 1,
sub
#line 106 "dds.y"
{
		  my $dods = $_[0]->YYData->{dods};
		  my $var = $dods->getVariable($_[1]);
		  if (!$var){
		    $var = new LAS::DODS::Variable($_[1], $dods, $Type);
		    $_[0]->YYData->{var} = $var;
		  } else {
		    $_[0]->YYData->{var} = undef;
		  }
		}
	],
	[#Rule 35
		 'var', 2, undef
	],
	[#Rule 36
		 'array_decl', 3,
sub
#line 120 "dds.y"
{
		   my $dods = $_[0]->YYData->{dods};
		   my $var = $_[0]->YYData->{var};
		   if (defined $var){
		     $var->addDim(new LAS::DODS::Dim($var->getName, $dods, $_[2]));
		   }
		 }
	],
	[#Rule 37
		 'array_decl', 5,
sub
#line 128 "dds.y"
{
		   my $dods = $_[0]->YYData->{dods};
		   my $var = $_[0]->YYData->{var};
		   if (defined $var){
		     $var->addDim(new LAS::DODS::Dim($_[2], $dods, $_[4]));
		   }
		 }
	],
	[#Rule 38
		 'array_decl', 1, undef
	],
	[#Rule 39
		 'name', 1, undef
	],
	[#Rule 40
		 'name', 1, undef
	],
	[#Rule 41
		 'name', 1, undef
	]
],
                                  @_);
    bless($self,$class);
}

#line 143 "dds.y"


1;
