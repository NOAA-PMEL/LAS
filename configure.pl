#
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: configure.pl,v 1.157 2005/10/25 23:26:44 xwang Exp $
#
use Cwd;              # getcwd()
use File::Copy;       # cp()
use File::Path;       # mkpath()
use File::Basename;   # fileparse(), basename(), dirname()

#
# LAS configuration
#
my $PERLBIN = shift;
#
# Ferret environment variable names
#
my @EnvVars = qw(FER_DIR FER_DESCR FER_DATA FER_GRIDS FER_PALETTE
                 FER_GO PLOTFONTS FER_EXTERNAL_FUNCTIONS DODS_CONF);

sub usage {
    print STDERR "Usage: configure.pl [-noui]\n";
    exit 1;
}

if ($ARGV[0] && $ARGV[0] ne '-noui'){
    usage;
}

my $wantUI = 1;

if ($ARGV[0] && $ARGV[0] eq '-noui'){
   $wantUI=0;
}

#
# Scripts to edit
#
    
my @Scripts = qw(build.xml
                 WebContent/fds/fds.xml
                 xml/perl/genLas.pl
                 xml/perl/template.xml
                 xml/perl/genTests.pl
                 xml/perl/printMethods.pl
                 xml/perl/testReq.pl
                 bin/initialize_check.sh
                 bin/las_ui_check.sh
                 conf/example/sample_las.xml
                 conf/example/sample_ui.xml
                 conf/example/sample_insitu_las.xml
                 conf/example/sample_insitu_ui.xml
                 conf/example/productserver.xml
                 conf/example/LAS_config.pl
                 JavaSource/resources/ferret/FerretBackendConfig.xml.base
                 JavaSource/resources/kml/KMLBackendConfig.xml
                 JavaSource/resources/database/DatabaseBackendConfig.xml
                 WebContent/WEB-INF/struts-config.xml
                 WebContent/WEB-INF/web.xml
                 WebContent/TestLinks.html
                 );

my $configp = "config.results";
if (-f $configp){
    do $configp;
}


package main;
my $modules_list;

use URI::URL;

sub error {
    my $mess = shift;
    print "$mess\n";
    print "Press any key to continue:";
    my $ans = <STDIN>;
}

sub saveConfig {
    my $status = open CONFIG, ">$configp";
    if (! $status){
        error "\nCan't write $configp file\nIf you rerun the configuration, you will have to reenter all of the configuration parameters.\n";
        return 0;
    }
    foreach my $key (keys %LasConfig){
        print CONFIG '$LasConfig{',$key,'} = \'',$LasConfig{$key},"';\n";
    }
    print CONFIG "1;\n";
    close CONFIG;
}

sub getExecutable {
    my ($file) = @_;
    foreach my $path (split ':',$ENV{PATH}){
        my $checkfile = "$path/$file";
        if (-x "$checkfile"){
            return $checkfile;
        }
    }
    "";
}

sub mychmod {
    my $mask = shift;
    my $file = shift;
    my @values = stat $file;
    if (($values[2] & $mask) != $mask){
        chmod $mask, $file or
            die "Couldn't change file mode for $file; need to run as root";
    }
}

sub validate_port {
    my $port = shift;
    if ($port <= 0 || $port > 65535){
        print "Invalid port: $port. Must have value between 1 and 65535\n";
        return 0;
    }
    my $result = `netstat --numeric-ports --all | grep $port`;
    if ($result) {
        print "Port: $port is already in use.  You may continue if this is a previously installed Tomcat.\n";
        my $ans = getYesOrNo("Do you want to use this port");
        return $ans;
    }
    1;
}

#
# See if we're running as root, print warning if not
#

my $AmRoot = 0;
my $name = getpwuid $>;
$AmRoot = 1 if $name eq "root";
#
# Make sure Ferret environment variable has been set up
#

if (! $ENV{FER_DIR} ){
    print <<EOF;
  Your FERRET environment has not been properly set up.
  (The environment variable FER_DIR is not defined)

  Have you executed "source your_system_path/ferret_paths" ?
  You need to do this before configuring LAS.
EOF
    exit 1;
}

#
# Save perl path
#
$LasConfig{perl} = $Config{perlpath};


#
# Search for Ferret, make sure it is correct version
#

$ENV{PATH} = $ENV{PATH} . ":/usr/local/ferret/bin/";
my ($ferret, $autoferret);
$autoferret = $LasConfig{ferret};
if (! $autoferret){
    $autoferret = getExecutable('ferret');
}
while (! $ferret){
    print "\nLocation of ferret executable: [$autoferret] ";
    $ferret = <STDIN>;
    chomp($ferret);
    $ferret = $autoferret if ! $ferret;
    if (! -x $ferret){
        print "$ferret is not an executable file\n";
        $ferret = undef;
    } else {
        print "Verifying Ferret version. This might take a few minutes...\n";
        my $isFerret = 0;
        my $testres = `echo exit | $ferret -nojnl`;
        my @lines = split /^/m, $testres;
        my $ferretVersion = "6.02";
        foreach my $line (@lines){
            my @words = split(/\s+/,$line);
            if ($words[1] =~ /Version|FERRET/){
                $words[2] =~ s/^v//;
                my $version = $words[2];
                if ($version < $ferretVersion){
                    print "\nYou need to upgrade Ferret.\n";
                    print "You need at least version $ferretVersion.\n\n";
                    exit 1;
                } else {
                    $isFerret = 1;
                }

            }
        }
        if (! $isFerret){
            print "\n$ferret is not the Ferret program\n\n";
            $ferret = undef;
        }
    }
}
$LasConfig{ferret} = $ferret;
print "You have a valid version of Ferret.\n\n";
if ( $version > 6.0 && $version < $ferretVersion ) {
   print "\nThe generate script product requires Ferret 6.02+.\n";
   print "\nYou should upgrade your Ferret. \n";
   print "Until then the 'Generate scripts' product may not work in all cases.\n";
}

#
# Check for appropriate Java virtual machine
#

