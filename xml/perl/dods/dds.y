%{
  use LASDods;
  my $Type;
  sub dods_error {
    die "Error: ", @_;
  }
  sub dods_warn {
    print STDERR "Error: ", @_, "\n";
  }
%}

%expect 56

%token SEMICOLON LB RB COLON LSB RSB EQUALS
%token SCAN_ID
%token SCAN_NAME
%token SCAN_INT
%token SCAN_DATASET
%token SCAN_ARRAY
%token SCAN_MAPS
%token SCAN_LIST
%token SCAN_SEQUENCE
%token SCAN_STRUCTURE
%token SCAN_GRID
%token SCAN_BYTE
%token SCAN_INT16
%token SCAN_UINT16
%token SCAN_INT32
%token SCAN_UINT32
%token SCAN_FLOAT32
%token SCAN_FLOAT64
%token SCAN_STRING
%token SCAN_URL 
%token SCAN_ERROR

%%

start:    	datasets | error_message
;

error_message:	SCAN_ERROR LB err_code err_message RB SEMICOLON
;

err_code:	SCAN_ID EQUALS SCAN_INT SEMICOLON
;

err_message:	SCAN_ID EQUALS SCAN_STR SEMICOLON
                   {dods_error("Server returned error: ",$_[3]); }
;

datasets:	dataset
		| datasets dataset
;

dataset:	SCAN_DATASET LB declarations RB name SEMICOLON
                | error
;

declarations:	/* empty */
		| declaration
		| declarations declaration
;

declaration: 	list non_list_decl
                | non_list_decl
;

/* This non-terminal is here only to keep types like `List List Int32' from
   parsing. DODS does not allow Lists of Lists. Those types make translation
   to/from arrays too hard. */

non_list_decl:  base_type { $Type = $_[1] } var SEMICOLON
		| structure  LB declarations RB var SEMICOLON 
		| sequence LB declarations RB var SEMICOLON 
		| grid LB SCAN_ARRAY COLON declaration SCAN_MAPS COLON 
                  declarations RB 
                  var SEMICOLON
                | error
;
 

list:		SCAN_LIST { dods_error('DODS list not supported'); }
;

structure:	SCAN_STRUCTURE { dods_error('DODS structure not supported');}
;

sequence:	SCAN_SEQUENCE { dods_error('DODS sequence not supported'); }
;

grid:		SCAN_GRID 
;

base_type:	SCAN_BYTE
		| SCAN_INT16
		| SCAN_UINT16
		| SCAN_INT32
		| SCAN_UINT32
		| SCAN_FLOAT32
		| SCAN_FLOAT64 
		| SCAN_STRING
		| SCAN_URL
;

var:		SCAN_ID
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
 		| var array_decl
;

array_decl:	LSB SCAN_INT RSB
                 {
		   my $dods = $_[0]->YYData->{dods};
		   my $var = $_[0]->YYData->{var};
		   if (defined $var){
		     $var->addDim(new LAS::DODS::Dim($var->getName, $dods, $_[2]));
		   }
		 }
		 | LSB SCAN_ID EQUALS SCAN_INT RSB
                 {
		   my $dods = $_[0]->YYData->{dods};
		   my $var = $_[0]->YYData->{var};
		   if (defined $var){
		     $var->addDim(new LAS::DODS::Dim($_[2], $dods, $_[4]));
		   }
		 }
		 | error
;

name:		SCAN_NAME 
		| SCAN_ID 
                | error 
;

%%
