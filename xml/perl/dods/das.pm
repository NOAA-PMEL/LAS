####################################################################
#
#    This file was generated using Parse::Yapp version 1.02.
#
#        Don't edit this file, use source file instead.
#
#             ANY CHANGE MADE HERE WILL BE LOST !
#
####################################################################
package das;
use vars qw ( @ISA );
use strict;

@ISA= qw ( Parse::Yapp::Driver );
use Parse::Yapp::Driver;

#line 1 "das.y"

  use LASDods;
  sub dods_error {
    die "Error: line $.: ", @_;
  }
  sub dods_warn {
    print STDERR "Error: line $.: ", @_, "\n";
  }
  sub initAttList {
    my ($parser) = @_;
#    print "Initing att list\n";
    $parser->YYData->{attList} = [];
  }
  sub addAttList {
    my ($parser, $value) = @_;
    $value =~ s/^"//g;
    $value =~ s/"$//g;
#    print "Adding $value\n";
    push(@{$parser->YYData->{attList}}, $value);
  }
  sub addAtt {
    my ($parser, $name) = @_;
    my $var = $parser->YYData->{var};
    if (defined($var)){
      $var->addAttribute($name, $parser->YYData->{attList});
    } else {			# It's global
      $parser->YYData->{dods}->addAttribute($name, $parser->YYData->{attList});
    }
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
			'SCAN_ATTR' => 2,
			'error' => 1
		},
		GOTOS => {
			'attribute' => 3,
			'attr_start' => 4,
			'attributes' => 5
		}
	},
	{#State 1
		DEFAULT => -5
	},
	{#State 2
		ACTIONS => {
			'LB' => 6
		}
	},
	{#State 3
		DEFAULT => -2
	},
	{#State 4
		ACTIONS => {
			'' => 7
		}
	},
	{#State 5
		ACTIONS => {
			'' => -1,
			'SCAN_ATTR' => 2,
			'error' => 1
		},
		GOTOS => {
			'attribute' => 8
		}
	},
	{#State 6
		ACTIONS => {
			'SCAN_UINT32' => -6,
			'SCAN_UINT16' => -6,
			'SCAN_FLOAT32' => -6,
			'SCAN_STRING' => -6,
			'SCAN_URL' => -6,
			'SCAN_ID' => 14,
			'error' => 15,
			'SCAN_ALIAS' => 17,
			'RB' => -6,
			'SCAN_INT32' => -6,
			'SCAN_INT16' => -6,
			'SCAN_FLOAT64' => -6,
			'SCAN_BYTE' => -6
		},
		GOTOS => {
			'@6-0' => 9,
			'@1-0' => 10,
			'alias' => 12,
			'@7-0' => 11,
			'@2-0' => 13,
			'@8-0' => 16,
			'@3-0' => 18,
			'@9-0' => 19,
			'@4-0' => 20,
			'attr_tuple' => 21,
			'@5-0' => 23,
			'attr_list' => 22
		}
	},
	{#State 7
		DEFAULT => -0
	},
	{#State 8
		DEFAULT => -3
	},
	{#State 9
		ACTIONS => {
			'SCAN_FLOAT32' => 24
		}
	},
	{#State 10
		ACTIONS => {
			'SCAN_BYTE' => 25
		}
	},
	{#State 11
		ACTIONS => {
			'SCAN_FLOAT64' => 26
		}
	},
	{#State 12
		DEFAULT => -9
	},
	{#State 13
		ACTIONS => {
			'SCAN_INT16' => 27
		}
	},
	{#State 14
		DEFAULT => -28,
		GOTOS => {
			'@10-1' => 28
		}
	},
	{#State 15
		ACTIONS => {
			'SEMICOLON' => 29
		}
	},
	{#State 16
		ACTIONS => {
			'SCAN_STRING' => 30
		}
	},
	{#State 17
		ACTIONS => {
			'SCAN_ID' => 31
		}
	},
	{#State 18
		ACTIONS => {
			'SCAN_UINT16' => 32
		}
	},
	{#State 19
		ACTIONS => {
			'SCAN_URL' => 33
		}
	},
	{#State 20
		ACTIONS => {
			'SCAN_INT32' => 34
		}
	},
	{#State 21
		DEFAULT => -7
	},
	{#State 22
		ACTIONS => {
			'SCAN_UINT32' => -18,
			'SCAN_UINT16' => -14,
			'SCAN_FLOAT32' => -20,
			'SCAN_URL' => -26,
			'SCAN_STRING' => -24,
			'SCAN_ID' => 14,
			'error' => 15,
			'SCAN_ALIAS' => 17,
			'RB' => 35,
			'SCAN_INT16' => -12,
			'SCAN_INT32' => -16,
			'SCAN_FLOAT64' => -22,
			'SCAN_BYTE' => -10
		},
		GOTOS => {
			'@6-0' => 9,
			'@1-0' => 10,
			'alias' => 12,
			'@7-0' => 11,
			'@2-0' => 13,
			'@8-0' => 16,
			'@3-0' => 18,
			'@9-0' => 19,
			'@4-0' => 20,
			'attr_tuple' => 36,
			'@5-0' => 23
		}
	},
	{#State 23
		ACTIONS => {
			'SCAN_UINT32' => 37
		}
	},
	{#State 24
		ACTIONS => {
			'SCAN_ID' => 38
		}
	},
	{#State 25
		ACTIONS => {
			'SCAN_ID' => 39
		}
	},
	{#State 26
		ACTIONS => {
			'SCAN_ID' => 40
		}
	},
	{#State 27
		ACTIONS => {
			'SCAN_ID' => 41
		}
	},
	{#State 28
		ACTIONS => {
			'LB' => 42
		}
	},
	{#State 29
		DEFAULT => -30
	},
	{#State 30
		ACTIONS => {
			'SCAN_ID' => 43
		}
	},
	{#State 31
		ACTIONS => {
			'SCAN_ID' => 44
		}
	},
	{#State 32
		ACTIONS => {
			'SCAN_ID' => 45
		}
	},
	{#State 33
		ACTIONS => {
			'SCAN_ID' => 46
		}
	},
	{#State 34
		ACTIONS => {
			'SCAN_ID' => 47
		}
	},
	{#State 35
		DEFAULT => -4
	},
	{#State 36
		DEFAULT => -8
	},
	{#State 37
		ACTIONS => {
			'SCAN_ID' => 48
		}
	},
	{#State 38
		ACTIONS => {
			'SCAN_INT' => 49,
			'SCAN_FLOAT' => 50
		},
		GOTOS => {
			'float32' => 52,
			'float_or_int' => 51
		}
	},
	{#State 39
		ACTIONS => {
			'SCAN_INT' => 53
		},
		GOTOS => {
			'bytes' => 54
		}
	},
	{#State 40
		ACTIONS => {
			'SCAN_INT' => 49,
			'SCAN_FLOAT' => 50
		},
		GOTOS => {
			'float64' => 55,
			'float_or_int' => 56
		}
	},
	{#State 41
		ACTIONS => {
			'SCAN_INT' => 57
		},
		GOTOS => {
			'int16' => 58
		}
	},
	{#State 42
		ACTIONS => {
			'SCAN_UINT32' => -6,
			'SCAN_UINT16' => -6,
			'SCAN_FLOAT32' => -6,
			'SCAN_STRING' => -6,
			'SCAN_URL' => -6,
			'SCAN_ID' => 14,
			'error' => 15,
			'SCAN_ALIAS' => 17,
			'RB' => -6,
			'SCAN_INT32' => -6,
			'SCAN_INT16' => -6,
			'SCAN_FLOAT64' => -6,
			'SCAN_BYTE' => -6
		},
		GOTOS => {
			'@6-0' => 9,
			'@1-0' => 10,
			'alias' => 12,
			'@7-0' => 11,
			'@2-0' => 13,
			'@8-0' => 16,
			'@3-0' => 18,
			'@9-0' => 19,
			'@4-0' => 20,
			'attr_tuple' => 21,
			'attr_list' => 59,
			'@5-0' => 23
		}
	},
	{#State 43
		ACTIONS => {
			'SCAN_STR' => 64,
			'SCAN_INT' => 60,
			'SCAN_FLOAT' => 61,
			'SCAN_ID' => 62
		},
		GOTOS => {
			'strs' => 65,
			'str_or_id' => 63
		}
	},
	{#State 44
		ACTIONS => {
			'SEMICOLON' => 66
		}
	},
	{#State 45
		ACTIONS => {
			'SCAN_INT' => 67
		},
		GOTOS => {
			'uint16' => 68
		}
	},
	{#State 46
		ACTIONS => {
			'SCAN_STR' => 72,
			'SCAN_ID' => 70
		},
		GOTOS => {
			'url' => 71,
			'urls' => 69
		}
	},
	{#State 47
		ACTIONS => {
			'SCAN_INT' => 73
		},
		GOTOS => {
			'int32' => 74
		}
	},
	{#State 48
		ACTIONS => {
			'SCAN_INT' => 75
		},
		GOTOS => {
			'uint32' => 76
		}
	},
	{#State 49
		DEFAULT => -56
	},
	{#State 50
		DEFAULT => -55
	},
	{#State 51
		DEFAULT => -41
	},
	{#State 52
		ACTIONS => {
			'COMMA' => 78,
			'SEMICOLON' => 77
		}
	},
	{#State 53
		DEFAULT => -31
	},
	{#State 54
		ACTIONS => {
			'COMMA' => 80,
			'SEMICOLON' => 79
		}
	},
	{#State 55
		ACTIONS => {
			'COMMA' => 82,
			'SEMICOLON' => 81
		}
	},
	{#State 56
		DEFAULT => -43
	},
	{#State 57
		DEFAULT => -33
	},
	{#State 58
		ACTIONS => {
			'COMMA' => 84,
			'SEMICOLON' => 83
		}
	},
	{#State 59
		ACTIONS => {
			'SCAN_UINT32' => -18,
			'SCAN_UINT16' => -14,
			'SCAN_FLOAT32' => -20,
			'SCAN_URL' => -26,
			'SCAN_STRING' => -24,
			'SCAN_ID' => 14,
			'error' => 15,
			'SCAN_ALIAS' => 17,
			'RB' => 85,
			'SCAN_INT16' => -12,
			'SCAN_INT32' => -16,
			'SCAN_FLOAT64' => -22,
			'SCAN_BYTE' => -10
		},
		GOTOS => {
			'@6-0' => 9,
			'@1-0' => 10,
			'alias' => 12,
			'@7-0' => 11,
			'@2-0' => 13,
			'@8-0' => 16,
			'@3-0' => 18,
			'@9-0' => 19,
			'@4-0' => 20,
			'attr_tuple' => 36,
			'@5-0' => 23
		}
	},
	{#State 60
		DEFAULT => -53
	},
	{#State 61
		DEFAULT => -54
	},
	{#State 62
		DEFAULT => -52
	},
	{#State 63
		DEFAULT => -45
	},
	{#State 64
		DEFAULT => -51
	},
	{#State 65
		ACTIONS => {
			'COMMA' => 87,
			'SEMICOLON' => 86
		}
	},
	{#State 66
		DEFAULT => -57
	},
	{#State 67
		DEFAULT => -35
	},
	{#State 68
		ACTIONS => {
			'COMMA' => 89,
			'SEMICOLON' => 88
		}
	},
	{#State 69
		ACTIONS => {
			'COMMA' => 91,
			'SEMICOLON' => 90
		}
	},
	{#State 70
		DEFAULT => -49
	},
	{#State 71
		DEFAULT => -47
	},
	{#State 72
		DEFAULT => -50
	},
	{#State 73
		DEFAULT => -37
	},
	{#State 74
		ACTIONS => {
			'COMMA' => 93,
			'SEMICOLON' => 92
		}
	},
	{#State 75
		DEFAULT => -39
	},
	{#State 76
		ACTIONS => {
			'COMMA' => 95,
			'SEMICOLON' => 94
		}
	},
	{#State 77
		DEFAULT => -21
	},
	{#State 78
		ACTIONS => {
			'SCAN_INT' => 49,
			'SCAN_FLOAT' => 50
		},
		GOTOS => {
			'float_or_int' => 96
		}
	},
	{#State 79
		DEFAULT => -11
	},
	{#State 80
		ACTIONS => {
			'SCAN_INT' => 97
		}
	},
	{#State 81
		DEFAULT => -23
	},
	{#State 82
		ACTIONS => {
			'SCAN_INT' => 49,
			'SCAN_FLOAT' => 50
		},
		GOTOS => {
			'float_or_int' => 98
		}
	},
	{#State 83
		DEFAULT => -13
	},
	{#State 84
		ACTIONS => {
			'SCAN_INT' => 99
		}
	},
	{#State 85
		DEFAULT => -29
	},
	{#State 86
		DEFAULT => -25
	},
	{#State 87
		ACTIONS => {
			'SCAN_STR' => 64,
			'SCAN_INT' => 60,
			'SCAN_FLOAT' => 61,
			'SCAN_ID' => 62
		},
		GOTOS => {
			'str_or_id' => 100
		}
	},
	{#State 88
		DEFAULT => -15
	},
	{#State 89
		ACTIONS => {
			'SCAN_INT' => 101
		}
	},
	{#State 90
		DEFAULT => -27
	},
	{#State 91
		ACTIONS => {
			'SCAN_STR' => 72,
			'SCAN_ID' => 70
		},
		GOTOS => {
			'url' => 102
		}
	},
	{#State 92
		DEFAULT => -17
	},
	{#State 93
		ACTIONS => {
			'SCAN_INT' => 103
		}
	},
	{#State 94
		DEFAULT => -19
	},
	{#State 95
		ACTIONS => {
			'SCAN_INT' => 104
		}
	},
	{#State 96
		DEFAULT => -42
	},
	{#State 97
		DEFAULT => -32
	},
	{#State 98
		DEFAULT => -44
	},
	{#State 99
		DEFAULT => -34
	},
	{#State 100
		DEFAULT => -46
	},
	{#State 101
		DEFAULT => -36
	},
	{#State 102
		DEFAULT => -48
	},
	{#State 103
		DEFAULT => -38
	},
	{#State 104
		DEFAULT => -40
	}
],
                                  yyrules  =>
