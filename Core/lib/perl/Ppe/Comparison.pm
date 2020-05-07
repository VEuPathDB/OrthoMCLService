package OrthoMCLWebsite::Model::Ppe::Comparison;

use strict;
use Data::Dumper;

sub new {
    my ($class,
	$taxa,   # one or more species or clades
	$comparator, # '=', '<', '>', '>=', '<='
	$value,      # numeric value 
	$proteinOrTaxonFlag) # 'P' or 'T'
	= @_;

    my $self = {};

    bless($self,$class);
    $self->{taxa} = {};
    map {$self->{taxa}->{$_} = 1} @$taxa; # convert from list to hash
    $self->{comparator} = $comparator;
    $self->{value} = $value;
    $self->{proteinOrTaxonFlag} = $proteinOrTaxonFlag;

    return $self;
}

sub getOtherAndTaxa {
    my ($self) = @_;

    return $self->{taxa};
}

sub toSqlString {
    my ($self, $columnMgr) = @_;

    my $taxaString = $self->getTaxaString($columnMgr);
    return "$taxaString $self->{comparator} $self->{value}";
}

sub getTaxaString {
    my ($self, $columnMgr) = @_;

    my @columns = map { $columnMgr->getColumnName($_, $self->{proteinOrTaxonFlag}) } keys(%{$self->{taxa}});
    my $taxaString = join(" + ", @columns);

}

1;
