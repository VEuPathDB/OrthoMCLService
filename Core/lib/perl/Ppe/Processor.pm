package OrthoMCLWebsite::Model::Ppe::Processor;

@ISA = qw( ApiCommonWebsite::View::CgiApp );

use strict;
use ApiCommonWebsite::View::CgiApp;
use OrthoMCLModel::Ppe::MatrixColumnManager;
use OrthoMCLWebsite::Model::Ppe::Parser;

sub run {
  my ($self, $cgi) = @_;

  print $cgi->header('text/plain');

  my $dbh = $self->getQueryHandle();
  my $expression = $cgi->param('expression');
  &error("missing 'expression' param") unless $expression;


  my $groupIds = $self->processPpe($dbh, $expression, 1);

  exit();
}

sub processPpe {
  my ($self, $dbh, $ppeExpression, $fromCmdLine) = @_;

  my $columnMgr = OrthoMCLModel::Ppe::MatrixColumnManager->new($dbh);
  my $boolean = &parsePpeExpression($ppeExpression);
  $boolean->setOtherTaxa();
  my $whereClause = $boolean->toSqlString($columnMgr);

  my $sql = "
SELECT ortholog_group_id
FROM apidb.GroupTaxonMatrix
WHERE $whereClause
ORDER BY ortholog_group_id
";

  my $stmt = $dbh->prepare($sql);
  $stmt->execute();
  my $groupIds = [];
  while (my ($id) = $stmt->fetchrow_array) {
      push(@$groupIds, $id);
  }
  if ($fromCmdLine) {
    my $count = scalar(@$groupIds);
    print "found $count groups\n";
    print "$whereClause\n";
  }

  return $groupIds;
}

sub parsePpeExpression {
    my($expression) = @_;
    $expression =~ s/plus/\+/g;
    
    my $parser = OrthoMCLWebsite::Model::Ppe::Parser->new();

    $parser->YYData()->{INPUT} = $expression;

    return $parser->YYParse(yylex => \&OrthoMCLWebsite::Model::Ppe::Parser::Lexer,
			    yyerror => \&OrthoMCLWebsite::Model::Ppe::Parser::Error,
			    yydebug => 0);  # set to 0x01 for lexer debug
}

sub error {
  my ($msg) = @_;

  print "ERROR: $msg\n\n";
  exit(1);
}


