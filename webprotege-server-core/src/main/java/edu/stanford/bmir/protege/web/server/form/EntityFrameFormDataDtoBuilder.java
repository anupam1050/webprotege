package edu.stanford.bmir.protege.web.server.form;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.server.frame.FrameComponentSessionRenderer;
import edu.stanford.bmir.protege.web.server.pagination.PageCollector;
import edu.stanford.bmir.protege.web.shared.entity.IRIData;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.entity.OWLPrimitiveData;
import edu.stanford.bmir.protege.web.shared.form.FormDescriptor;
import edu.stanford.bmir.protege.web.shared.form.FormPageRequest;
import edu.stanford.bmir.protege.web.shared.form.data.*;
import edu.stanford.bmir.protege.web.shared.form.field.*;
import edu.stanford.bmir.protege.web.shared.lang.LangTagFilter;
import edu.stanford.bmir.protege.web.shared.pagination.Page;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLPrimitive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static edu.stanford.bmir.protege.web.shared.form.field.GridControlOrderByDirection.ASC;
import static java.util.stream.Collectors.toMap;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-10-31
 */
public class EntityFrameFormDataDtoBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final FrameComponentSessionRenderer sessionRenderer;

    @Nonnull
    private final GridRowDataDtoComparatorFactory gridRowDataDtoComparatorFactory;

    @Nonnull
    private final TextControlValuesBuilder textControlValuesBuilder;

    @Nonnull
    private final NumberControlValuesBuilder numberControlValuesBuilder;

    @Nonnull
    private final MultiChoiceControlValueBuilder multiChoiceControlValueBuilder;

    @Nonnull
    private final SingleChoiceControlValuesBuilder SingleChoiceControlValuesBuilder;

    @Nonnull
    private final EntityNameControlValuesBuilder entityNameControlValuesBuilder;

    @Nonnull
    private final ImageControlValuesBuilder imageControlValuesBuilder;

    @AutoFactory
    @Inject
    public EntityFrameFormDataDtoBuilder(@Provided @Nonnull BindingValuesExtractor bindingValuesExtractor,
                                         @Nonnull FrameComponentSessionRenderer sessionRenderer,
                                         @Provided @Nonnull GridRowDataDtoComparatorFactory gridRowDataDtoComparatorFactory,
                                         @Provided @Nonnull TextControlValuesBuilder textControlValuesBuilder,
                                         @Provided @Nonnull NumberControlValuesBuilder numberControlValuesBuilder,
                                         @Provided @Nonnull MultiChoiceControlValueBuilder multiChoiceControlValueBuilder,
                                         @Provided @Nonnull SingleChoiceControlValuesBuilder singleChoiceControlValuesBuilder,
                                         @Provided @Nonnull EntityNameControlValuesBuilder entityNameControlValuesBuilder,
                                         @Provided @Nonnull ImageControlValuesBuilder imageControlValuesBuilder) {
        this.bindingValuesExtractor = bindingValuesExtractor;
        this.sessionRenderer = sessionRenderer;
        this.gridRowDataDtoComparatorFactory = gridRowDataDtoComparatorFactory;
        this.textControlValuesBuilder = textControlValuesBuilder;
        this.numberControlValuesBuilder = numberControlValuesBuilder;
        this.multiChoiceControlValueBuilder = multiChoiceControlValueBuilder;
        SingleChoiceControlValuesBuilder = singleChoiceControlValuesBuilder;
        this.entityNameControlValuesBuilder = entityNameControlValuesBuilder;
        this.imageControlValuesBuilder = imageControlValuesBuilder;
    }

    private FormSubjectDto getFormSubject(OWLPrimitiveData root) {
        if (root instanceof IRIData) {
            return FormSubjectDto.get((IRIData) root);
        } else if (root instanceof OWLEntityData) {
            return FormSubjectDto.get((OWLEntityData) root);
        } else {
            throw new RuntimeException("Cannot process form subjects that are not IRIs or Entities");
        }
    }

    @Nullable
    private OWLEntityData toEntityFormSubject(OWLPrimitive primitive) {
        if (primitive instanceof OWLEntity) {
            return getRendering((OWLEntity) primitive);
        } else if (primitive instanceof IRI) {
            var iri = (IRI) primitive;
            return sessionRenderer.getRendering(iri).stream().sorted()
                                  .findFirst()
                                  .orElse(null);
        } else {
            return null;
        }
    }

    @Nonnull
    private OWLEntityData getRendering(@Nonnull OWLEntity entity) {
        return sessionRenderer.getEntityRendering(entity);
    }

    private ImmutableList<FormControlDataDto> toFormControlValues(@Nonnull OWLEntityData subject,
                                                                  @Nonnull FormRegionId formFieldId,
                                                                  @Nonnull BoundControlDescriptor descriptor,
                                                                  @Nonnull FormPageRequestIndex formPageRequestIndex,
                                                                  @Nonnull LangTagFilter langTagFilter,
                                                                  @Nonnull Optional<GridControlOrdering> regionOrdering,
                                                                  @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var owlBinding = descriptor.getOwlBinding();
        if (owlBinding.isEmpty()) {
            return ImmutableList.of();
        }
        var theBinding = owlBinding.get();
        var formControlDescriptor = descriptor.getFormControlDescriptor();
        return formControlDescriptor.accept(new FormControlDescriptorVisitor<>() {
            @Override
            public ImmutableList<FormControlDataDto> visit(TextControlDescriptor textControlDescriptor) {
                return textControlValuesBuilder.getTextControlDataDtoValues(textControlDescriptor,
                                                                            subject,
                                                                            theBinding,
                                                                            langTagFilter);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(NumberControlDescriptor numberControlDescriptor) {
                return numberControlValuesBuilder.getNumberControlDataDtoValues(numberControlDescriptor,
                                                                                subject,
                                                                                theBinding);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(SingleChoiceControlDescriptor singleChoiceControlDescriptor) {
                return SingleChoiceControlValuesBuilder.getSingleChoiceControlDataDtoValues(singleChoiceControlDescriptor,
                                                                                            subject,
                                                                                            theBinding,
                                                                                            langTagFilter);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(MultiChoiceControlDescriptor multiChoiceControlDescriptor) {
                return multiChoiceControlValueBuilder.getMultiChoiceControlDataDtoValues(multiChoiceControlDescriptor,
                                                                                         subject,
                                                                                         theBinding);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(EntityNameControlDescriptor entityNameControlDescriptor) {
                return entityNameControlValuesBuilder.getEntityNameControlDataDtoValues(entityNameControlDescriptor,
                                                                                        subject,
                                                                                        theBinding);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(ImageControlDescriptor imageControlDescriptor) {
                return imageControlValuesBuilder.getImageControlDataDtoValues(imageControlDescriptor,
                                                                              subject,
                                                                              theBinding);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(GridControlDescriptor gridControlDescriptor) {
                return getGridControlDataDtoValues(gridControlDescriptor,
                                                   subject,
                                                   theBinding,
                                                   formFieldId,
                                                   formPageRequestIndex,
                                                   langTagFilter,
                                                   regionOrdering,
                                                   orderings);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(SubFormControlDescriptor subFormControlDescriptor) {
                return getSubFormControlDataDtoValues(subFormControlDescriptor,
                                                      subject,
                                                      theBinding,
                                                      formPageRequestIndex,
                                                      langTagFilter,
                                                      orderings);
            }
        });
    }

    private ImmutableList<FormControlDataDto> getSubFormControlDataDtoValues(SubFormControlDescriptor subFormControlDescriptor,
                                                                             @Nonnull OWLEntityData subject,
                                                                             @Nonnull OwlBinding theBinding,
                                                                             @Nonnull FormPageRequestIndex formPageRequestIndex,
                                                                             @Nonnull LangTagFilter langTagFilter,
                                                                             @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        FormDescriptor subFormDescriptor = subFormControlDescriptor.getFormDescriptor();
        return values.stream()
                     .filter(p -> p instanceof OWLEntity)
                     .map(p -> (OWLEntity) p)
                     .map(entity -> toFormData(entity,
                                               subFormDescriptor,
                                               formPageRequestIndex,
                                               langTagFilter,
                                               orderings))
                     .collect(toImmutableList());
    }

    private ImmutableList<FormControlDataDto> getGridControlDataDtoValues(GridControlDescriptor gridControlDescriptor, @Nonnull OWLEntityData subject, OwlBinding theBinding, @Nonnull FormRegionId formFieldId, @Nonnull FormPageRequestIndex formPageRequestIndex, @Nonnull LangTagFilter langTagFilter, @Nonnull Optional<GridControlOrdering> regionOrdering, @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return ImmutableList.of(toGridControlData(subject,
                                                  formFieldId,
                                                  values,
                                                  gridControlDescriptor,
                                                  formPageRequestIndex,
                                                  langTagFilter,
                                                  regionOrdering,
                                                  orderings));
    }


    public FormDataDto toFormData(@Nonnull OWLEntity subject,
                                  @Nonnull FormDescriptor formDescriptor,
                                  @Nonnull FormPageRequestIndex formPageRequestIndex,
                                  @Nonnull LangTagFilter langTagFilter,
                                  @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var orderingMap = orderings.stream()
                                   .collect(toMap(GridControlOrdering::getFieldId,
                                                  ordering -> ordering,
                                                  (left, right) -> left));
        var subjectData = getRendering(subject);
        var formSubject = getFormSubject(subjectData);
        var fieldData = formDescriptor.getFields()
                                      .stream()
                                      .map(field -> {
                                          var ordering = Optional.ofNullable(orderingMap.get(field.getId()));
                                          var formControlValues = toFormControlValues(subjectData,
                                                                                      field.getId(),
                                                                                      field,
                                                                                      formPageRequestIndex,
                                                                                      langTagFilter,
                                                                                      ordering,
                                                                                      orderings);
                                          var controlValuesStream = formControlValues
                                                  .stream()
                                                  .filter(fcv -> isIncluded(fcv, langTagFilter));
                                          var pageRequest = formPageRequestIndex.getPageRequest(formSubject.toFormSubject(),
                                                                                                field.getId(),
                                                                                                FormPageRequest.SourceType.CONTROL_STACK);
                                          var controlValuesPage = controlValuesStream.collect(PageCollector.toPage(
                                                  pageRequest.getPageNumber(),
                                                  pageRequest.getPageSize()
                                          ))
                                                                                     .orElse(Page.emptyPage());
                                          return FormFieldDataDto.get(field, controlValuesPage);
                                      })
                                      .collect(toImmutableList());
        return FormDataDto.get(formSubject, formDescriptor, fieldData);
    }

    private GridControlDataDto toGridControlData(OWLPrimitiveData root,
                                                 FormRegionId formFieldId,
                                                 ImmutableList<OWLPrimitive> subjects,
                                                 GridControlDescriptor gridControlDescriptor,
                                                 @Nonnull FormPageRequestIndex formPageRequestIndex,
                                                 @Nonnull LangTagFilter langTagFilter,
                                                 @Nonnull Optional<GridControlOrdering> regionOrdering,
                                                 @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var rootSubject = getFormSubject(root);
        var pageRequest = formPageRequestIndex.getPageRequest(rootSubject.toFormSubject(),
                                                              formFieldId,
                                                              FormPageRequest.SourceType.GRID_CONTROL);
        var comparator = regionOrdering.filter(ordering -> ordering.getFieldId().equals(formFieldId))
                                       .map(ordering ->  gridRowDataDtoComparatorFactory.get(ordering.getOrdering(), gridControlDescriptor))
                                       .orElseGet(() -> gridRowDataDtoComparatorFactory.get(ImmutableList.of(), gridControlDescriptor));
        var rowData = subjects.stream()
                              .map(this::toEntityFormSubject)
                              .filter(Objects::nonNull)
                              .map(entity -> toGridRow(entity,
                                                       gridControlDescriptor,
                                                       formPageRequestIndex,
                                                       langTagFilter,
                                                       orderings))
                              .filter(Objects::nonNull)
                              .sorted(comparator)
                              .collect(PageCollector.toPage(pageRequest.getPageNumber(),
                                                            pageRequest.getPageSize()));
        var gridOrdering = regionOrdering.map(GridControlOrdering::getOrdering)
                                         .orElseGet(() -> {
                                             return gridControlDescriptor.getLeafColumns()
                                                                  .map(GridColumnDescriptor::getId)
                                                                  .map(id -> GridControlOrderBy.get(id, ASC))
                                                                  .limit(1)
                                                                  .collect(toImmutableList());
                                         });
        return GridControlDataDto.get(gridControlDescriptor, rowData.orElse(Page.emptyPage()), gridOrdering);
    }

    /**
     * Generate a row of a grid for the specified row subject
     *
     * @param rowSubject            The row subject
     * @param gridControlDescriptor The grid control descriptor
     * @return null if there is no row for the specified subject (because it is filtered out)
     */
    @Nullable
    private GridRowDataDto toGridRow(OWLEntityData rowSubject,
                                     GridControlDescriptor gridControlDescriptor,
                                     @Nonnull FormPageRequestIndex formPageRequestIndex,
                                     @Nonnull LangTagFilter langTagFilter,
                                     @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var columnDescriptors = gridControlDescriptor.getColumns();
        // To Cells
        var cellData = toGridRowCells(rowSubject,
                                      columnDescriptors,
                                      formPageRequestIndex,
                                      langTagFilter,
                                      orderings);
        if (cellData.isEmpty()) {
            return null;
        }
        var formSubject = FormEntitySubjectDto.get(rowSubject);
        return GridRowDataDto.get(formSubject, cellData);
    }

    private ImmutableList<GridCellDataDto> toGridRowCells(OWLEntityData rowSubject,
                                                          @Nonnull ImmutableList<GridColumnDescriptor> columnDescriptors,
                                                          @Nonnull FormPageRequestIndex formPageRequestIndex,
                                                          @Nonnull LangTagFilter langTagFilter,
                                                          @Nonnull ImmutableList<GridControlOrdering> orderings) {
        var resultBuilder = ImmutableList.<GridCellDataDto>builder();
        for (var columnDescriptor : columnDescriptors) {
            var formControlData = toFormControlValues(
                    rowSubject,
                    columnDescriptor.getId(),
                    columnDescriptor,
                    formPageRequestIndex,
                    langTagFilter,
                    Optional.empty(),
                    orderings);
            if (formControlData.isEmpty()) {
                var cellData = GridCellDataDto.get(columnDescriptor.getId(), Page.emptyPage());
                resultBuilder.add(cellData);
            } else {
                if (columnDescriptor.getRepeatability() == Repeatability.NON_REPEATABLE) {
                    var firstValue = formControlData.get(0);
                    if (isIncluded(firstValue, langTagFilter)) {
                        var cellData = GridCellDataDto.get(columnDescriptor.getId(),
                                                           Page.of(firstValue));
                        resultBuilder.add(cellData);
                    } else {
                        return ImmutableList.of();
                    }

                } else {
                    var cellData = GridCellDataDto.get(columnDescriptor.getId(),
                                                       new Page<>(1, 1, formControlData, formControlData.size()));
                    resultBuilder.add(cellData);
                }
            }
        }
        return resultBuilder.build();
    }

    private boolean isIncluded(@Nonnull FormControlDataDto formControlData,
                               @Nonnull LangTagFilter langTagFilter) {
        FormControlDataLangTagBasedInclusion formControlDataLangTagBasedInclusion = new FormControlDataLangTagBasedInclusion(
                langTagFilter);
        return formControlDataLangTagBasedInclusion.isIncluded(formControlData);

    }
}
