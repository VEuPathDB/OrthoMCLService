package OrthoMCLWebsite::Model::Ppe::Other;

use OrthoMCLWebsite::Model::Ppe::Comparison;

@ISA = qw( OrthoMCLWebsite::Model::Ppe::Comparison );

use strict;
use Data::Dumper;

sub new {
    my ($class,
	$comparator, # '=', '<', '>', '>=', '<='
	$value,      # numeric value 
	$proteinOrTaxonFlag) # 'P' or 'T'
	= @_;

    my $self = {};

    bless($self, $class);
    $self->{comparator} = $comparator;
    $self->{value} = $value;
    $self->{proteinOrTaxonFlag} = $proteinOrTaxonFlag;

    return $self;
}

sub setTaxa {
    my ($self, $taxa) = @_;
    $self->{taxa} = $taxa;
}

sub toSqlString {
    my ($self, $columnMgr) = @_;

    my $taxaString = $self->getTaxaString($columnMgr);
    my $allCol = $columnMgr->getColumnName('ALL', $self->{proteinOrTaxonFlag});
    return "$allCol - ($taxaString) $self->{comparator} $self->{value}";
}

1;
