package OrthoMCLService::Service::CgiApp::MsaOrthoMCL;
@ISA = qw( EbrcWebsiteCommon::View::CgiApp );

use strict;
use Tie::IxHash;
use EbrcWebsiteCommon::View::CgiApp;
use Data::Dumper;
use Bio::Seq;
use LWP::UserAgent;
use HTTP::Request::Common;
use HTTP::Request::Common qw(POST);

use File::Temp qw/ tempfile /;



sub run {
  my ($self, $cgi) = @_;

  my $dbh = $self->getQueryHandle($cgi);
  my $GUS_HOME = $ENV{'GUS_HOME'};
  print $cgi->header('text/html');

  my @ids = $cgi->param('msa_full_ids');
  my $ids = join(',', map { "'$_'" } @ids);

  my $sql = <<EOSQL;
SELECT secondary_identifier AS full_id, sequence
FROM dots.ExternalAaSequence
WHERE secondary_identifier in ($ids)
EOSQL

  my $sth = $dbh->prepare($sql);

  my ($infh, $infile)  = tempfile();
  $sth->execute();
  while(my ($id, $seq) = $sth->fetchrow_array()) {
    print $infh ">$id\n$seq\n";
  }
  close $infh;

  my ($outfh, $outfile) = tempfile();
  my ($dndfh, $dndfile) = tempfile();
  my ($tmpfh, $tmpfile) = tempfile();

  my $userOutFormat = $cgi->param('clustalOutFormat');
  if ((! defined $userOutFormat) || ($userOutFormat eq "")){
      $userOutFormat = "clu";
  }

  my $cmd = "clustalo -v --residuenumber --infile=$infile --outfile=$outfile --outfmt=$userOutFormat --output-order=tree-order --guidetree-out=$dndfile --force > $tmpfile";
  system($cmd);
  my $dndData = "";

  open(D, "$dndfile"); #This is for the iTOL input. 
  while(<D>) {
    my $revData = reverse($_);
    $revData =~ s/:/%/;
    $revData =~ s/:/_/;
    $revData = reverse($revData);
    $revData =~ s/%/:/;
    $dndData = $dndData . $revData . "\n";
  }
  close D;

  ## Interacting with iTOL to make a tree.
  ## NOTE - check elsewhere this is used when done. SNP etc.
  ## This uses the dnd file out put.

  my $ua = LWP::UserAgent->new;
  my $request = HTTP::Request::Common::POST( 'https://itol.embl.de/upload.cgi',
     Content_Type => 'form-data',
     Content      => [
                       ttext => $dndData,
                     ]);
  my $response = $ua->request($request);
  # print Dumper $response->{'_headers'}->{'location'};
  # print Dumper $response->content;
  my $iTOLLink =  "https://itol.embl.de/" . $response->{'_headers'}->{'location'};
  my $iTOLHTML = "<a href='$iTOLLink' target='_blank'><h4>Click here to view a phylogenetic tree of the alignment.</h4></a>";
  &createHTML($iTOLHTML,$outfile,$cgi);

  open(D, "$dndfile"); # Printing the dendrogram on the results page.
  print "<pre>";
  print "<hr>.dnd file\n\n";
  while(<D>) {
	print $_;
  }
}

sub error {
  my ($msg) = @_;
  print "ERROR: $msg\n\n";
  exit(1);
}


sub createHTML {
  my ($iTOLLINK, $outfile, $cgi) = @_;
  open(O, "$outfile") or die "can't open $outfile for reading:$!";

  my $userOutFormat = $cgi->param('clustalOutFormat');
  if ((! defined $userOutFormat) || ($userOutFormat eq "")){
    $userOutFormat = "clu";
  }

  print "<pre>";
    while(<O>) {
      if(/CLUSTAL O/ && $userOutFormat eq "clu") {
        print $cgi->h3($_);
        print $cgi->pre($iTOLLINK);
      }
      else {
        print;
      }
    }
   close O;
  print "</pre>";
}

1;
