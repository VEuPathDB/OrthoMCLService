package OrthoMCLWebsite::Model::Ppe::Boolean;

use strict;

sub new {
    my ($class,
	$head, # a Comparison, OtherComparison or Boolean
	$tail, # a recursive list of Booleans or undef
        $type) # 'AND' or 'OR'
	= @_;

    my $self = {};

    bless($self, $class);
    $self->{head} = $head;
    $self->{tail} = $tail;
    $self->{type} = $type;

    return $self;
}


# this is made complicated by needing to handle OTHER
# to do so, we 
sub toString {
    my ($self) = @_;

    my $s = $self->{head}->toString();

    $s = "($s)" if ref($self->{head}) =~ /Boolean/;

    if ($self->{tail}) {
	$s = "$s $self->{type} " . $self->{tail}->toString();
    }
    return $s;
}

sub toSqlString {
    my ($self,$columnMgr) = @_;

    my $s = $self->{head}->toSqlString($columnMgr);
    $s = "($s)" if ref($self->{head}) =~ /Boolean/;

    if ($self->{tail}) {
	$s = "$s $self->{type} " . $self->{tail}->toSqlString($columnMgr);
    }
    return $s;
}

# call getOtherAndTaxa 
# if Other found then set its taxa w/ the taxa list
sub setOtherTaxa {
    my ($self) = @_;

    my ($taxa,$other) = $self->getOtherAndTaxa();
    $other->setTaxa($taxa) if $other;
}

# recurse through my peer boolean elements and collect the list of
# all taxa.  also find the Other if any
# (if more than one Other, the headmost wins)
sub getOtherAndTaxa {
    my ($self) = @_;

    my ($headTaxa, $tailTaxa, $other);

    # if my head is an Other, set $other (and don't collect taxa
    # because Others don't have any)
    if (ref($self->{head}) =~ /Other/) {
	$other = $self->{head};
    } 
    # otherwise, remember taxa found in head
    else {
	($headTaxa) = $self->{head}->getOtherAndTaxa();
    }

    # if head is Boolean, then it is its own root tree.
    # all root trees can have an other, which needs to be initialized
    if (ref($self->{head}) eq 'OrthoMCLWebsite::Model::Ppe::Boolean') {
	$self->{head}->setOtherTaxa();
    }

    # if we have a tail, collect its taxa
    # if we don't already have an Other and there is one in the tail
    # use it.  (if we have both, error).
    if ($self->{tail}) {
	my $other2;
	($tailTaxa, $other2) = $self->{tail}->getOtherAndTaxa();
	if ($other && $other2) {
	    die "clashing OTHERs: [" . $other->toString . "] and [" . $other2->toString() . "]";
	}
	$other = $other2 unless $other;
    }

    return (&hashUnion($tailTaxa, $headTaxa), $other);
}

sub hashUnion {
    my ($hash1, $hash2) = @_;

    my $hash3;
    foreach my $key (keys(%$hash1)) {
#	$hash3->{$key} = 0 unless $hash3->{$key};
	$hash3->{$key} += $hash1->{$key};
    }
    foreach my $key (keys(%$hash2)) {
#	$hash3->{$key} = 0 unless $hash3->{$key};
	$hash3->{$key} += $hash2->{$key};
    }
    return $hash3;
}

1;