[
	[#Rule 0
		 '$start', 2, undef
	],
	[#Rule 1
		 'attr_start', 1, undef
	],
	[#Rule 2
		 'attributes', 1, undef
	],
	[#Rule 3
		 'attributes', 2, undef
	],
	[#Rule 4
		 'attribute', 4, undef
	],
	[#Rule 5
		 'attribute', 1, undef
	],
	[#Rule 6
		 'attr_list', 0, undef
	],
	[#Rule 7
		 'attr_list', 1, undef
	],
	[#Rule 8
		 'attr_list', 2, undef
	],
	[#Rule 9
		 'attr_tuple', 1,
sub
#line 71 "das.y"
{dods_warn('alias not supported -- ignored'); }
	],
	[#Rule 10
		 '@1-0', 0,
sub
#line 74 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 11
		 'attr_tuple', 5,
sub
#line 80 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 12
		 '@2-0', 0,
sub
#line 85 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 13
		 'attr_tuple', 5,
sub
#line 91 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 14
		 '@3-0', 0,
sub
#line 96 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 15
		 'attr_tuple', 5,
sub
#line 102 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 16
		 '@4-0', 0,
sub
#line 107 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 17
		 'attr_tuple', 5,
sub
#line 113 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 18
		 '@5-0', 0,
sub
#line 118 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 19
		 'attr_tuple', 5,
sub
#line 124 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 20
		 '@6-0', 0,
sub
#line 129 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 21
		 'attr_tuple', 5,
sub
#line 135 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 22
		 '@7-0', 0,
sub
#line 140 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 23
		 'attr_tuple', 5,
sub
#line 146 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 24
		 '@8-0', 0,
sub
#line 151 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 25
		 'attr_tuple', 5,
sub
#line 155 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 26
		 '@9-0', 0,
sub
#line 160 "das.y"
{
		  initAttList($_[0]);
		}
	],
	[#Rule 27
		 'attr_tuple', 5,
sub
#line 166 "das.y"
{
		  addAtt($_[0], $_[3]);
		}
	],
	[#Rule 28
		 '@10-1', 0,
sub
#line 171 "das.y"
{
		  my $dods = $_[0]->YYData->{dods};
		  my $var = $dods->getVariable($_[1]);
		  if ($var){
		    $_[0]->YYData->{var} = $var;
		  } 
                }
	],
	[#Rule 29
		 'attr_tuple', 5,
sub
#line 180 "das.y"
{
		  $_[0]->YYData->{var} = undef;
		}
	],
	[#Rule 30
		 'attr_tuple', 2, undef
	],
	[#Rule 31
		 'bytes', 1,
sub
#line 188 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 32
		 'bytes', 3,
sub
#line 189 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 33
		 'int16', 1,
sub
#line 192 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 34
		 'int16', 3,
sub
#line 193 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 35
		 'uint16', 1,
sub
#line 196 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 36
		 'uint16', 3,
sub
#line 197 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 37
		 'int32', 1,
sub
#line 200 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 38
		 'int32', 3,
sub
#line 201 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 39
		 'uint32', 1,
sub
#line 204 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 40
		 'uint32', 3,
sub
#line 205 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 41
		 'float32', 1,
sub
#line 208 "das.y"
{ addAttList($_[0], $_[1]);}
	],
	[#Rule 42
		 'float32', 3,
sub
#line 209 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 43
		 'float64', 1,
sub
#line 212 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 44
		 'float64', 3,
sub
#line 213 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 45
		 'strs', 1,
sub
#line 216 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 46
		 'strs', 3,
sub
#line 217 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 47
		 'urls', 1,
sub
#line 220 "das.y"
{ addAttList($_[0], $_[1]); }
	],
	[#Rule 48
		 'urls', 3,
sub
#line 221 "das.y"
{ addAttList($_[0], $_[3]); }
	],
	[#Rule 49
		 'url', 1, undef
	],
	[#Rule 50
		 'url', 1, undef
	],
	[#Rule 51
		 'str_or_id', 1, undef
	],
	[#Rule 52
		 'str_or_id', 1, undef
	],
	[#Rule 53
		 'str_or_id', 1, undef
	],
	[#Rule 54
		 'str_or_id', 1, undef
	],
	[#Rule 55
		 'float_or_int', 1, undef
	],
	[#Rule 56
		 'float_or_int', 1, undef
	],
	[#Rule 57
		 'alias', 4, undef
	]
],
                                  @_);
    bless($self,$class);
}

#line 237 "das.y"


1;
