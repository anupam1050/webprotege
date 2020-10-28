package edu.stanford.bmir.protege.web.server.mansyntax.render;

import edu.stanford.bmir.protege.web.shared.project.OntologyDocumentId;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.Comparator;
import java.util.List;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public interface FrameSectionRenderer<E extends OWLObject, I, R> {


    enum Formatting {
        LINE_PER_ITEM,
        ALL_ONE_ONE_LINE
    }

    ManchesterOWLSyntax getSection();

    Formatting getSectionFormatting();

    List<I> getItemsInOntology(E subject, OntologyDocumentId ontologyDocumentId, ShortFormProvider shortFormProvider, Comparator<OWLObject> objectComparator);

    List<R> getRenderablesForItem(E subject, I item, OntologyDocumentId ontologyDocumentId);

    String getSeparatorAfter(int renderableIndex, List<R> renderables);

    List<OWLAnnotation> getAnnotations(I item);


}
