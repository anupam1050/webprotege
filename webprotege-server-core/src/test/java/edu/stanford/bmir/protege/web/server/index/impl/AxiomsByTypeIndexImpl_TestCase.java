package edu.stanford.bmir.protege.web.server.index.impl;

import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.server.change.AddAxiomChange;
import edu.stanford.bmir.protege.web.server.change.RemoveAxiomChange;
import edu.stanford.bmir.protege.web.shared.project.OntologyDocumentId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-07
 */
@RunWith(MockitoJUnitRunner.class)
public class AxiomsByTypeIndexImpl_TestCase {

    private AxiomsByTypeIndexImpl impl;

    @Mock
    private OntologyDocumentId ontologyDocumentId;

    @Mock
    private OWLSubClassOfAxiom axiom, axiom2;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        impl = new AxiomsByTypeIndexImpl();
        when(axiom.getAxiomType())
                .thenReturn((AxiomType) AxiomType.SUBCLASS_OF);
        when(axiom2.getAxiomType())
                .thenReturn((AxiomType) AxiomType.SUBCLASS_OF);
        impl.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyDocumentId, axiom)));
    }

    @Test
    public void shouldGetSubClassOfAxiom() {
        var axiomsStream = impl.getAxiomsByType(AxiomType.SUBCLASS_OF, ontologyDocumentId);
        var axioms = axiomsStream.collect(toSet());
        assertThat(axioms, hasItem(axiom));
    }

    @Test
    public void shouldContainAxiom() {
        var containsAxioms = impl.containsAxiom(axiom, ontologyDocumentId);
        assertThat(containsAxioms, is(true));
    }

    @Test
    public void shouldNotContainAxiom() {
        var subClassOfAxiom = mock(OWLSubClassOfAxiom.class);
        when(subClassOfAxiom.getAxiomType())
                .thenReturn((AxiomType) AxiomType.SUBCLASS_OF);
        var containsAxiom = impl.containsAxiom(subClassOfAxiom, ontologyDocumentId);
        assertThat(containsAxiom, is(false));
    }


    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldThrowNpeIfContainsAxiom_Axiom_IsNull() {
        impl.containsAxiom(null, ontologyDocumentId);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldThrowNpeIfContainsAxiom_OntologyId_IsNull() {
        impl.containsAxiom(axiom, null);
    }

    @Test
    public void shouldReturnEmptyStreamIfForUnknownOntologyId() {
        var axiomsStream = impl.getAxiomsByType(AxiomType.SUBCLASS_OF, mock(OntologyDocumentId.class));
        var axioms = axiomsStream.collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @Test
    public void shouldHandleAddAxiom() {
        impl.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyDocumentId, axiom2)));
        var axioms = impl.getAxiomsByType(AxiomType.SUBCLASS_OF, ontologyDocumentId).collect(toSet());
        assertThat(axioms, hasItem(axiom2));
    }

    @Test
    public void shouldHandleRemoveAxiom() {
        impl.applyChanges(ImmutableList.of(RemoveAxiomChange.of(ontologyDocumentId, axiom)));
        var axioms = impl.getAxiomsByType(AxiomType.SUBCLASS_OF, ontologyDocumentId).collect(toSet());
        assertThat(axioms, not(hasItem(axiom)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNpeIfAxiomTypeIsNull() {
        impl.getAxiomsByType(null, ontologyDocumentId);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNpeIfOntologyIdIsNull() {
        impl.getAxiomsByType(AxiomType.SUBCLASS_OF, null);
    }
}
