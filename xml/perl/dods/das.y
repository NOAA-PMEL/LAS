%{
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
%}

%token SEMICOLON LB RB COMMA
%token SCAN_ATTR

%token SCAN_ID
%token SCAN_INT
%token SCAN_FLOAT
%token SCAN_STR
%token SCAN_ALIAS

%token SCAN_BYTE
%token SCAN_INT16
%token SCAN_UINT16
%token SCAN_INT32
%token SCAN_UINT32
%token SCAN_FLOAT32
%token SCAN_FLOAT64
%token SCAN_STRING
%token SCAN_URL

%%

attr_start:
                attributes
;

attributes:     attribute
    	    	| attributes attribute

;
    	    	
attribute:    	SCAN_ATTR LB attr_list RB
                | error
;

attr_list:  	/* empty */
    	    	| attr_tuple
    	    	| attr_list attr_tuple
;

attr_tuple:	alias {dods_warn('alias not supported -- ignored'); }

                |
                {
		  initAttList($_[0]);
		}
                SCAN_BYTE 
                SCAN_ID  
		bytes SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_INT16  
                SCAN_ID  
		int16 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_UINT16  
                SCAN_ID  
		uint16 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_INT32  
                SCAN_ID  
		int32 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_UINT32  
                SCAN_ID  
		uint32 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_FLOAT32  
                SCAN_ID  
		float32 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		| 
                {
		  initAttList($_[0]);
		}
                SCAN_FLOAT64  
                SCAN_ID  
		float64 SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		| 
                {
		  initAttList($_[0]);
		}
                SCAN_STRING SCAN_ID strs SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		|
                {
		  initAttList($_[0]);
		}
                SCAN_URL  
                SCAN_ID  
		urls SEMICOLON
                {
		  addAtt($_[0], $_[3]);
		}

		| SCAN_ID 
                {
		  my $dods = $_[0]->YYData->{dods};
		  my $var = $dods->getVariable($_[1]);
		  if ($var){
		    $_[0]->YYData->{var} = $var;
		  } 
                }
		LB attr_list 
		RB
                {
		  $_[0]->YYData->{var} = undef;
		}

		| error 
                SEMICOLON
;

bytes:		SCAN_INT { addAttList($_[0], $_[1]); }
		| bytes COMMA SCAN_INT { addAttList($_[0], $_[3]); }
;

int16:		SCAN_INT { addAttList($_[0], $_[1]); }
		| int16 COMMA SCAN_INT { addAttList($_[0], $_[3]); }
;

uint16:		SCAN_INT { addAttList($_[0], $_[1]); }
		| uint16 COMMA SCAN_INT { addAttList($_[0], $_[3]); }
;

int32:		SCAN_INT { addAttList($_[0], $_[1]); }
		| int32 COMMA SCAN_INT { addAttList($_[0], $_[3]); }
;

uint32:		SCAN_INT { addAttList($_[0], $_[1]); }
		| uint32 COMMA SCAN_INT { addAttList($_[0], $_[3]); }
;

float32:	float_or_int { addAttList($_[0], $_[1]);}
		| float32 COMMA float_or_int { addAttList($_[0], $_[3]); }
;

float64:	float_or_int { addAttList($_[0], $_[1]); }
		| float64 COMMA float_or_int { addAttList($_[0], $_[3]); }
;

strs:		str_or_id { addAttList($_[0], $_[1]); }
		| strs COMMA str_or_id { addAttList($_[0], $_[3]); }
;

urls:		url { addAttList($_[0], $_[1]); }
		| urls COMMA url { addAttList($_[0], $_[3]); }
;

url:		SCAN_ID | SCAN_STR
;

str_or_id:	SCAN_STR | SCAN_ID | SCAN_INT | SCAN_FLOAT
;

float_or_int:   SCAN_FLOAT | SCAN_INT
;

alias:          SCAN_ALIAS SCAN_ID 
                SCAN_ID
                SEMICOLON
;
%%
