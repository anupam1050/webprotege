package edu.stanford.bmir.protege.web.server.mansyntax.render;

import com.google.common.collect.Lists;
import edu.stanford.bmir.protege.web.server.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import edu.stanford.bmir.protege.web.shared.project.OntologyDocumentId;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ObjectPropertySubPropertyOfRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLObjectProperty, OWLSubObjectPropertyOfAxiom, OWLObjectPropertyExpression> {

    @Nonnull
    private final SubObjectPropertyAxiomsBySubPropertyIndex axiomsIndex;

    @Inject
    public ObjectPropertySubPropertyOfRenderer(@Nonnull SubObjectPropertyAxiomsBySubPropertyIndex axiomsIndex) {
        this.axiomsIndex = checkNotNull(axiomsIndex);
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.SUB_PROPERTY_OF;
    }

    @Override
    protected Set<OWLSubObjectPropertyOfAxiom> getAxiomsInOntology(OWLObjectProperty subject, OntologyDocumentId ontologyDocumentId) {
        return axiomsIndex.getSubPropertyOfAxioms(subject, ontologyDocumentId).collect(toSet());
    }

    @Override
    public List<OWLObjectPropertyExpression> getRenderablesForItem(OWLObjectProperty subject,
                                                                   OWLSubObjectPropertyOfAxiom item,
                                                                   OntologyDocumentId ontologyDocumentId) {
        return Lists.newArrayList(item.getSuperProperty());
    }

    @Override
    public String getSeparatorAfter(int renderableIndex, List<OWLObjectPropertyExpression> renderables) {
        return ", ";
    }
}
