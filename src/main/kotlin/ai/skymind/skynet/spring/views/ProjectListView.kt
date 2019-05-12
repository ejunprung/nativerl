package ai.skymind.skynet.spring.views

import ai.skymind.skynet.spring.services.Project
import ai.skymind.skynet.spring.services.ProjectService
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.Route

@Route("projects")
class ProjectListView(
        val projectService: ProjectService
): AppLayout() {
    val grid = Grid(Project::class.java)
    val filterText = TextField()

    init {
        setBranding(Span("Skymind"))
        setContent(VerticalLayout().apply {
            grid.apply {
                setSelectionMode(Grid.SelectionMode.SINGLE)
                setColumns("modelName", "dateCreated")
                addItemClickListener {
                    ui.ifPresent { it.navigate(ExperimentListView::class.java) }
                }
            }

            filterText.apply {
                placeholder = "search..."
                isClearButtonVisible = true
                valueChangeMode = ValueChangeMode.EAGER
                addValueChangeListener {
                    updateList()
                }
            }

            add(HorizontalLayout(
                    H2("Projects"),
                    Button("New Project").apply{
                        addClickListener { ui.ifPresent { it.navigate(ProjectCreateView::class.java) } }
                    }
            ).apply {
                setWidthFull()
                alignItems = FlexComponent.Alignment.BASELINE
            })
            add(filterText)
            add(grid)

            updateList()
        })
    }

    fun updateList(){
        val foundItems = projectService.find(filterText.value)
        grid.setItems(foundItems)
    }
}