my ($java, $autojava);
$autojava = $LasConfig{java};
if (! $autojava){
    $autojava = getExecutable('java');
}
while (! $java){
    print "Location of java executable: [$autojava] ";
    $java = <STDIN>;
    chomp($java);
    $java = $autojava if ! $java;
    if (! -x $java){
        print "$java is not an executable file\n";
        $java = undef;
    } else {
        print "Verifying Java version...\n";
        my $isJava = 0;
        open STATUS, "$java -version 2>&1|"
            or die "Can't run $java";
        my $line;
        while(<STATUS>){
            if (/^java/){
                $line = $_;
                last;
            }
        }
        close STATUS;
        if (defined $line){
            my @pieces = split(' ', $line);
            my $vstring = $pieces[$#pieces];
            $vstring =~ s/\"//g;
            my ($major,$minor) = split(/\./, $vstring);
            if ($major < 2 && $minor < 4){
                print "Java version is $vstring. Must have at least 1.4\n";
            } else {
                $isJava = 1;
            }
        }
        if (! $isJava){
            print "\n$java is not the Java program or not the right version\n\n";
            $java = undef;
        }
    }
}
$LasConfig{java} = $java;
print "You have a valid version of Java.\n\n";

#
# Check for jar tool
#

my $java_base = dirname($LasConfig{java});
my $jar = "$java_base/jar";
if (! -x $jar){
    print "Can't find Java jar tool: $jar\n";
    print "This configuration script requires the Java jar tool which is\n";
    print "part of the Java SDK. Please download a full Java SDK and try again\n";
    exit 1;
}

#
# Set up servlet in existing tomcat distribution:
#

$LasConfig{jakarta_home} = "/usr/local/tomcat" if !defined($LasConfig{jakarta_home});

my $jakarta_home = $LasConfig{jakarta_home};
my $different_Tomcat = $jakarta_home eq "las_servlet/jakarta" ? 1 : 0;

# NOTE:  There are no 'auto_' settings for the servlet ports
# NOTE:  'servlet_shutdown_port' and 'servlet_connector_port' don't seem to be used 
my ($servlet_port, $autoservlet_port);
my ($servlet_shutdown_port, $autoservlet_shutdown_port);
my ($servlet_connector_port, $autoservlet_connector_port);
$jakarta_home = getAnswer("Full path of Tomcat JAKARTA_HOME directory where you would like to deploy the servlet", $jakarta_home);
$LasConfig{jakarta_home} = $jakarta_home;
$LasConfig{webapps}="$jakarta_home/webapps";

$autoservlet_port = $LasConfig{servlet_port};
while (!$servlet_port){
    $servlet_port = getAnswer("Which HTTP port does the Tomcat server use",
                                       $autoservlet_port);
    $LasConfig{servlet_port} = $servlet_port;
}

$LasConfig{use_las_tomcat} = 0;

my $hstn = `hostname`;
chomp $hstn;
my $tomcat_hostname;
my $autotomcathostname = $LasConfig{tomcat_hostname};
if (! $autotomcathostname){
    ($autotomcathostname) = gethostbyname $hstn;
    chomp $autotomcathostname;
}

while (! $tomcat_hostname){
    print "Enter the full domain name of the Tomcat Server (do not include the port number): [$autotomcathostname] ";
    $tomcat_hostname = <STDIN>;
    chomp $tomcat_hostname;
    $tomcat_hostname = $autotomcathostname if ! $tomcat_hostname;
    if ($tomcat_hostname !~ /\./){
        print "You must enter a full domain name\n";
        $tomcat_hostname = undef;
    }
}
$LasConfig{tomcat_hostname} = $tomcat_hostname;

use DBI;              # MySQL access
                      # This 'use' declaration does not appear 
                      # near the top so that the code in the 
                      # BEGIN block has a chance to install
                      # the DBI module first.
#
# Database stuff
#

sub db_login($$$$;$) {
    my ($db, $mysql_host, $mysql_account, $mysql_pass, $printError) = @_;
    $printError = 1 if ! defined $printError;
    $db = $db.":$mysql_host";
    eval {
        $dbh = DBI->connect($db, $mysql_account,
                               $mysql_pass, {RaiseError => 1, PrintError=>$printError});
    };
    if ($@){
        if ($printError){
            print "Error received when attempting to connect to database\n";
            print "Error was: $@";
        }
        $dbh = "";
    }
    return $dbh;
}

sub db_prompted_login($$$) {
    my ($db, $mess, $account) = @_;
    my ($mysql_account);
    my $mysql_pass = "";
    my $auto_mysql_account = "$account";
    my $auto_host = $LasConfig{db_host};
    if (!$auto_host){
        my $hn = `hostname`;
        chomp $hn;
        ($auto_host) = gethostbyname $hn;
        chomp $auto_host;
    }

    my $dbh;
    while (!$dbh){
        $mysql_host = getAnswer("Enter name of mysql host ", "$auto_host");
        $mysql_account = getAnswer($mess, $auto_mysql_account);
        system "stty -echo";
        $mysql_pass = getAnswer("Enter password", "");
        system "stty echo";
        $dbh = db_login($db, $mysql_host, $mysql_account, $mysql_pass);
    }
    $LasConfig{db_host} = $mysql_host;
    if ( $mysql_host eq "localhost" ) {
        my $hn = `hostname`;
        chomp $hn;
        ($auto_full_domain) = gethostbyname $hn;
        chomp $auto_full_domain;
       $mysql_full_domain = getAnswer("\nEnter the fully qualified domain name of the mysql host ", "$auto_full_domain");
    }
    else {
       $mysql_full_domain = $mysql_host;
    }
    return ($dbh, $mysql_host, $mysql_full_domain, $mysql_account, $mysql_pass);
}

sub db_execute {
    my ($dbh, $statement) = @_;
    eval {
        $dbh->do;
    }
}

#
# Check for MySQL driver
#
my @mysqls = grep(/mysql/, DBI->available_drivers);
if (scalar @mysqls == 0){
    print <<EOM;
You don't appear to have a driver installed for MySQL.
Have you installed MySQL? LAS now requires MySQL (a relational database).
You can obtain info on installing MySQL at www.mysql.com.
If you have installed MySQL, you may not have installed the required
Perl driver for MySQL. You may install this by downloading the DBD::mysql
module from CPAN (see www.perl.com/CPAN-local/ for instructions).
EOM
   exit 1;
}

my ($dbh, $host, $account, $password);

my $classesDir = "WebContent/WEB-INF/classes";
if ( ! -d $classesDir ) {
    mkdir $classesDir, 0755 or die "Can't create directory $classesDir: $!\n";
    print "Created a directory for the webapp classes and resources.\n\n";
}
if ( $wantUI ) {
print <<EOM;

I need to log in to the MySQL database. To do this, I have to connect to 
your MySQL server using a privileged MySQL account that allows me to create 
a new user account and/or edit the las database.
EOM

# Log in to mysql table
while (!$dbh){
    ($dbh, $host, $full_host, $account, $password) =
        db_prompted_login("DBI:mysql:mysql", "MySQL account name", "root");
                                # Make sure this account has privileges
    my $rows = $dbh->do(qq(select user,create_priv, drop_priv from user where user = '$account' AND create_priv = 'Y' AND drop_priv='Y'));
    if ($rows == 0){
        print "The '$account' user can't create and drop tables\n";
        $dbh->disconnect;
        $dbh = 0;
    }
}

## Write out the initial copy of the las.properties file.
## genLas.pl will read this and add some stuff it needs.
    open OUT,">$classesDir/las.properties" or die "Can't open $classesDir/las.properties'";
    print OUT "las.db.host=$full_host\n";
    print OUT "las.db.user=las\n";
    print OUT "las.db.password=lasrules\n";

# Attempt log in to las table
my $create_las = 0;
my $dbh_las = db_login("DBI:mysql:las", $host, $account, $password, 0);

#
# Create las database if it doesn't exist
#

if (!$dbh_las){
    print "Creating las database...\n";
    print "Looking for mysqladmin program...\n";
    my $admin = "";
    foreach my $loc qw(/usr/bin /usr/local/bin
                       /usr/mysql/bin /usr/local/mysql/bin){
        if (-x "$loc/mysqladmin"){
            $admin = "$loc/mysqladmin";
            last;
        }
    }
    if (!$admin){
        while(1){
            $admin = getAnswer("Location of mysqladmin", "");
            last if -x $admin;
            print "$admin not a prgram\n";
        }
    }

    my $ok;
    while (!$ok){
        $pstring = "--password=$password" if $password;
        my $status = `$admin -u $account $pstring -h $host create las`;
        if ($status){
            print "Received error message: $status\n";
        } else {
            $ok = 1;
        }
    }
    $dbh_las = db_login("DBI:mysql:las", $host, $account, $password, 0);
}


# Make sure sessions table exists
eval {
    $dbh_las->do(qq(select * from sessions where id = -1));
};
if ($@){
    print "Creating sessions table in database las...\n";
    $dbh_las->do(qq(CREATE TABLE sessions (id char(32) not null primary key, a_session text))) || die "Can't create sessions table in database las";
}

$dbh_las->disconnect;
    

#
# See if there is a LAS user account
#
my $rows = $dbh->do(qq(select * from user where user='las' and Host='$full_host')) ||
    die "Can't run select query on MySQL user table";
if ($rows < 1){
    print "\nCreating MySQL LAS user account on $full_host...\n";
    $dbh->do(qq(insert into user (Host, User, password, Select_priv, Insert_priv, Update_priv, Delete_priv, Create_priv) VALUES ('$full_host', 'las', password('lasrules'), 'Y', 'Y', 'Y', 'Y', 'Y'))) || die "Can't create LAS user account";
    $dbh->do(qq(flush privileges)) || die "Can't create LAS user account";
}

$dbh->disconnect if defined $dbh;
} #end of if $wantUI

print "\n\n";

#
# Set up any custom stuff
#
my $serverDir = "conf/server";
my $customDir = "conf/server/custom";
my $docsDir = "WebContent/docs";
if (! -d $docsDir){
       mkdir $docsDir, 0755 or die "Can't create directory $docsDir: $!\n";
       print "Created a directory for docs.\n\n";
}
if (! -d $customDir){
    if ( ! -d $serverDir ) {
       mkdir $serverDir, 0755 or die "Can't create directory $serverDir: $!\n";
       print "Created a directory for an example server.\n\n";
    }
    mkdir $customDir, 0755 or die "Can't create directory $customDir: $!\n";
    print "Created a directory for custom code.\n\n";
}
if (! -d "$customDir/WebContent"){
    mkdir "$customDir/WebContent", 0755 or die "Can't create directory $customDir/WebContent $!\n";
}

if (! -d "$customDir/WebContent/images"){
    mkdir "$customDir/WebContent/images", 0755 or die "Can't create directory $customDir/WebContent/images $!\n";
    print "Created a directory for custom images.\n\n";
}

# Set up DODS Configuration and Cache stuff
#

my $dodsConfDir = "conf/server/dods";
if (! -d $dodsConfDir){
   mkdir $dodsConfDir, 0775 or die "Can't create directory $dodsConfDir: $!\n";
}
my $dodsCacheDir = "$dodsConfDir/.dods_cache";
if (! -d $dodsCacheDir){
   mkdir $dodsCacheDir, 0775 or die "Can't create directory $dodsCacheDir: $!\n";
}

my $dodsConf = "$dodsConfDir/.dodsrc";
if (! -f $dodsConf){
   open DOUT, ">$dodsConf" or die "Can't open $dodsConf for writing";
   print DOUT "\# DODS client configuration file. See the DODS\n";
   print DOUT "\# users guide for information.\n";
   print DOUT "USE_CACHE=1\n";
   print DOUT "MAX_CACHE_SIZE=100\n";
   print DOUT "MAX_CACHED_OBJ=5\n";
   print DOUT "IGNORE_EXPIRES=0\n";
   print DOUT "CACHE_ROOT=$ENV{PWD}/$dodsCacheDir\n";
   print DOUT "DEFAULT_EXPIRES=86400\n";
   print DOUT "ALWAYS_VALIDATE=0\n";
   close DOUT;
}

my $dodsConf_no_cache = "$dodsConfDir/.dodsrc_no_cache";
if (! -f $dodsConf_no_cache){
   open DOUT, ">$dodsConf_no_cache" or die "Can't open $dodsConf_no_cache for writing";
   print DOUT "\# DODS client configuration file. See the DODS\n";
   print DOUT "\# users guide for information.  This version of the\n";
   print DOUT "\# file has caching turned off.\n";
   print DOUT "USE_CACHE=0\n";
   print DOUT "MAX_CACHE_SIZE=100\n";
   print DOUT "MAX_CACHED_OBJ=5\n";
   print DOUT "IGNORE_EXPIRES=0\n";
   print DOUT "CACHE_ROOT=$ENV{PWD}/$dodsCacheDir\n";
   print DOUT "DEFAULT_EXPIRES=86400\n";
   print DOUT "ALWAYS_VALIDATE=0\n";
   close DOUT;
}

#
# Get the hostname of the LAS server
#
my $hostname;
my $hn = `hostname`;
chomp $hn;
my $autohostname = $LasConfig{hostname};
if (! $autohostname){
    ($autohostname) = gethostbyname $hn;
    chomp $autohostname;
}
while (! $hostname){
    print "Enter the full domain name of the Web Server (including port number if non standard): [$autohostname] ";
    $hostname = <STDIN>;
    chomp $hostname;
    $hostname = $autohostname if ! $hostname;
    if ($hostname !~ /\./){
        print "You must enter a full domain name\n";
        $hostname = undef;
    }
}
$LasConfig{hostname} = $hostname;

my $autoproxy = $LasConfig{proxy};
if (! $autoproxy){
   $autoproxy = "yes";
}

while (! $proxy){
    print "Do you plan to use a proxy pass or connector from the HTTP server to the tomcat server (recommended; instructions below): [$autoproxy] ";
    $proxy = <STDIN>;
    chomp $proxy;
    $proxy = $autoproxy if ! $proxy;
    if ($proxy ne "yes" && $proxy ne "no") {
        print "You must answer 'yes' or 'no'.\n";
        $proxy = undef;
    }
}
$LasConfig{proxy} = $proxy;

#
# Check HTTP server
#

my $res;
$LasConfig{apache} = 1;
eval {
    require(LWP::UserAgent);
};
if ($@){
    print <<EOF;

You don't have the LWP::UserAgent module, so I can't verify that you
have a HTTP server that is compatible with LAS. I will assume that you
have an Apache HTTP server, and that is at least version 1.3.x

EOF
    print "Press any key to continue:";
    <STDIN>;
} else {
#
# Validate server version
#
    #print <<EOF;

#If the Web server is running, I can make sure that it is a server
#supported by LAS. 
#EOF
    #$res = getYesOrNo("Is the server running");
#}

#if ($res){
    #print "Checking server...\n";
    #my $url = new URI::URL("http://" . $hostname , "/");
    #my $req = new HTTP::Request(GET, $url);
    #my $ra = new LWP::UserAgent;
    #my $resp = $ra->request($req);
    #if ($resp->is_success){
        #my $server = $resp->headers->server;
        #my ($junk) = split(' ',$server);
        #my ($type, $version) = split('/',$junk);
        #if ($type =~ /Apache/){ # Bug 142
            #my ($major, $minor) = split('\.', $version);
            #if (($major == 1 && $minor >= 3) || ($major > 1)){
                #print "Good. You are running an Apache server.\n";
            #} else {
                #print "You are running Apache $version. LAS requires you to run ";
                #print "at least 1.3.1\n";
                #exit 1;
            #}
        #} else {
            #print "You are running an unsupported server:\n";
            #print "\t$server\n";
            #print "The script will continue, but LAS has not been tested on\n";
            #print "this server\n";
            #$LasConfig{apache} = 0;
        }
    #} else {
        #die $resp->message;
    #}
#}

my ($pathname, $autopathname);
#
# Get the path to access the LAS UI
#
$autopathname = $LasConfig{uipath};
$autopathname = "/las" if ! $autopathname;
if ($wantUI) {
print "\nYou must now specify the path name the Web client will use\n";
print "when accessing LAS. Unless you have more than one version of LAS\n";
print "installed, the default of $autopathname should be fine\n";
} else {
print "\nYou must now specify the path name the Web client will use\n";
print "when accessing your product server. Unless you have more than one \n";
print "version of LAS installed, the default of $autopathname should be fine\n";
}

while (! $pathname){
    print "Enter path name for LAS: [$autopathname] ";
    $pathname = <STDIN>;
    chomp($pathname);
    $pathname = $autopathname if ! $pathname;
    if ($pathname !~ /^\//){
        print "Path name must begin with a '/'\n";
        undef $pathname;
    }
    $pathname =~ s/[\/]+$//g;
    my $count = split('\/', $pathname);
    if ($count > 2){
        print "Path name can only be one level deep (i.e. /las ok, /las/foo not)\n";
        undef $pathname;
    }
}
$LasConfig{uipath} = $pathname;

if (!$wantUI) {
print "Your product server URL will be: \n";
print "http://".$LasConfig{tomcat_hostname}.":".$servlet_port.$pathname."/ProductServer.do\n";
}
undef $pathname;

my $appname = $LasConfig{uipath};
$appname =~ s/\///g;
$LasConfig{appname} = $appname;

createScripts();

#
# Get a title for the LAS
#
my $title;
my $autotitle = $LasConfig{title};
print "\nPlease provide a meaningful title for your LAS.\n";
print "This title will appear in the upper left hand corner of the LAS interface.\n";
print "Please use ONLY ALPHA-NUMERIC characters\n";
while (! $title){
    print "Enter a title for the LAS server: [$autotitle] ";
    $title = <STDIN>;
    chomp($title);
    if ($title =~ m/[^a-zA-Z0-9_\.-\s]/){
        print "Please use ONLY ALPHA-NUMERIC and .,-,_ characters\n";
        $title= '';
    }else{
        $title = $autotitle if ! $title;
    }
}
$LasConfig{title} = $title;

#
# Get an email address(es) for the LAS administrator(s)
#
my $email;
my $autoemail = $LasConfig{email};
print "\nProvide email address(es) for the administrator(s).\n";
print "The email address(es) will be used to configure a script that can automatically\n";
print "check to see if the UI server is responding and restart it if it has failed.\n";
print "An email will be send to the list of addresses if the server was restarted.\n";

    print "Enter a blank separated list email address(es): [$autoemail] ";
    $email = <STDIN>;
    chomp($email);
    $email = $autoemail if ! $email;

$LasConfig{email} = $email;

my $servlet_root_url="";
if ($LasConfig{proxy} eq "yes") {
    $servlet_root_url = $LasConfig{hostname};
} else {
    $servlet_root_url = $LasConfig{tomcat_hostname} . ":" . $servlet_port;
}
# Get info about the TDS installation.
#
# Get the temp dir.
#
my $tds_temp;
my $autotds_temp = $LasConfig{tds_temp};
$autotds_temp = $ENV{PWD}."/conf/server/temp" if ! $autotds_temp;

print "\nThe TDS you installed will be used by LAS to regrid data as needed to make comparisons.\n";
print "During this process the Ferret IOSP will write lots of temporary files.\n";

    print "Enter a directory path for F-TDS temporary files: [$autotds_temp] ";
    $tds_temp = <STDIN>;
    chomp($tds_temp);
    $tds_temp = $autotds_temp if ! $tds_temp;

if ( !(-d $tds_temp) ) {
   &File::Path::mkpath($tds_temp);
   print "Creating the $tds_temp directory.\n";
}

$LasConfig{tds_temp} = $tds_temp;

# Get the data dir.
my $autotds_data = $LasConfig{tds_data};
$autotds_data = $ENV{PWD}."/conf/server/data" if ! $autotds_data;

print "\nIn the installation installation instructions you were asked to configure TDS\n";
print "with a datascan directory to be used by LAS.\n";

    print "Enter the directory path you configured for LAS F-TDS data files: [$autotds_data] ";
    $tds_data = <STDIN>;
    chomp($tds_data);
    $tds_data = $autotds_data if ! $tds_data;

if ( !(-d $tds_data) ) {
   &File::Path::mkpath($tds_data);
   print "Creating the $tds_data directory.\n";
}
   $LasConfig{tds_data} = $tds_data;

# Make the dynamic data dir.
my $tds_dynadata = $tds_data."/dynamic";

if ( !(-d $tds_dynadata) ) {
   &File::Path::mkpath($tds_dynadata);
   print "\nCreating the $tds_dynadata directory for the dynamic data for user defined variables and comparison regridding.\n";
}
   $LasConfig{tds_dynadata} = $tds_dynadata;

# End of TDS info.
print "\nEditing scripts...\n";
foreach my $script (@Scripts){
    my $template = "$script.in";
    open INSCRIPT, $template or die "Can't open template file $template";
    if (-f $script){
        mychmod 0755, $script;
    }
    open OUTSCRIPT, ">$script" or die "Can't create output file $script";
    my $cust_name = $LasConfig{custom_name};
    my $cname;
    $cname = qq(src="$cust_name/custom.js") if $cust_name;
    my $cplinclude = "";
    $cplinclude = "$cust_name" if $cust_name;
    my $java_home = dirname(dirname($java));
    while (<INSCRIPT>){
        s/\@PERL\@/$PERLBIN/g;
        s/\@FULLPATH\@/$LasConfig{fullpath}/g;
        s/\@JAKARTA_HOME\@/$jakarta_home/g;
        s/\@JAVA_HOME\@/$java_home/g;
        s/\@FERRET\@/$LasConfig{ferret}/g;
        s/\@OUTPUT_ALIAS\@/$LasConfig{output_alias}/g;
        s/\@UIPATH\@/$LasConfig{uipath}/g;
        s/\@APPNAME\@/$LasConfig{appname}/g;
        s/\@SERVERHOST\@/$LasConfig{hostname}/g;
        s/\@TOMCATHOST\@/$LasConfig{tomcat_hostname}/g;
        s/\@PROXY\@/$LasConfig{proxy}/g;
        s/\@JSINCLUDE\@/$cname/g;
        s/\@CUSTOM_PERL_INCLUDE\@/$cplinclude/g;
        s/\@DB_HOST\@/$host/g;
        s/\@SERVLET_PORT\@/$servlet_port/g;
        s/\@SERVLET_SHUTDOWN_PORT\@/$servlet_shutdown_port/g;
        s/\@SERVLET_CONNECTOR_PORT\@/$servlet_connector_port/g;
        s/\@SERVLET_ROOT_URL\@/$servlet_root_url/g;
        s/\@MODULES_LIST\@/$modules_list/g;
        s/\@TITLE\@/$LasConfig{title}/g;
        s/\@ADMIN_EMAIL\@/$LasConfig{email}/g;
        s/\@WEBAPPS\@/$LasConfig{webapps}/g;
        s/\@TDS_DATA\@/$LasConfig{tds_data}/g;
        s/\@TDS_DYNADATA\@/$LasConfig{tds_dynadata}/g;
        s/\@TDS_TEMP\@/$LasConfig{tds_temp}/g;
        s/\@PWD\@/$ENV{PWD}/g;
        print OUTSCRIPT $_;
    }
    close INSCRIPT;
    close OUTSCRIPT;
    mychmod 0755, $script;
}


#
# Set up Ferret paths
#

$ferretConfig = "JavaSource/resources/ferret/FerretBackendConfig.xml";

print <<EOF;

Now setting up the Ferret environment variables for the server...
If you want to change them, edit 'JavaSource/resources/ferret/FerretBackendConfig.xml'


EOF

if (-f "JavaSource/resources/ferret/FerretBackendConfig.xml"){
    print "You already have a config file for your Ferret backend environment.\n";
    

    if ( getYesOrNo("Do you want to use this file") ) {
    } else {
print <<EOF;

Creating a  new 'JavaSource/resources/ferret/FerretBackendConfig.xml'
based on your current environment variable settings.  

The current file has been saved in: JavaSource/resources/ferret/FerretBackendConfig.xml.old

EOF
       system("mv $ferretConfig $ferretConfig.old");
       copy ("$ferretConfig.base","$ferretConfig") or
       die "Could not get FerretBackendConfig.xml initialization file";
       printENV($ferretConfig, @EnvVars);
    }
} else {
       copy("$ferretConfig.base","$ferretConfig") or
       die "Could not get FerretBackendConfig.xml initialization file";
       printENV($ferretConfig, @EnvVars);
}

# Make the old Ferret_Config.pl file because genLas.pl needs it.
Ferret_Config(@EnvVars);

# Always get a fresh a copy of LAS_config.pl from the examples.
    copy("conf/example/LAS_config.pl","conf/server/LAS_config.pl") or
         die "Cannot get copy of LAS_config.pl from conf/example.";

# Get a copy of operations.xml from the examples.
if (! -f "conf/server/operations.xml") {
    copy("conf/example/operations.xml","conf/server/operations.xml") or
         die "Cannot get copy of operations.xml from conf/example.";
}

# Get a copy of insitu_operations.xml from the examples.
if (! -f "conf/server/insitu_operations.xml") {
    copy("conf/example/insitu_operations.xml","conf/server/insitu_operations.xml") or
         die "Cannot get copy of insitu_operations.xml from conf/example.";
}

# Get a copy of options.xml from the examples.
if (! -f "conf/server/options.xml") {
    copy("conf/example/options.xml","conf/server/options.xml") or
         die "Cannot get copy of options.xml from conf/example.";
}

# Get a copy of insitu_options.xml from the examples.
if (! -f "conf/server/insitu_options.xml") {
    copy("conf/example/insitu_options.xml","conf/server/insitu_options.xml") or
         die "Cannot get copy of insitu_options.xml from conf/example.";
}

# Get a copy of browsers.xml from the examples.
if (! -f "conf/server/browsers.xml") {
    copy("conf/example/browsers.xml","conf/server/browsers.xml") or
         die "Cannot get copy of browsers.xml from conf/example.";
}

system("mkdir -p WebContent/classes/gifs");
my @gifs = glob("WebContent/luis/applet_gifs/*.gif");
chomp(@gifs);
foreach my $file (@gifs) {
   $target = basename($file);
   copy("$file","WebContent/classes/gifs/$target") or
       die "Cannot get applet gifs from WebContent/luis/applet_gifs.";
}
copy("WebContent/luis/livemap/LiveMap_30.jar","WebContent/classes/LiveMap_30.jar") or
    die "Cannot copy map applet from WebContent/luis/livemap.";

#
# See if the user wants to use existing XML in server/las.xml
#
my $checkForSample = 1;
if ( $wantUI ) {
if (-f 'conf/server/las.xml'){
    $checkForSample = 0;
    print "\nYou have an existing XML file in server/las.xml\n";
    if (getYesOrNo("Do you want to set up the server to use this file")){
        &checkOperationsURL;
        &genHTML;
    } else {
        print <<EOF;

OK. If you have changed the XML file since last running configure, the
server might not work.

EOF
    }
}
} else {
   if ( -f 'conf/server/las.xml' ) {
      $checkForSample=0;
   }
}

#
# See if user wants to set up sample XML/HTML
#

if ($checkForSample){
    print <<EOF;

You can set the server up to use the sample COADS climatology and Levitus 
climatology datasets that are distributed with Ferret. 
EOF
    if (getYesOrNo("Do you want to do this")){
        &genSamples;
        if ( $wantUI ) {
           &genHTML;
        }
    } else {
        print <<EOF;

OK. Unless you have already configured a XML file in server/las.xml
and generated JavaScript and HTML from it, the server will not function
correctly.

EOF
    }
}

#
# Create properties file for ant
#
my $antprops = "WebContent/WEB-INF/classes/ant.properties";
open ANTPROPS, ">$antprops" or die "Can't write to file $antprops";
my $appname = $LasConfig{uipath};
$appname =~ s/\///g;
print ANTPROPS "app.name=$appname\n";
close ANTPROPS;

#
# Check for las.war file
#
my $target = "dist";
if (!$wantUI) {
   $target="lps-lbes";
}

if (! -f $LasConfig{appname}.".war"){
    print "Building servlet war file.\n";
    system("ant $target");
}
if ($wantUI && ! -f $LasConfig{appname}.".war"){
    print "Can't find servlet WAR file.\n";
    print "Try building it yourself by running these commands.\n";
    print "ant dist\n";
    exit 1;
} elsif ( !$wantUI && ! -f $LasConfig{appname}.".war") {
    print "Can't find servlet WAR file.\n";
    print "Try building it yourself by running these commands.\n";
    print "ant lps-lbes\n";
    exit 1;
}
$target = "deploy";
if (!$wantUI) {
   $target="lps-lbes-deploy";
}

system("ant $target");

#
# Change directory protections
#

print <<EOF;

I need to know the group your Web server runs as. This is needed
so that the Web server can write log files and create output images and data.
EOF

my $auto_group = $LasConfig{group};
$auto_group = 'nobody' if ! $auto_group;
print "Group name? [$auto_group] ";
my $group = <STDIN>;
chomp($group);
$group = "$auto_group" if ! $group;
$LasConfig{group} = $group;

print "\nConfiguration completed succesfully\n";

#
# Save configuration
#

if (!&saveConfig){
    print <<EOF;

Couldn't save configuration results. The next time you run the
configuration script, you will have to reenter all of the
configuration data.

EOF
}

#
# Print out startup information to the terminal
#

my $path = $LasConfig{pathname} . '/';
my $realcgipath = $ENV{PWD} . '/server/';
my $outputpath = $LasConfig{output_alias} . '/';
my $realoutputpath = $ENV{PWD} . '/server/output/';
    print <<EOF;

To get started, you need to start (or restart) your Tomcat server.

You can use the startserver.sh or the rebootserver.sh scripts provided to
start or restart Tomcat.

If you have your own scripts for managing your Tomcat please note that to run LAS you
need the following options set for Tomcat:

JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"

If you have ample memory you can increase the size allocated to Tomcat by increasing
the -Xmx256M and -Xms256M to -Xmx512M and -Xms512M or larger.

EOF

if ($LasConfig{proxy} eq "yes") {

  print <<EOF;

To use the proxy module to hide the Tomcat port in the URLs used to connect
to your LAS User Interface and your FDS installation, add the following to your
Apache configuration file (/etc/httpd/conf/httpd.conf):

    ProxyPass $LasConfig{uipath}/ http://$LasConfig{tomcat_hostname}:$LasConfig{servlet_port}$LasConfig{uipath}/
    ProxyPassReverse $LasConfig{uipath}/ http://$LasConfig{tomcat_hostname}:$LasConfig{servlet_port}$LasConfig{uipath}/

EOF
}     


print <<EOF;

You can activate the scripts to check and automatically restart the Tomcat UI server
by running the script:

$ENV{PWD}/bin/initialize_check.sh

once after you have started your Java servlet server.  You also need to a line similar
to this in your crontab:

55 0-23 * * * $ENV{PWD}/bin/las_ui_check.sh

EOF

#
# Generate sample XML/HTML
#

sub genSamples {

    my @sample_in = ();
    my @sample_out = ();

    my @insitu_in = ();
    my @insitu_out = ();

    $sample_in[0] = "conf/example/sample_las.xml";
    $sample_out[0] = "conf/server/las.xml";
    $sample_in[1] = "conf/example/productserver.xml";
    $sample_out[1] = "conf/server/productserver.xml";
    $sample_in[2] = "conf/example/operationsV7.xml";
    $sample_out[2] = "conf/server/operationsV7.xml";
    $sample_in[3] = "conf/example/sample_ui.xml";
    $sample_out[3]= "conf/server/ui.xml";
    $sample_in[4] = "xml/perl/coads.xml";
    $sample_out[4] = "conf/server/coads.xml";
    $sample_in[5] = "conf/example/DODS_IRI_NOAA_NCEP_EMC_CMB_Pac_ocean.xml";
    $sample_out[5] = "conf/server/DODS_IRI_NOAA_NCEP_EMC_CMB_Pac_ocean.xml";
    $sample_in[6] = "xml/perl/levitus.xml";
    $sample_out[6] = "conf/server/levitus.xml";
    $insitu_in[0] = "conf/example/insitu_demo_1.xml";
    $insitu_out[0] = "conf/server/insitu_demo_1.xml";
    $insitu_in[1] = "conf/example/insitu_demo_2.xml";
    $insitu_out[1] = "conf/server/insitu_demo_2.xml";
    $insitu_in[2] = "conf/example/insitu_demo_ui.xml";
    $insitu_out[2] = "conf/server/insitu_demo_ui.xml";
    $insitu_in[3] = "conf/example/insitu_ui.xml";
    $insitu_out[3] = "conf/server/insitu_ui.xml";
    $insitu_in[4] = "conf/example/insitu_options.xml";
    $insitu_out[4] = "conf/server/insitu_options.xml";

    # Overwrite gridded only las.xml and ui.xml for insitu demo.
    $insitu_in[5] = "conf/example/sample_insitu_las.xml";
    $insitu_out[5] = "conf/server/las.xml";
    $insitu_in[6] = "conf/example/sample_insitu_ui.xml";
    $insitu_out[6] = "conf/server/ui.xml";
    $insitu_in[7] = "conf/example/nwioos_hake98.xml";
    $insitu_out[7] = "conf/server/nwioos_hake98.xml";
    $insitu_in[8] = "conf/example/pfeg.xml";
    $insitu_out[8] = "conf/server/pfeg.xml";


    my $insitu = 0;
    if ( $wantUI) {
       if (getYesOrNo("Do you want set up the sample in-situ datasets")) {
          system("mysql -u $account -h $host -p$password < data/insitu/LAS_insitu_demo_1.sql");
          system("mysql -u $account -h $host -p$password < data/insitu/LAS_insitu_demo_2.sql");
          $insitu=1;
       }
    }

    for ( my $i = 0; $i <= $#sample_in; $i++ ) {
       if ( -f $sample_out[$i] ) {
           print "You already have this XML configuration file for your\n";
           print "product server in $sample_out[$i].\n";
           if (! getYesOrNo("Overwrite this file", 1)){
              print "I will not generate a sample configuration\n";
              return;
          }
       }
       if (-f $sample_out[$i] && !unlink $sample_out[$i]){
           print "Couldn't delete $sample_out[$i]\n"; return;
       }
       if (!copy($sample_in[$i], $sample_out[$i])){
           print "Couldn't copy $sample_in[$i] to $sample_out[$i]\n";
           return;
       }
    }
    if (!copy("WebContent/luis/web/levitus_monthly.html","WebContent/docs/levitus_monthly.html")){
       print "Couldn't copy levitus_monthly.html.\n";
    }
    if ($insitu) {
       for ( my $i = 0; $i <= $#insitu_in; $i++ ) {
          if ( -f $insitu_out[$i] && 
                 !$insitu_out[$i] =~ "/las.xml" &&
                 !$insitu_out[$i] =~ "/ui.xml" ) {
              print "You already have this XML configuration file for your\n";
              print "product server in $insitu_out[$i].\n";
              if (! getYesOrNo("Overwrite this file", 1)){
                 print "I will not generate a sample configuration\n";
                 return;
             }
          }
          if (-f $insitu_out[$i] && !unlink $insitu_out[$i]){
              print "Couldn't delete $insitu_out[$i]\n"; return;
          }
          if (!copy($insitu_in[$i], $insitu_out[$i])){
              print "Couldn't copy $insitu_in[$i] to $insitu_out[$i]\n";
              return;
          }
       }
    }
}

sub genHTML {
    print "Generating HTML... \n";
    if (system(qq{cd conf/server; ../../xml/perl/genLas.pl -s -u "$account" -p "$password" -h "$host" las.xml})){
        die "Error in generating HTML.\n";
    }
}

sub createScripts {

my $jakarta_home = $LasConfig{jakarta_home};
my $java_home = dirname(dirname($java));
my $workdir = $LasConfig{uipath};
my $removeWork = $jakarta_home."/work/Catalina/localhost".$workdir;

open SCRIPT_OUT, ">startserver.sh" or die "Can't open startserver.sh";
print SCRIPT_OUT <<EOL;
#!/bin/sh
JAVA_HOME="$java_home"
JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"
CATALINA_PID="$LasConfig{webapps}/UI_PID"
CATALINA_HOME="$LasConfig{jakarta_home}"
export JAVA_HOME JAVA_OPTS CLASSPATH CATALINA_PID CATALINA_HOME
rm -rf $removeWork
exec $jakarta_home/bin/catalina.sh start
EOL

close SCRIPT_OUT;
mychmod 0755,"startserver.sh";

open SCRIPT_OUT, ">stopserver.sh" or die "Can't open stopserver.sh";
print SCRIPT_OUT <<EOL2;
#!/bin/sh
JAVA_HOME="$java_home"
JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"
CATALINA_PID="$LasConfig{webapps}/UI_PID"
CATALINA_HOME="$LasConfig{jakarta_home}"
export JAVA_HOME JAVA_OPTS CLASSPATH CATALINA_PID CATALINA_HOME
exec $jakarta_home/bin/catalina.sh stop
EOL2

close SCRIPT_OUT;
mychmod 0755,"stopserver.sh";

open SCRIPT_OUT, ">rebootserver.sh" or die "Can't open rebootserver.sh";
print SCRIPT_OUT <<EOL3;
#! /bin/sh
#

# Attempt to shutdown the server gracefully.
$ENV{PWD}/stopserver.sh

# Wait to let things shutdown.
sleep 5

# Check to see if it is still running.
ps `cat $LasConfig{webapps}/UI_PID`
STATUS=$?
 if [ \$STATUS -eq 0 ] ; then
     # LAS UI did not shutdown ok.  Kill it.
     kill -9 `cat $LasConfig{webapps}/UI_PID`
 else
     # LAS UI shutdown ok.
     continue
 fi

# Start the server again.  Answer the question 'yes' if necessary.
$ENV{PWD}/startserver.sh
EOL3

close SCRIPT_OUT;
mychmod 0755,"rebootserver.sh";
}

#
# Check that the operations URL in las.xml matches $LasConfig{fullpath}
#
sub checkOperationsURL {
print "\nChecking operations URL...\n";
  my $existing_fullpath = "";
  open CONFIG_FILE, "< server/las.xml";
  while (<CONFIG_FILE>) {
    if (/LASserver.pl/) {
      $_ =~ /url=\"(.*)\"/;
      $existing_fullpath = $1;
      if ($existing_fullpath eq $LasConfig{fullpath}) {  
        return;
      } else {
        print "\nThe existing las.xml file has an operations URL that points to another LAS:\n";
        print "\t$existing_fullpath\n\n";
        print "The operations URL you just specified is:\n";
        print "\t$LasConfig{fullpath}\n\n";
        print "Please fix this discrepancy and then reconfigure.\n\n";
        print "CONFIGURATION FAILED.\n\n";
        exit 1;
      }
    }
  }
}


BEGIN {
    use Config;       # perl %Config hash
                      # On one RedHat 9 system this 'use' caused
                      # problems when it appeared in the lines above.
                      # $Config{perlpath} returned long lines with
                      # embedded spaces and quotes.

    my ($major,$minor, $minorminor) = split('\.', $Config{version});
    die "Need at least Perl version 5.6.0"
        if ($major < 5 || ($major == 5 && ! defined($minorminor)));

    sub getAnswer($$) {
        my ($mess, $default) = @_;
        print "$mess: [$default] ";
        my $answer = <STDIN>;
        chomp($answer);
        $answer = $default if ! $answer;
        return $answer;
    }

    sub getYesOrNo {
        my $prompt = shift;
        my $useNo = shift;
        my $default = $useNo ? "no" : "yes";
        print "$prompt? [$default] ";
        my $ans = <STDIN>;
        chomp($ans);
        $ans = $default if ! $ans;
        if ($ans !~ /^[yY]/){
            return 0;
        }
        return 1;
    }    

    sub installModule {
        my ($module, $lasref, $user, $pass, $host) = @_;
        my @LAS_INC = @{$lasref};

        my @comm = ($Config{perlpath});
        foreach (@LAS_INC){
            push(@comm, "-I$_");
        }
        my $perlcomm = join(" ", @comm);
        my $cwd = getcwd;
        my $root = $cwd . "/xml/perl";
        my $prefix = $Config{prefix};
        $prefix = $root . "/install" if scalar(grep(/$root/, @LAS_INC)) > 0;


        my $modRoot = $root . "/modules/" . $module;

        if (!chdir($modRoot)){
            warn "Can't change dir to: $modRoot";
            return;
        }

# Date-Calc-5.4 requires "LANG=en_US" instead of "LANG=en_US.UTF-8" as found on RH9.
        $ENV{LANG} = "en_US";
      
        my $comm;
        if ($module =~ /^netcdf-perl/){
          # Find netCDF libraries
            my @locs = qw(/usr/local /usr /opt/local /opt);
            my $found = 0;
            my $path;
            foreach my $loc (@locs){
                if (-f "$loc/lib/libnetcdf.a" && -f "$loc/include/netcdf.h"){
                    $found = 1;
                    $path = $loc;
                    last;
                }
            }
            if (!$found){
                print "\nCan't find netCDF libraries\n";
                exit 1;
            }
            $comm = qq{(CPP_NETCDF="-I$path/include";};
            $comm .= qq{LD_NETCDF="-L$path/lib -lnetcdf";};
            $comm .= qq{PERL="$perlcomm";};
            $comm .= qq{PERL_MANDIR="$prefix/man";};
            $comm .= qq{export CPP_NETCDF LD_NETCDF PERL PERL_MANDIR LANG;};
            my $makecomm = qq{Makefile.PL } .
                           qq{ FULLPERL="$perlcomm"} .
                               qq{ PREFIX="$prefix"};
            $comm .= qq{./configure && (cd perl && \$PERL $makecomm POLLUTE="1" && make && make test && make install)) 2>&1};
        } else {
            my $gencomm = "make clean; $perlcomm Makefile.PL" . qq{ FULLPERL="$perlcomm"} .
                qq{ PREFIX="$prefix"};
            if ($module =~ /^DBD-mysql/){
                $gencomm .= qq{ --testuser="$user" --testhost="$host"};
                $gencomm .= qq{ --testpassword="$pass"} if $pass;
            }
            $comm = "($gencomm && make && make test && make install)";
        }
        if (!open(STATUS, "$comm 2>&1|")){
            warn "Can't execute Perl command: $comm";
            chdir($cwd) or die "Can't change back to dir: $cwd";
            return;
        }
        while (<STATUS>){
            print;
        }

        chdir($cwd) or die "Can't change back to dir: $cwd";

    }

    if (dirname($0) ne "."){
        print "configure script must be run as ./configure\n";
        exit 1;
    }
#
# Make sure all modules installed
#
    my @modules = qw(
                   Bit::Vector
                   CGI
                   Compress::Zlib
                   DBD::mysql
                   DBI
                   Data::ShowTable
                   Date::Calc
                   Date::Manip
                   Digest::MD5
                   File::PathConvert
                   HTML::Parser
                   HTML::Tagset
                   LWP::UserAgent
                   Log::Agent
                   MD5
                   MIME::Base64
                   Mail::Sendmail
                   Parse::Lex
                   Parse::Yapp
                   Template
                   Test::More
                   Time::HiRes
                   URI::URL
                   XML::DOM
                   XML::Parser
                   XML::Parser::PerlSAX
                   XML::RegExp
                   );

    $modules_list = join("\n",@modules);
    my %modulesHash = (
                   Bit::Vector => 'Bit-Vector-6.4',
                   CGI => 'CGI.pm-3.05',
                   Compress::Zlib => 'Compress-Zlib-1.34',
                   DBD::mysql => 'DBD-mysql-2.9004',
                   DBI => 'DBI-1.47',
                   Data::ShowTable => 'Data-ShowTable-3.3',
                   Date::Calc => 'Date-Calc-5.4',
                   Date::Manip => 'DateManip-5.42',
                   Digest::MD5 => 'Digest-MD5-2.20',
                   File::PathConvert => 'File-PathConvert-0.9',
                   HTML::Parser => 'HTML-Parser-3.26',
                   HTML::Tagset => 'HTML-Tagset-3.03',
                   LWP::UserAgent => 'libwww-perl-5.65',
                   Log::Agent => 'Log-Agent-0.301',
                   MD5 => 'MD5-2.03',
                   MIME::Base64 => 'MIME-Base64-3.05',
                   Mail::Sendmail => 'Mail-Sendmail-0.79',
                   Parse::Lex => 'ParseLex-2.15',
                   Parse::Yapp => 'Parse-Yapp-1.05',
                   Template => 'Template-Toolkit-2.14',
                   Test::More => 'Test-Simple-0.44',
                   Time::HiRes => 'Time-HiRes-1.66',
                   URI::URL => 'URI-1.19',
                   XML::DOM => 'XML-DOM-1.39',
                   XML::Parser => 'XML-Parser-2.34',
                   XML::Parser::PerlSAX => 'libxml-perl-0.07',
                   XML::RegExp => 'XML-RegExp-0.03',
                   );

    sub searchModules {
        my @LAS_INC = @_;
        my @notFound = ();
        print "Searching for required Perl modules...\n";
        foreach my $module (@modules) {
            my $mod = $module;
            $mod =~ s/::/\//g;
            my $found = 0;
            foreach my $prefix (@LAS_INC){
                my $name = "$prefix/$mod.pm";
                if (-f "$name"){

                    if ($module eq 'Date::Calc') {

# In LAS 6.5 we  require version 5.4 for support for years<1000

                      require('Date/Calc.pm');
                      if ($Date::Calc::VERSION >= 5.4) {
                        $found = 1;
                        last;
                      }

                    } elsif ($module eq 'DBI') {

# Not sure what problems we had with ancient versions of DBI.

                      require('DBI.pm');
                      if ($DBI::VERSION >= 1.06) {
                        $found = 1;
                        last;
                      }

                    } elsif ($module eq 'Template') {

# Older versions of Template-Toolkit (not sure which ones) don't flush
# properly and cause LAS to return a broken image link even though the
# image is created.

                      require('Template.pm');
                      if ($Template::VERSION >= 2.00) {
                        $found = 1;
                        last;
                      }

                    } else {

                      $found = 1;
                      last;
                    }
                }
            }
            push(@notFound, $module) if ! $found;
        }
        return @notFound;
    }
#
# Automatically build any missing modules
#
    my $root = getcwd;
    $root .= "/xml/perl/install/lib";
    my $version = $Config{version};
    my $arch = $Config{archname};
    my @LAS_INC = ("$version/$arch","$version",
                   "site_perl/$version/$arch","site_perl/$version",
                   "site_perl");
    @LAS_INC = map {("$root/$_","$root/perl5/$_")} @LAS_INC;

    my @notFound = searchModules(@LAS_INC,@INC);
    if (scalar @notFound > 0){
        print "\nThe following Perl modules haven't been installed:\n";
        foreach (@notFound){
            print "\t$_\n";
        }

        my $ans = getYesOrNo("Do you want to build these modules (this can take up to thirty minutes)");
        exit 1 if ! $ans;

        print "\nYou can install these modules as:\n";
        print "1) part of the Perl distribution\n";
        print "2) part of the LAS distribution\n";
        print "If you choose 1), all Perl applications\n";
        print "(including other LAS servers) will have access to the modules.\n";
        print "If you choose 2), the modules will only be accessible from\n";
        print "this LAS server. \n";
        my $installType = "";
        while(1){
            $installType = getAnswer("Choose installation type", 1);
            my $cantWrite = "";
            if ($installType == 1){
                my $i=0;
                for (; $i < 3; $i++){
                    if (! -w $INC[$i]){
                        $cantWrite = $INC[$i];
                        last;
                    }
                }
            }
            if ($cantWrite){
                print "\nThis process doesn't have write access to the Perl\n";
                print "installation directories. You can install the modules\n";
                print "as part of the LAS distribution, or run the installation\n";
                print "under a user id that has write access to the Perl\n";
                print "installtion directories\n";
                exit 1;
            }
            last if $installType == 1 || $installType == 2;
            print "Must choose '1' or '2'\n";
        }

        @INSTALL_INC = @LAS_INC;
        @INSTALL_INC = @INC if $installType == 1;

        my ($user, $pass, $host);
        if (scalar grep(/DBD::mysql/, @notFound)){
            print "\nWe'll need a user name, host and password to test\n";
            print "installation of the MySQL DBD::mysql driver.\n";
            print "Looking for mysqladmin program...\n";
            my $admin = "";
            foreach my $loc qw(/usr/bin /usr/local/bin /usr/mysql/bin
                               /usr/local/mysql/bin){
                if (-x "$loc/mysqladmin"){
                    $admin = "$loc/mysqladmin";
                    last;
                }
            }
            if (!$admin){
                while(1){
                    $admin = getAnswer("Location of mysqladmin", "");
                    last if -x $admin;
                    print "$admin not a prgram\n";
                }
            }
                    
            while(1){
                $user = getAnswer("MySQL user name", "root");
                system "stty -echo";
                $pass = getAnswer("MySQL password", "");
                system "stty echo";
                print "\n";
                $host = getAnswer("MySQL host", "localhost");
                my @status = `$admin -h$host -u$user --password=$pass ping 2>&1`;
                last if ($status[0] !~ /failed/);
                print "Couldn't connect to server -- error was:\n\t$status[1]\n";
            }
        }

        print "\nNow building and installing Perl modules...\n";
        foreach my $mod (@notFound){
            print "Building and installing ", $modulesHash{$mod}, "...\n";
            installModule($modulesHash{$mod}, \@INSTALL_INC, $user, $pass, $host);
        }
        @notFound = searchModules(@LAS_INC,@INC);
        if (scalar @notFound > 0){
            print "\nInstallation of following modules failed:\n";
            foreach my $mod (@notFound){
                print $modulesHash{$mod}, "\n";
            }
            print "\nYou need to install these modules by hand\n";
            exit 1;
        }
        print "\n";
    }
    
    
    @INC = (@LAS_INC,@INC);
}
sub trim($)
{
        my $string = shift;
        $string =~ s/^\s+//;
        $string =~ s/\s+$//;
        return $string;
}
sub printENV($ferretConfig, @EnvVars) {
       open CONFIGFILE, ">>$ferretConfig"
           or die "Couldn't open config file $ferretConfig";

        print CONFIGFILE '    <environment>',"\n";
        foreach my $var (@EnvVars){
            $ENV{$var} = ". " . $ENV{$var} if $var !~ /PLOTFONT/;
            if ($var =~ /FER_GO|FER_PALETTE/){
                $ENV{$var} = "scripts jnls jnls/insitu jnls/section " . $ENV{$var};
                $ENV{$var} = $LasConfig{custom_name} . " " . $ENV{$var}
                    if $LasConfig{custom_name};
            } elsif ($var =~ /FER_DATA/){
                $ENV{$var} = "./data " . $ENV{$var};
            } elsif ($var =~ /FER_DESCR/){
                $ENV{$var} = "des " . $ENV{$var};
            } elsif ($var =~ /DODS_CONF/){
                $ENV{$var} = "dods/.dodsrc";
            }

            my @values = split(' ',$ENV{$var});

            # Trim so ". " and "." match
            foreach my $value (@values) {
               $value = trim($value);
            }

            #Extract unique entries, see perl FAQ
            undef %saw;
            @saw{@values} = ();
            @out = sort keys %saw;  # remove sort if undesired
    
            print CONFIGFILE '        <variable>',"\n";
            print CONFIGFILE '            <name>',$var,'</name>',"\n";
            foreach my $value (@out) {
               $value = trim($value);
               print CONFIGFILE '             <value>',$value,'</value>',"\n";
            }
            print CONFIGFILE '        </variable>',"\n";
        }
        print CONFIGFILE '    </environment>',"\n";
        print CONFIGFILE '</ferret>',"\n";
        close CONFIGFILE;
     }

sub Ferret_Config (@EnvVars) {

my $ferretConfig = "conf/server/Ferret_config.pl";
print <<EOF;

Now setting up the Ferret environment for the server...
I will use settings in your current Ferret environment. If you want to change
them, edit 'server/Ferret_config.pl'.
EOF

open CONFIGFILE, ">>$ferretConfig"
    or die "Couldn't open config file $ferretConfig";

foreach my $var (@EnvVars){
    $ENV{$var} = ". " . $ENV{$var} if $var !~ /PLOTFONT/;
    if ($var =~ /FER_GO|FER_PALETTE/){
        $ENV{$var} = "jnls jnls/insitu jnls/section " . $ENV{$var};
        $ENV{$var} = $LasConfig{custom_name} . " " . $ENV{$var}
            if $LasConfig{custom_name};
    } elsif ($var =~ /FER_DATA/){
        $ENV{$var} = "./data " . $ENV{$var};
    } elsif ($var =~ /FER_DESCR/){
        $ENV{$var} = "des " . $ENV{$var};
    } elsif ($var =~ /DODS_CONF/){
        $ENV{$var} = "dods/.dodsrc";
    }
    print CONFIGFILE '$ENV{',$var,'} = "',$ENV{$var},'";',"\n";
}
print CONFIGFILE "1;\n";
close CONFIGFILE;
mychmod 0755, $ferretConfig;
}
